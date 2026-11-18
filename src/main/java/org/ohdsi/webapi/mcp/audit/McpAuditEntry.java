package org.ohdsi.webapi.mcp.audit;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * One MCP tool call. Written as {@link Outcome#STARTED} before the tool runs and completed
 * exactly once afterwards; the database trigger rejects any other change.
 */
@Entity(name = "McpAuditEntry")
@Table(name = "mcp_audit_log")
public class McpAuditEntry {

    public enum Outcome {
        STARTED,
        SUCCESS,
        /** The tool ran and failed. */
        ERROR,
        /** The call broke the tool contract (missing rationale, unverified concept, bad arguments) and did not run. */
        REJECTED,
        /** WebAPI authorization refused the call. */
        DENIED
    }

    /** What kind of WebAPI artifact a call was about; see {@code entity_type} in {@code mcp_audit_log}. */
    public enum EntityType {
        COHORT_DEFINITION,
        CONCEPT_SET
    }

    @Id
    @Column(name = "id")
    @SequenceGenerator(name = "mcp_audit_log_seq", sequenceName = "mcp_audit_log_sequence", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "mcp_audit_log_seq")
    private Long id;

    @Column(name = "trace_id", nullable = false, length = 32, updatable = false)
    private String traceId;

    @Column(name = "parent_span_id", length = 16, updatable = false)
    private String parentSpanId;

    @Column(name = "span_id", nullable = false, length = 16, updatable = false)
    private String spanId;

    @Column(name = "trace_generated", nullable = false, updatable = false)
    private boolean traceGenerated;

    @Column(name = "reasoning_step", length = 64, updatable = false)
    private String reasoningStep;

    @Column(name = "tool_name", nullable = false, length = 128, updatable = false)
    private String toolName;

    @Column(name = "arguments_json", updatable = false)
    private String argumentsJson;

    @Column(name = "clinical_rationale", updatable = false)
    private String clinicalRationale;

    @Column(name = "evidence_reference", updatable = false)
    private String evidenceReference;

    @Enumerated(EnumType.STRING)
    @Column(name = "outcome", nullable = false, length = 16)
    private Outcome outcome;

    @Column(name = "result_json")
    private String resultJson;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "user_id", updatable = false)
    private Long userId;

    @Column(name = "user_login", length = 255, updatable = false)
    private String userLogin;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 32)
    private EntityType entityType;

    @Column(name = "entity_id")
    private Integer entityId;

    @Column(name = "started_at", nullable = false, updatable = false)
    private Instant startedAt;

    @Column(name = "finished_at")
    private Instant finishedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }

    public String getParentSpanId() { return parentSpanId; }
    public void setParentSpanId(String parentSpanId) { this.parentSpanId = parentSpanId; }

    public String getSpanId() { return spanId; }
    public void setSpanId(String spanId) { this.spanId = spanId; }

    public boolean isTraceGenerated() { return traceGenerated; }
    public void setTraceGenerated(boolean traceGenerated) { this.traceGenerated = traceGenerated; }

    public String getReasoningStep() { return reasoningStep; }
    public void setReasoningStep(String reasoningStep) { this.reasoningStep = reasoningStep; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public String getArgumentsJson() { return argumentsJson; }
    public void setArgumentsJson(String argumentsJson) { this.argumentsJson = argumentsJson; }

    public String getClinicalRationale() { return clinicalRationale; }
    public void setClinicalRationale(String clinicalRationale) { this.clinicalRationale = clinicalRationale; }

    public String getEvidenceReference() { return evidenceReference; }
    public void setEvidenceReference(String evidenceReference) { this.evidenceReference = evidenceReference; }

    public Outcome getOutcome() { return outcome; }
    public void setOutcome(Outcome outcome) { this.outcome = outcome; }

    public String getResultJson() { return resultJson; }
    public void setResultJson(String resultJson) { this.resultJson = resultJson; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserLogin() { return userLogin; }
    public void setUserLogin(String userLogin) { this.userLogin = userLogin; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getFinishedAt() { return finishedAt; }
    public void setFinishedAt(Instant finishedAt) { this.finishedAt = finishedAt; }

    public EntityType getEntityType() { return entityType; }
    public void setEntityType(EntityType entityType) { this.entityType = entityType; }

    public Integer getEntityId() { return entityId; }
    public void setEntityId(Integer entityId) { this.entityId = entityId; }
}
