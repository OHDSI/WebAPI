package org.ohdsi.webapi.mcp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Turns a {@link ToolDefinition} into an MCP tool whose every call is traced and audited.
 *
 * <p>For each call: the {@code _meta.traceparent} is continued (or a trace started), the audit row is
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

    private final McpAuditService audit;
    private final ObjectMapper mapper;
    private final boolean requireTraceparent;
    private final int minRationaleLength;

    public McpToolFactory(McpAuditService audit, ObjectMapper mapper,
            @Value("${mcp.requireTraceparent:false}") boolean requireTraceparent,
            @Value("${mcp.minRationaleLength:20}") int minRationaleLength) {
        this.audit = audit;
        this.mapper = mapper;
        this.requireTraceparent = requireTraceparent;
        this.minRationaleLength = minRationaleLength;
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
        ToolArguments args = new ToolArguments(request.arguments());

        TraceContext trace;
        String traceProblem = null;
        Object traceparent = meta.get(META_TRACEPARENT);
        if (traceparent instanceof String tp && !tp.isBlank()) {
            try {
                trace = TraceContext.continueFrom(tp);
            } catch (IllegalArgumentException e) {
                trace = TraceContext.newRoot();
                traceProblem = e.getMessage();
            }
        } else {
            trace = TraceContext.newRoot();
            if (requireTraceparent) {
                traceProblem = "_meta.traceparent is required by this server";
            }
        }
        String step = meta.get(META_REASONING_STEP) instanceof String s && !s.isBlank() ? s : def.step.name();
        String rationale = args.raw(CLINICAL_RATIONALE) instanceof String s ? s : null;
        String evidence = args.raw(EVIDENCE_REFERENCE) instanceof String s ? s : null;

        McpAuditEntry entry;
        try {
            entry = audit.start(trace, def.name, step, toJson(args.asMap()), rationale, evidence);
        } catch (Exception e) {
            log.error("MCP audit trail unavailable; refusing to run tool {}", def.name, e);
            return error(trace, null, Outcome.ERROR, "The audit trail could not be written, so the tool was not run.");
        }

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
        try {
            audit.finish(entry, outcome, resultJson, message);
        } catch (Exception e) {
            // The action already happened and its STARTED row is on record; do not hide the result.
            log.error("Could not record outcome {} of MCP audit entry {}", outcome, entry.getId(), e);
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
