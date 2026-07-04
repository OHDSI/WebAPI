package org.ohdsi.webapi.mcp.support;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.assertj.core.api.Assertions.assertThat;

class McpCallTest {

    @Test
    void wrapsSuccessfulValue() {
        McpResult r = McpCall.guard(() -> "hello");
        assertThat(r.ok()).isTrue();
        assertThat(r.status()).isEqualTo("ok");
        assertThat(r.data()).isEqualTo("hello");
    }

    @Test
    void mapsAccessDeniedToPermissionDenied() {
        McpResult r = McpCall.guard(() -> { throw new AccessDeniedException("no read:cohort"); });
        assertThat(r.ok()).isFalse();
        assertThat(r.status()).isEqualTo("permission_denied");
        assertThat(r.message()).contains("no read:cohort");
    }

    @Test
    void mapsIllegalArgumentToInvalidInput() {
        McpResult r = McpCall.guard(() -> { throw new IllegalArgumentException("bad sourceKey"); });
        assertThat(r.status()).isEqualTo("invalid_input");
        assertThat(r.message()).contains("bad sourceKey");
    }

    @Test
    void mapsUnknownToUpstreamErrorWithoutLeakingDetail() {
        McpResult r = McpCall.guard(() -> { throw new RuntimeException("SQLState 42P01 relation missing"); });
        assertThat(r.status()).isEqualTo("upstream_error");
        assertThat(r.message()).doesNotContain("42P01");
    }
}
