package org.ohdsi.webapi.mcp.audit;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import org.ohdsi.webapi.mcp.audit.McpAuditEntry.EntityType;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Read-only provenance of cohort definitions and concept sets changed through the MCP server: every
 * audited tool call about the artifact, in order, with the agent's stated rationale and the outcome.
 * Anyone who can read the artifact can read its provenance.
 *
 * <p>Unlike the MCP endpoint this controller is not switched off with {@code spring.ai.mcp.server.enabled},
 * so the history stays visible after MCP is disabled.
 */
@RestController
@Transactional(readOnly = true)
public class McpProvenanceController {

    /** One audited tool call, without its arguments and result payloads. */
    public record ProvenanceEntry(
            Long id,
            String traceId,
            String spanId,
            String parentSpanId,
            String reasoningStep,
            String tool,
            String outcome,
            String clinicalRationale,
            String evidenceReference,
            String errorMessage,
            String userLogin,
            Date startedAt,
            Date finishedAt) {

        static ProvenanceEntry of(McpAuditEntry e) {
            return new ProvenanceEntry(e.getId(), e.getTraceId(), e.getSpanId(), e.getParentSpanId(),
                    e.getReasoningStep(), e.getToolName(), e.getOutcome().name(), e.getClinicalRationale(),
                    e.getEvidenceReference(), e.getErrorMessage(), e.getUserLogin(), date(e.getStartedAt()),
                    date(e.getFinishedAt()));
        }

        private static Date date(Instant instant) {
            return instant == null ? null : Date.from(instant);
        }
    }

    private final McpAuditEntryRepository repository;

    public McpProvenanceController(McpAuditEntryRepository repository) {
        this.repository = repository;
    }

    @GetMapping(value = "/cohortdefinition/{id}/mcp-provenance", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, COHORT_DEFINITION) or isAnyPermitted(anyOf('read:cohort-definition','write:cohort-definition')) or hasEntityAccess(#id, COHORT_DEFINITION, READ)")
    public List<ProvenanceEntry> getCohortDefinitionProvenance(@PathVariable("id") final int id) {
        return find(EntityType.COHORT_DEFINITION, id);
    }

    @GetMapping(value = "/conceptset/{id}/mcp-provenance", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isOwner(#id, CONCEPT_SET) or isPermitted('read:conceptset') or isPermitted('write:conceptset') or hasEntityAccess(#id, CONCEPT_SET, READ)")
    public List<ProvenanceEntry> getConceptSetProvenance(@PathVariable("id") final int id) {
        return find(EntityType.CONCEPT_SET, id);
    }

    private List<ProvenanceEntry> find(EntityType type, int id) {
        return repository.findByEntityTypeAndEntityIdOrderByStartedAtAscIdAsc(type, id).stream()
                .map(ProvenanceEntry::of)
                .toList();
    }
}
