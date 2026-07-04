package org.ohdsi.webapi.mcp.support;

import org.junit.jupiter.api.Test;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class McpToolContextTest {

    private SourceService sourceService = mock(SourceService.class);
    private McpToolContext context = new McpToolContext(sourceService);

    @Test
    void requireSourceReturnsKeyWhenValid() {
        Source s = new Source();
        s.setSourceKey("DEMO_CDM");
        when(sourceService.findBySourceKey("DEMO_CDM")).thenReturn(s);
        assertThat(context.requireSource("DEMO_CDM")).isEqualTo("DEMO_CDM");
    }

    @Test
    void requireSourceThrowsListingValidKeysWhenUnknown() {
        Source s = new Source();
        s.setSourceKey("DEMO_CDM");
        when(sourceService.findBySourceKey("NOPE")).thenReturn(null);
        when(sourceService.getSources()).thenReturn(List.of(s));
        assertThatThrownBy(() -> context.requireSource("NOPE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("DEMO_CDM");
    }
}
