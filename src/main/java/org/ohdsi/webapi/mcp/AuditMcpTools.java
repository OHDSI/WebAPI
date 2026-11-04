package org.ohdsi.webapi.mcp;

import static org.ohdsi.webapi.mcp.ToolDefinition.stringParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import org.ohdsi.webapi.mcp.audit.McpAuditEntry;
import org.ohdsi.webapi.mcp.audit.McpAuditService;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Reads back the audit trail of a trace, as the decision_log of an Audit Manifest. A user sees
 * only the entries they made themselves.
 */
@Component
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public class AuditMcpTools implements McpToolProvider {

    private final McpAuditService audit;
    private final AuthorizationService authorizationService;
    private final ObjectMapper mapper;

    public AuditMcpTools(McpAuditService audit, AuthorizationService authorizationService, ObjectMapper mapper) {
        this.audit = audit;
        this.authorizationService = authorizationService;
        this.mapper = mapper;
    }

    @Override
    public List<ToolDefinition> tools() {
        return List.of(tool("get_audit_trail")
                .title("Get audit trail")
                .description("Returns, in order, every tool call recorded for a trace id by the current user: "
                        + "span ids, reasoning step, arguments, clinical rationale, evidence and outcome. Use it to "
                        + "build the decision_log of the Audit Manifest from what the server actually recorded.")
                .step(ReasoningStep.AUDIT)
                .readOnly()
                .requiredParam("trace_id", stringParam("The 32-hex-digit W3C trace id."))
                .handler(this::trail)
                .build());
    }

    private Map<String, Object> trail(ToolArguments args) {
        String traceId = args.string("trace_id").trim().toLowerCase(Locale.ROOT);
        if (!traceId.matches("[0-9a-f]{32}")) {
            throw new ToolContractException("'trace_id' must be 32 lowercase hex digits");
        }
        WebApiPrincipal me = authorizationService.getAuthenticatedPrincipal();
        List<Map<String, Object>> log = new ArrayList<>();
        for (McpAuditEntry e : audit.findByTrace(traceId)) {
            if (!Objects.equals(e.getUserLogin(), me.getName())) {
                continue;
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("audit_entry_id", e.getId());
            m.put("span_id", e.getSpanId());
            m.put("parent_span_id", e.getParentSpanId());
            m.put("reasoning_step", e.getReasoningStep());
            m.put("tool", e.getToolName());
            m.put("arguments", readTree(e.getArgumentsJson()));
            m.put("clinical_rationale", e.getClinicalRationale());
            m.put("evidence_reference", e.getEvidenceReference());
            m.put("outcome", e.getOutcome().name());
            m.put("result", readTree(e.getResultJson()));
            m.put("error_message", e.getErrorMessage());
            m.put("started_at", e.getStartedAt() == null ? null : e.getStartedAt().toString());
            m.put("finished_at", e.getFinishedAt() == null ? null : e.getFinishedAt().toString());
            log.add(m);
        }
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("trace_id", traceId);
        out.put("entry_count", log.size());
        out.put("decision_log", log);
        return out;
    }

    private Object readTree(String json) {
        if (json == null) {
            return null;
        }
        try {
            return mapper.readTree(json);
        } catch (JsonProcessingException e) {
            // truncated results are no longer valid JSON; return them as text
            return json;
        }
    }
}
