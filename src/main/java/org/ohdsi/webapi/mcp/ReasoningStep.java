package org.ohdsi.webapi.mcp;

/**
 * The phenotype-engineering step a tool belongs to. Recorded on every audit entry; a client may
 * override it per call with {@code _meta.reasoning_step}.
 */
public enum ReasoningStep {
    STEP_1_CONCEPT_EXPLORATION,
    STEP_2_CONCEPT_SET_CONSOLIDATION,
    STEP_3_COHORT_CRITERIA_DEFINITION,
    STEP_4_SQL_VALIDATION,
    STEP_5_GENERATION_VERIFICATION,
    AUDIT
}
