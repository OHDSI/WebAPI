package org.ohdsi.webapi.mcp;

import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Registers every {@link McpToolset} bean's {@code @Tool} methods with the MCP server.
 * Active only when {@code mcp.server.enabled=true}.
 */
@Configuration
@ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true")
public class McpServerConfig {

    @Bean
    public ToolCallbackProvider webApiMcpTools(List<McpToolset> toolsets) {
        return MethodToolCallbackProvider.builder()
                .toolObjects(toolsets.toArray())
                .build();
    }
}
