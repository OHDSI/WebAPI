package org.ohdsi.webapi.mcp;

import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class McpServerConfigTest {

    private final ApplicationContextRunner runner =
            new ApplicationContextRunner().withUserConfiguration(McpServerConfig.class);

    @Test
    void toolProviderIsAbsentWhenDisabled() {
        runner.run(ctx -> assertThat(ctx).doesNotHaveBean(ToolCallbackProvider.class));
    }

    @Test
    void toolProviderIsPresentWhenEnabled() {
        runner.withPropertyValues("mcp.server.enabled=true")
              .run(ctx -> assertThat(ctx).hasSingleBean(ToolCallbackProvider.class));
    }
}
