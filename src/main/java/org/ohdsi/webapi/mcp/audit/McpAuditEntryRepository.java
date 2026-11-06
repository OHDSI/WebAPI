package org.ohdsi.webapi.mcp.audit;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface McpAuditEntryRepository extends JpaRepository<McpAuditEntry, Long> {

    List<McpAuditEntry> findByTraceIdOrderByStartedAtAscIdAsc(String traceId);

    List<McpAuditEntry> findByEntityTypeAndEntityIdOrderByStartedAtAscIdAsc(McpAuditEntry.EntityType entityType,
            Integer entityId);
}
