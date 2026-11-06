package org.ohdsi.webapi.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.ohdsi.webapi.mcp.ToolDefinition.Rationale;
import org.ohdsi.webapi.mcp.audit.McpAuditEntry;
import org.ohdsi.webapi.mcp.audit.McpAuditEntry.Outcome;
import org.ohdsi.webapi.mcp.audit.McpAuditService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Turns a {@link ToolDefinition} into an MCP tool whose every call is traced and audited.
 *
 * <p>For each call: an OpenTelemetry server span is opened as a child of {@code _meta.traceparent}
 * (MCP semantic conventions; a no-op without OpenTelemetry), the audit row is
 * written <em>before</em> the tool runs, the rationale contract is enforced, the tool runs under the
 * caller's WebAPI identity (so the services' {@code @PreAuthorize} rules apply unchanged), and the
 * audit row is completed with the outcome. The result carries the tool span's {@code traceparent}
 * and the audit entry id in its {@code _meta}.
 */
@Component
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public class McpToolFactory {

    public static final String CLINICAL_RATIONALE = "clinical_rationale";
    public static final String EVIDENCE_REFERENCE = "evidence_reference";
    public static final String META_TRACEPARENT = "traceparent";
    public static final String META_REASONING_STEP = "reasoning_step";
    public static final String META_AUDIT_ENTRY_ID = "audit_entry_id";

    private static final Logger log = LoggerFactory.getLogger(McpToolFactory.class);

    // OpenTelemetry semantic conventions for MCP (gen-ai/mcp) plus WebAPI's own attributes.
    static final String MCP_METHOD = "tools/call";
    static final AttributeKey<String> ATTR_MCP_METHOD = AttributeKey.stringKey("mcp.method.name");
    static final AttributeKey<String> ATTR_OPERATION = AttributeKey.stringKey("gen_ai.operation.name");
    static final AttributeKey<String> ATTR_TOOL_NAME = AttributeKey.stringKey("gen_ai.tool.name");
    static final AttributeKey<String> ATTR_ERROR_TYPE = AttributeKey.stringKey("error.type");
    static final AttributeKey<String> ATTR_REASONING_STEP = AttributeKey.stringKey("ohdsi.mcp.reasoning_step");
    static final AttributeKey<String> ATTR_OUTCOME = AttributeKey.stringKey("ohdsi.mcp.outcome");
    static final AttributeKey<Long> ATTR_AUDIT_ENTRY_ID = AttributeKey.longKey("ohdsi.mcp.audit_entry_id");
    static final AttributeKey<String> ATTR_ENTITY_TYPE = AttributeKey.stringKey("ohdsi.mcp.entity.type");
    static final AttributeKey<Long> ATTR_ENTITY_ID = AttributeKey.longKey("ohdsi.mcp.entity.id");

    /** Reads W3C trace context from {@code params._meta}, where MCP carries it instead of HTTP headers. */
    private static final TextMapGetter<Map<String, Object>> META_GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(Map<String, Object> carrier) {
            return carrier.keySet();
        }

        @Override
        public String get(Map<String, Object> carrier, String key) {
            return carrier != null && carrier.get(key) instanceof String value ? value : null;
        }
    };

    private final McpAuditService audit;
    private final ObjectMapper mapper;
    private final boolean requireTraceparent;
    private final int minRationaleLength;
    private final Tracer tracer;

    /**
     * Uses the application's {@link OpenTelemetry} bean if there is one, otherwise the global instance: the
     * OpenTelemetry Java agent's when WebAPI runs with it, a no-op otherwise.
     */
    @Autowired
    public McpToolFactory(McpAuditService audit, ObjectMapper mapper,
            @Value("${mcp.requireTraceparent:false}") boolean requireTraceparent,
            @Value("${mcp.minRationaleLength:20}") int minRationaleLength,
            ObjectProvider<OpenTelemetry> openTelemetry) {
        this(audit, mapper, requireTraceparent, minRationaleLength,
                openTelemetry.getIfAvailable(GlobalOpenTelemetry::get));
    }

    McpToolFactory(McpAuditService audit, ObjectMapper mapper, boolean requireTraceparent, int minRationaleLength,
            OpenTelemetry openTelemetry) {
        this.audit = audit;
        this.mapper = mapper;
        this.requireTraceparent = requireTraceparent;
        this.minRationaleLength = minRationaleLength;
        this.tracer = openTelemetry.getTracer("org.ohdsi.webapi.mcp");
    }

    public SyncToolSpecification build(ToolDefinition def) {
        return SyncToolSpecification.builder()
                .tool(describe(def))
                .callHandler((transportContext, request) -> call(def, request))
                .build();
    }

    McpSchema.Tool describe(ToolDefinition def) {
        Map<String, Object> properties = new LinkedHashMap<>(def.properties);
        List<String> required = new ArrayList<>(def.required);
        if (def.rationale != Rationale.NONE) {
            properties.put(CLINICAL_RATIONALE, ToolDefinition.stringParam(
                    "Why this action is clinically correct for the target phenotype: why each concept fits, why "
                    + "descendants are or are not included, and which terminology logic was applied. Stored in the "
                    + "append-only audit log."));
            properties.put(EVIDENCE_REFERENCE, ToolDefinition.stringParam(
                    "Guidelines, terminology hierarchies or literature consulted (e.g. 'SNOMED-CT clinical finding "
                    + "hierarchy; ADA Standards of Care'). Stored in the audit log."));
            if (def.rationale == Rationale.REQUIRED) {
                required.add(CLINICAL_RATIONALE);
                required.add(EVIDENCE_REFERENCE);
            }
        }
        String description = def.description
                + "\n\nSend the caller's W3C traceparent in _meta.traceparent (and optionally _meta.reasoning_step); "
                + "the result's _meta.traceparent is this call's span and _meta.audit_entry_id its audit record.";
        return McpSchema.Tool.builder()
                .name(def.name)
                .title(def.title)
                .description(description)
                .inputSchema(new McpSchema.JsonSchema("object", properties, required, false, null, null))
                .annotations(new McpSchema.ToolAnnotations(def.title, def.readOnly, def.destructive, null, false, null))
                .build();
    }

    CallToolResult call(ToolDefinition def, CallToolRequest request) {
        Map<String, Object> meta = request.meta() == null ? Map.of() : request.meta();

        // The client's traceparent is checked against the W3C rules whether or not OpenTelemetry is active,
        // so a malformed one is refused the same way in both cases.
        TraceContext clientTrace = null;
        String traceProblem = null;
        Object traceparent = meta.get(META_TRACEPARENT);
        if (traceparent instanceof String tp && !tp.isBlank()) {
            try {
                clientTrace = TraceContext.continueFrom(tp);
            } catch (IllegalArgumentException e) {
                traceProblem = e.getMessage();
            }
        } else if (requireTraceparent) {
            traceProblem = "_meta.traceparent is required by this server";
        }

        // MCP semantic conventions: the server span's parent is the context in params._meta, and the
        // ambient context (the HTTP request's span, when the OpenTelemetry agent runs) is linked.
        Span ambient = Span.current();
        Context parent = clientTrace != null
                ? W3CTraceContextPropagator.getInstance().extract(Context.current(), meta, META_GETTER)
                : Context.current();
        SpanBuilder spanBuilder = tracer.spanBuilder(MCP_METHOD + " " + def.name)
                .setSpanKind(SpanKind.SERVER)
                .setParent(parent)
                .setAttribute(ATTR_MCP_METHOD, MCP_METHOD)
                .setAttribute(ATTR_OPERATION, "execute_tool")
                .setAttribute(ATTR_TOOL_NAME, def.name);
        if (clientTrace != null && ambient.getSpanContext().isValid()) {
            spanBuilder.addLink(ambient.getSpanContext());
        }
        Span span = spanBuilder.startSpan();
        try (Scope ignored = span.makeCurrent()) {
            return run(def, request, meta, traceFor(span, clientTrace, ambient), traceProblem, span);
        } finally {
            span.end();
        }
    }

    /**
     * The ids recorded in the audit log and returned to the client. With OpenTelemetry active they are the
     * exported span's own, so an audit row and its trace in the tracing backend share one trace id. Without
     * it (no agent, no SDK) WebAPI allocates them itself.
     */
    static TraceContext traceFor(Span span, TraceContext clientTrace, Span ambient) {
        SpanContext context = span.getSpanContext();
        if (!context.isValid()) {
            return clientTrace != null ? clientTrace : TraceContext.newRoot();
        }
        String parentSpanId = null;
        if (clientTrace != null) {
            parentSpanId = clientTrace.parentSpanId();
        } else if (ambient.getSpanContext().isValid()
                && ambient.getSpanContext().getTraceId().equals(context.getTraceId())) {
            parentSpanId = ambient.getSpanContext().getSpanId();
        }
        return new TraceContext(context.getTraceId(), parentSpanId, context.getSpanId(),
                context.getTraceFlags().asHex(), clientTrace == null);
    }

    private CallToolResult run(ToolDefinition def, CallToolRequest request, Map<String, Object> meta,
            TraceContext trace, String traceProblem, Span span) {
        ToolArguments args = new ToolArguments(request.arguments());
        String step = meta.get(META_REASONING_STEP) instanceof String s && !s.isBlank() ? s : def.step.name();
        String rationale = args.raw(CLINICAL_RATIONALE) instanceof String s ? s : null;
        String evidence = args.raw(EVIDENCE_REFERENCE) instanceof String s ? s : null;
        span.setAttribute(ATTR_REASONING_STEP, step);

        McpAuditEntry entry;
        try {
            Integer entityId = def.entityType == null ? null : entityId(args.raw(def.entityIdField));
            entry = audit.start(trace, def.name, step, toJson(args.asMap()), rationale, evidence,
                    entityId == null ? null : def.entityType, entityId);
        } catch (Exception e) {
            log.error("MCP audit trail unavailable; refusing to run tool {}", def.name, e);
            markOutcome(span, Outcome.ERROR);
            return error(trace, null, Outcome.ERROR, "The audit trail could not be written, so the tool was not run.");
        }
        span.setAttribute(ATTR_AUDIT_ENTRY_ID, entry.getId());

        Outcome outcome;
        Object result = null;
        String message = null;
        try {
            if (traceProblem != null) {
                throw new ToolContractException(traceProblem);
            }
            checkRationale(def, rationale, evidence);
            result = def.handler.handle(args);
            outcome = Outcome.SUCCESS;
        } catch (ToolContractException e) {
            outcome = Outcome.REJECTED;
            message = e.getMessage();
        } catch (AccessDeniedException e) {
            outcome = Outcome.DENIED;
            message = "Access denied: the authenticated WebAPI user lacks the permission this tool needs.";
        } catch (ResponseStatusException e) {
            outcome = Outcome.ERROR;
            message = e.getReason() != null ? e.getReason() : e.getStatusCode().toString();
        } catch (Exception e) {
            log.warn("MCP tool {} failed", def.name, e);
            outcome = Outcome.ERROR;
            message = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        }

        String resultJson = outcome == Outcome.SUCCESS ? toJson(result) : null;
        Integer createdId = def.entityType != null && result instanceof Map<?, ?> m
                ? entityId(m.get(def.entityIdField)) : null;
        try {
            audit.finish(entry, outcome, resultJson, message, createdId == null ? null : def.entityType, createdId);
        } catch (Exception e) {
            // The action already happened and its STARTED row is on record; do not hide the result.
            log.error("Could not record outcome {} of MCP audit entry {}", outcome, entry.getId(), e);
        }
        markOutcome(span, outcome);
        if (entry.getEntityType() != null) {
            span.setAttribute(ATTR_ENTITY_TYPE, entry.getEntityType().name());
            span.setAttribute(ATTR_ENTITY_ID, entry.getEntityId().longValue());
        }

        if (outcome != Outcome.SUCCESS) {
            return error(trace, entry.getId(), outcome, message);
        }
        return CallToolResult.builder()
                .addTextContent(resultJson)
                .isError(false)
                .meta(resultMeta(trace, entry.getId(), step))
                .build();
    }

    /**
     * Outcome on the span. Only the outcome is recorded: arguments, rationale and error messages stay in the
     * audit log, which has per-artifact access control, and out of the tracing backend, which does not.
     */
    private static void markOutcome(Span span, Outcome outcome) {
        span.setAttribute(ATTR_OUTCOME, outcome.name());
        if (outcome != Outcome.SUCCESS) {
            span.setAttribute(ATTR_ERROR_TYPE, "tool_error");
            span.setStatus(StatusCode.ERROR, outcome.name());
        }
    }

    private static Integer entityId(Object value) {
        if (value instanceof Number n) {
            return n.intValue();
        }
        if (value instanceof String s && s.matches("\\d{1,9}")) {
            return Integer.valueOf(s);
        }
        return null;
    }

    private void checkRationale(ToolDefinition def, String rationale, String evidence) {
        if (def.rationale != Rationale.REQUIRED) {
            return;
        }
        if (rationale == null || rationale.strip().length() < minRationaleLength) {
            throw new ToolContractException("'" + CLINICAL_RATIONALE + "' is required for " + def.name
                    + " and must be at least " + minRationaleLength + " characters: state why this action fits the "
                    + "target phenotype and why descendants are or are not included.");
        }
        if (evidence == null || evidence.isBlank()) {
            throw new ToolContractException("'" + EVIDENCE_REFERENCE + "' is required for " + def.name
                    + ": name the terminology hierarchy or guideline consulted.");
        }
    }

    private CallToolResult error(TraceContext trace, Long auditId, Outcome outcome, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("outcome", outcome.name());
        body.put("message", message);
        return CallToolResult.builder()
                .addTextContent(toJson(body))
                .isError(true)
                .meta(resultMeta(trace, auditId, null))
                .build();
    }

    private static Map<String, Object> resultMeta(TraceContext trace, Long auditId, String step) {
        Map<String, Object> meta = new LinkedHashMap<>();
        meta.put(META_TRACEPARENT, trace.traceparent());
        if (auditId != null) {
            meta.put(META_AUDIT_ENTRY_ID, auditId);
        }
        if (step != null) {
            meta.put(META_REASONING_STEP, step);
        }
        return meta;
    }

    private String toJson(Object value) {
        try {
            return mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize MCP tool payload", e);
        }
    }
}
