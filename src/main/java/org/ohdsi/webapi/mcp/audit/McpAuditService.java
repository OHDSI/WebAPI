package org.ohdsi.webapi.mcp.audit;

import java.time.Instant;
import java.util.List;
import org.ohdsi.webapi.mcp.TraceContext;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Writes the MCP audit trail. {@link #start} must succeed before a tool runs, so a tool never
 * changes anything that is not already on record.
 */
@Service
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public class McpAuditService {

    /** Results larger than this are cut, so one big SQL rendering cannot bloat the table. */
    static final int MAX_RESULT_CHARS = 200_000;

    private final McpAuditEntryRepository repository;
    private final AuthorizationService authorizationService;

    public McpAuditService(McpAuditEntryRepository repository, AuthorizationService authorizationService) {
        this.repository = repository;
        this.authorizationService = authorizationService;
    }

    public McpAuditEntry start(TraceContext trace, String toolName, String reasoningStep, String argumentsJson,
            String clinicalRationale, String evidenceReference, McpAuditEntry.EntityType entityType,
            Integer entityId) {
        WebApiPrincipal principal = authorizationService.getAuthenticatedPrincipal();
        McpAuditEntry entry = new McpAuditEntry();
        entry.setTraceId(trace.traceId());
        entry.setParentSpanId(trace.parentSpanId());
        entry.setSpanId(trace.spanId());
        entry.setTraceGenerated(trace.generated());
        entry.setReasoningStep(reasoningStep);
        entry.setToolName(toolName);
        entry.setArgumentsJson(argumentsJson);
        entry.setClinicalRationale(clinicalRationale);
        entry.setEvidenceReference(evidenceReference);
        entry.setOutcome(McpAuditEntry.Outcome.STARTED);
        entry.setUserId(principal.isAnonymous() ? null : principal.getUserId());
        entry.setUserLogin(principal.getName());
        entry.setStartedAt(Instant.now());
        if (entityType != null && entityId != null) {
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
        }
        return repository.saveAndFlush(entry);
    }

    /**
     * Completes an entry. {@code entityType}/{@code entityId} are recorded only if the entry has no entity
     * yet, which is the case for a create: the new artifact's id is known only once the tool has run.
     */
    public McpAuditEntry finish(McpAuditEntry entry, McpAuditEntry.Outcome outcome, String resultJson,
            String errorMessage, McpAuditEntry.EntityType entityType, Integer entityId) {
        if (entry.getEntityId() == null && entityType != null && entityId != null) {
            entry.setEntityType(entityType);
            entry.setEntityId(entityId);
        }
        entry.setOutcome(outcome);
        entry.setResultJson(truncate(resultJson));
        entry.setErrorMessage(errorMessage);
        entry.setFinishedAt(Instant.now());
        return repository.saveAndFlush(entry);
    }

    public List<McpAuditEntry> findByTrace(String traceId) {
        return repository.findByTraceIdOrderByStartedAtAscIdAsc(traceId);
    }

    static String truncate(String value) {
        if (value == null || value.length() <= MAX_RESULT_CHARS) {
            return value;
        }
        return value.substring(0, MAX_RESULT_CHARS) + "...[truncated " + (value.length() - MAX_RESULT_CHARS) + " chars]";
    }
}
