package org.ohdsi.webapi.mcp;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.mcp.support.McpToolContext;
import org.ohdsi.webapi.mcp.tools.AnalysisTools;
import org.ohdsi.webapi.mcp.tools.CohortTools;
import org.ohdsi.webapi.mcp.tools.ConceptSetTools;
import org.ohdsi.webapi.mcp.tools.SourceJobTools;
import org.ohdsi.webapi.mcp.tools.VocabularyTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.stereotype.Component;

import java.util.List;

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

    /**
     * Every {@code McpToolset} bean (plus {@link McpToolContext}) must be gated by the same
     * {@code mcp.server.enabled} flag as {@link McpServerConfig} itself, otherwise they get
     * instantiated (and pull in their whole dependency graph) even with the MCP server disabled.
     * Verified by reflection here since driving the real tool classes through an isolated
     * {@link ApplicationContextRunner} would require standing up their full production
     * dependency graphs (e.g. {@code VocabularyService}'s several {@code @Autowired} fields),
     * which is unnecessary to prove the conditional is wired correctly.
     */
    @Test
    void toolBeansAreGatedByServerEnabledFlag() {
        List<Class<?>> gatedToolBeans = List.of(
                VocabularyTools.class, SourceJobTools.class, ConceptSetTools.class,
                CohortTools.class, AnalysisTools.class, McpToolContext.class);

        for (Class<?> clazz : gatedToolBeans) {
            ConditionalOnProperty annotation = clazz.getAnnotation(ConditionalOnProperty.class);
            assertThat(annotation)
                    .as("%s should be @ConditionalOnProperty(mcp.server.enabled)", clazz.getSimpleName())
                    .isNotNull();
            assertThat(annotation.name()).containsExactly("mcp.server.enabled");
            assertThat(annotation.havingValue()).isEqualTo("true");
        }
    }

    /**
     * Confirms {@code @ConditionalOnProperty} genuinely gates bean creation (not just that the
     * annotation is present) using a minimal stand-in {@code @Component}, mirroring exactly how
     * the real tool classes are annotated.
     */
    @Test
    void conditionalOnPropertyGatesComponentCreation() {
        ApplicationContextRunner stubRunner =
                new ApplicationContextRunner().withUserConfiguration(GatedStubComponent.class);

        stubRunner.run(ctx -> assertThat(ctx).doesNotHaveBean(GatedStubComponent.class));
        stubRunner.withPropertyValues("mcp.server.enabled=true")
                  .run(ctx -> assertThat(ctx).hasSingleBean(GatedStubComponent.class));
    }

    @Component
    @ConditionalOnProperty(name = "mcp.server.enabled", havingValue = "true")
    static class GatedStubComponent {
    }
}
