package org.ohdsi.webapi.mcp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        return new McpToolFactory(audit, new ObjectMapper(), requireTraceparent, 20);
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
}
