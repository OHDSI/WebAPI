package org.ohdsi.webapi.mcp.support;

import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/** Resolves and validates human-friendly source keys used across MCP tools. */
@Component
@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true")
public class McpToolContext {

    private final SourceService sourceService;

    public McpToolContext(SourceService sourceService) {
        this.sourceService = sourceService;
    }

    /** @return the key if a source exists; otherwise throws with the list of valid keys. */
    public String requireSource(String sourceKey) {
        if (sourceKey != null && sourceService.findBySourceKey(sourceKey) != null) {
            return sourceKey;
        }
        throw new IllegalArgumentException(
                "Unknown sourceKey '" + sourceKey + "'. Valid keys: " + validSourceKeys());
    }

    public List<String> validSourceKeys() {
        return sourceService.getSources().stream()
                .map(Source::getSourceKey)
                .collect(Collectors.toList());
    }
}
