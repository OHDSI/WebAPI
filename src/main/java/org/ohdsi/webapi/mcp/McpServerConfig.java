package org.ohdsi.webapi.mcp;

import io.modelcontextprotocol.server.McpStatelessServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpStatelessSyncServer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.boot.LazyInitializationExcludeFilter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Publishes WebAPI's tools on the MCP endpoint (Spring AI MCP server, stateless Streamable HTTP,
 * {@code spring.ai.mcp.server.streamable-http.mcp-endpoint}, default {@code /mcp}).
 *
 * <p>The endpoint sits behind the same security filter chain as the REST API, so clients
 * authenticate with a WebAPI JWT ({@code Authorization: Bearer}) or API key ({@code X-API-KEY}),
 * and each tool runs with that user's permissions.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public class McpServerConfig {

    @Bean
    public List<SyncToolSpecification> webApiMcpTools(List<McpToolProvider> providers, McpToolFactory factory) {
        Set<String> names = new HashSet<>();
        return providers.stream()
                .flatMap(p -> p.tools().stream())
                .peek(def -> {
                    if (!names.add(def.name())) {
                        throw new IllegalStateException("Duplicate MCP tool name: " + def.name());
                    }
                })
                .map(factory::build)
                .toList();
    }

    /**
     * WebAPI runs with {@code spring.main.lazy-initialization=true}. The MCP server bean is never
     * injected anywhere, and it is the bean that attaches the request handler to the transport, so
     * left lazy it would never be created and every call to /mcp would fail.
     */
    @Bean
    public static LazyInitializationExcludeFilter mcpServerEagerInit() {
        return LazyInitializationExcludeFilter.forBeanTypes(McpStatelessSyncServer.class);
    }
}
