package org.ohdsi.webapi.mcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.mockito.ArgumentCaptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import java.util.List;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.mcp.ToolDefinition.Rationale;
import org.ohdsi.webapi.mcp.audit.McpAuditEntry;
import org.ohdsi.webapi.mcp.audit.McpAuditEntry.EntityType;
import org.ohdsi.webapi.mcp.audit.McpAuditEntry.Outcome;
import org.ohdsi.webapi.mcp.audit.McpAuditService;
import org.springframework.security.access.AccessDeniedException;

public class McpToolFactoryTest {

    private static final String TRACE_ID = "4bf92f3577b34da6a3ce929d0e0e4736";
    private static final String TRACEPARENT = "00-" + TRACE_ID + "-00f067aa0ba902b7-01";

    private McpAuditService audit;
    private McpAuditEntry entry;

    @Before
    public void setUp() {
        audit = mock(McpAuditService.class);
        entry = new McpAuditEntry();
        entry.setId(42L);
        when(audit.start(any(), anyString(), anyString(), any(), any(), any(), any(), any())).thenReturn(entry);
    }

    private McpToolFactory factory(boolean requireTraceparent) {
        return new McpToolFactory(audit, new ObjectMapper(), requireTraceparent, 20, OpenTelemetry.noop());
    }

    private static ToolDefinition guarded(AtomicBoolean ran) {
        return ToolDefinition.tool("add_concept_to_set")
                .description("test")
                .step(ReasoningStep.STEP_2_CONCEPT_SET_CONSOLIDATION)
                .rationale(Rationale.REQUIRED)
                .handler(args -> {
                    ran.set(true);
                    return Map.of("ok", true);
                })
                .build();
    }

    private static CallToolRequest request(Map<String, Object> args, Map<String, Object> meta) {
        return new CallToolRequest("add_concept_to_set", args, meta);
    }

    @Test
    public void runsTheToolAndReturnsTheToolSpanInMeta() {
        AtomicBoolean ran = new AtomicBoolean();
        CallToolResult result = factory(false).call(guarded(ran), request(
                Map.of("clinical_rationale", "Core diagnosis concept for the phenotype.",
                        "evidence_reference", "SNOMED-CT hierarchy"),
                Map.of("traceparent", TRACEPARENT, "reasoning_step", "STEP_2_CONCEPT_SELECTION")));

        assertTrue(ran.get());
        assertFalse(result.isError());
        String traceparent = (String) result.meta().get("traceparent");
        assertTrue(traceparent, traceparent.startsWith("00-" + TRACE_ID + "-"));
        assertEquals(42L, result.meta().get("audit_entry_id"));
        verify(audit).start(any(), eq("add_concept_to_set"), eq("STEP_2_CONCEPT_SELECTION"), anyString(),
                eq("Core diagnosis concept for the phenotype."), eq("SNOMED-CT hierarchy"), isNull(), isNull());
        verify(audit).finish(eq(entry), eq(Outcome.SUCCESS), eq("{\"ok\":true}"), isNull(), any(), any());
    }

    @Test
    public void refusesACallWithoutRationaleButStillAuditsIt() {
        AtomicBoolean ran = new AtomicBoolean();
        CallToolResult result = factory(false).call(guarded(ran), request(Map.of(), Map.of("traceparent", TRACEPARENT)));

        assertFalse(ran.get());
        assertTrue(result.isError());
        verify(audit).finish(eq(entry), eq(Outcome.REJECTED), isNull(), any(), any(), any());
    }

    @Test
    public void refusesATooShortRationale() {
        AtomicBoolean ran = new AtomicBoolean();
        CallToolResult result = factory(false).call(guarded(ran), request(
                Map.of("clinical_rationale", "fits", "evidence_reference", "SNOMED"), Map.of()));

        assertFalse(ran.get());
        assertTrue(result.isError());
    }

    @Test
    public void refusesAMissingTraceparentWhenRequired() {
        AtomicBoolean ran = new AtomicBoolean();
        CallToolResult result = factory(true).call(guarded(ran), request(
                Map.of("clinical_rationale", "Core diagnosis concept for the phenotype.",
                        "evidence_reference", "SNOMED-CT hierarchy"),
                Map.of()));

        assertFalse(ran.get());
        assertTrue(result.isError());
        verify(audit).finish(eq(entry), eq(Outcome.REJECTED), isNull(), any(), any(), any());
    }

    @Test
    public void doesNotRunTheToolWhenTheAuditTrailCannotBeWritten() {
        when(audit.start(any(), anyString(), anyString(), any(), any(), any(), any(), any()))
                .thenThrow(new IllegalStateException("database down"));
        AtomicBoolean ran = new AtomicBoolean();
        CallToolResult result = factory(false).call(guarded(ran), request(
                Map.of("clinical_rationale", "Core diagnosis concept for the phenotype.",
                        "evidence_reference", "SNOMED-CT hierarchy"),
                Map.of()));

        assertFalse(ran.get());
        assertTrue(result.isError());
        verify(audit, never()).finish(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void reportsAuthorizationFailuresAsDenied() {
        ToolDefinition def = ToolDefinition.tool("get_concept_set")
                .description("test")
                .step(ReasoningStep.STEP_2_CONCEPT_SET_CONSOLIDATION)
                .handler(args -> {
                    throw new AccessDeniedException("nope");
                })
                .build();
        CallToolResult result = factory(false).call(def, new CallToolRequest("get_concept_set", Map.of(), null));

        assertTrue(result.isError());
        verify(audit).finish(eq(entry), eq(Outcome.DENIED), isNull(), any(), any(), any());
    }

    @Test
    public void advertisesRationaleAsRequiredInTheInputSchema() {
        McpSchema.Tool tool = factory(false).describe(guarded(new AtomicBoolean()));
        assertTrue(tool.inputSchema().required().contains("clinical_rationale"));
        assertTrue(tool.inputSchema().required().contains("evidence_reference"));
        assertFalse(tool.annotations().readOnlyHint());
    }

    @Test
    public void recordsTheEntityFromTheArgumentsBeforeTheToolRuns() {
        ToolDefinition def = ToolDefinition.tool("generate_cohort")
                .description("test")
                .step(ReasoningStep.STEP_5_GENERATION_VERIFICATION)
                .entity(EntityType.COHORT_DEFINITION, "cohort_definition_id")
                .handler(args -> Map.of("status", "STARTING"))
                .build();
        factory(false).call(def, new CallToolRequest("generate_cohort", Map.of("cohort_definition_id", 7), Map.of()));
        verify(audit).start(any(), eq("generate_cohort"), anyString(), anyString(), isNull(), isNull(),
                eq(EntityType.COHORT_DEFINITION), eq(7));
    }

    @Test
    public void recordsTheEntityACreateReturns() {
        ToolDefinition def = ToolDefinition.tool("create_concept_set")
                .description("test")
                .step(ReasoningStep.STEP_2_CONCEPT_SET_CONSOLIDATION)
                .entity(EntityType.CONCEPT_SET, "concept_set_id")
                .handler(args -> Map.of("concept_set_id", 12))
                .build();
        factory(false).call(def, new CallToolRequest("create_concept_set", Map.of("name", "x"), Map.of()));
        verify(audit).start(any(), eq("create_concept_set"), anyString(), anyString(), isNull(), isNull(), isNull(),
                isNull());
        verify(audit).finish(eq(entry), eq(Outcome.SUCCESS), anyString(), isNull(), eq(EntityType.CONCEPT_SET),
                eq(12));
    }

    // ---- OpenTelemetry ---------------------------------------------------------------------

    private static final class Otel {
        final InMemorySpanExporter exporter = InMemorySpanExporter.create();
        final OpenTelemetrySdk sdk = OpenTelemetrySdk.builder()
                .setTracerProvider(SdkTracerProvider.builder()
                        .addSpanProcessor(SimpleSpanProcessor.create(exporter))
                        .build())
                .build();

        SpanData onlySpan() {
            List<SpanData> spans = exporter.getFinishedSpanItems();
            assertEquals(spans.toString(), 1, spans.size());
            return spans.get(0);
        }
    }

    private TraceContext auditedTrace() {
        ArgumentCaptor<TraceContext> trace = ArgumentCaptor.forClass(TraceContext.class);
        verify(audit).start(trace.capture(), anyString(), anyString(), any(), any(), any(), any(), any());
        return trace.getValue();
    }

    @Test
    public void opensAServerSpanUnderTheMetaTraceparentAndAuditsItsIds() {
        Otel otel = new Otel();
        McpToolFactory factory = new McpToolFactory(audit, new ObjectMapper(), false, 20, otel.sdk);

        CallToolResult result = factory.call(guarded(new AtomicBoolean()), request(
                Map.of("clinical_rationale", "Core diagnosis concept for the phenotype.",
                        "evidence_reference", "SNOMED-CT hierarchy"),
                Map.of("traceparent", TRACEPARENT, "reasoning_step", "STEP_2_CONCEPT_SELECTION")));

        SpanData span = otel.onlySpan();
        assertEquals("tools/call add_concept_to_set", span.getName());
        assertEquals(SpanKind.SERVER, span.getKind());
        assertEquals(TRACE_ID, span.getTraceId());
        assertEquals("00f067aa0ba902b7", span.getParentSpanId());
        assertEquals("tools/call", span.getAttributes().get(AttributeKey.stringKey("mcp.method.name")));
        assertEquals("add_concept_to_set", span.getAttributes().get(AttributeKey.stringKey("gen_ai.tool.name")));
        assertEquals("SUCCESS", span.getAttributes().get(AttributeKey.stringKey("ohdsi.mcp.outcome")));
        assertEquals(Long.valueOf(42), span.getAttributes().get(AttributeKey.longKey("ohdsi.mcp.audit_entry_id")));
        assertEquals("STEP_2_CONCEPT_SELECTION",
                span.getAttributes().get(AttributeKey.stringKey("ohdsi.mcp.reasoning_step")));
        assertNull("the rationale stays out of the tracing backend",
                span.getAttributes().get(AttributeKey.stringKey("clinical_rationale")));

        // The audit row and the returned traceparent carry the exported span's own ids.
        TraceContext audited = auditedTrace();
        assertEquals(TRACE_ID, audited.traceId());
        assertEquals("00f067aa0ba902b7", audited.parentSpanId());
        assertEquals(span.getSpanId(), audited.spanId());
        assertEquals("00-" + TRACE_ID + "-" + span.getSpanId() + "-01", result.meta().get("traceparent"));
    }

    @Test
    public void linksTheHttpRequestSpanWhenTheClientSentItsOwnTrace() {
        Otel otel = new Otel();
        McpToolFactory factory = new McpToolFactory(audit, new ObjectMapper(), false, 20, otel.sdk);
        Span http = otel.sdk.getTracer("test").spanBuilder("POST /mcp").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope ignored = http.makeCurrent()) {
            factory.call(guarded(new AtomicBoolean()), request(
                    Map.of("clinical_rationale", "Core diagnosis concept for the phenotype.",
                            "evidence_reference", "SNOMED-CT hierarchy"),
                    Map.of("traceparent", TRACEPARENT)));
        } finally {
            http.end();
        }

        SpanData tool = otel.exporter.getFinishedSpanItems().stream()
                .filter(s -> s.getName().startsWith("tools/call")).findFirst().orElseThrow();
        assertEquals(TRACE_ID, tool.getTraceId());
        assertEquals(1, tool.getLinks().size());
        assertEquals(http.getSpanContext().getSpanId(), tool.getLinks().get(0).getSpanContext().getSpanId());
    }

    @Test
    public void joinsTheHttpRequestTraceWhenTheClientSentNone() {
        Otel otel = new Otel();
        McpToolFactory factory = new McpToolFactory(audit, new ObjectMapper(), false, 20, otel.sdk);
        Span http = otel.sdk.getTracer("test").spanBuilder("POST /mcp").setSpanKind(SpanKind.SERVER).startSpan();
        try (Scope ignored = http.makeCurrent()) {
            factory.call(guarded(new AtomicBoolean()), request(
                    Map.of("clinical_rationale", "Core diagnosis concept for the phenotype.",
                            "evidence_reference", "SNOMED-CT hierarchy"),
                    Map.of()));
        } finally {
            http.end();
        }

        TraceContext audited = auditedTrace();
        assertEquals(http.getSpanContext().getTraceId(), audited.traceId());
        assertEquals(http.getSpanContext().getSpanId(), audited.parentSpanId());
        assertTrue("no client traceparent", audited.generated());
    }

    @Test
    public void marksARefusedCallAsAToolError() {
        Otel otel = new Otel();
        McpToolFactory factory = new McpToolFactory(audit, new ObjectMapper(), false, 20, otel.sdk);

        factory.call(guarded(new AtomicBoolean()), request(Map.of(), Map.of("traceparent", TRACEPARENT)));

        SpanData span = otel.onlySpan();
        assertEquals("REJECTED", span.getAttributes().get(AttributeKey.stringKey("ohdsi.mcp.outcome")));
        assertEquals("tool_error", span.getAttributes().get(AttributeKey.stringKey("error.type")));
        assertEquals(StatusCode.ERROR, span.getStatus().getStatusCode());
        assertEquals("REJECTED", span.getStatus().getDescription());
    }

    @Test
    public void withoutOpenTelemetryWebApiAllocatesTheIdsItself() {
        factory(false).call(guarded(new AtomicBoolean()), request(
                Map.of("clinical_rationale", "Core diagnosis concept for the phenotype.",
                        "evidence_reference", "SNOMED-CT hierarchy"),
                Map.of("traceparent", TRACEPARENT)));

        TraceContext audited = auditedTrace();
        assertEquals(TRACE_ID, audited.traceId());
        assertEquals("00f067aa0ba902b7", audited.parentSpanId());
        assertEquals(16, audited.spanId().length());
    }
}
