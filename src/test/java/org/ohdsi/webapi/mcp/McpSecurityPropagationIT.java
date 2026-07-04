package org.ohdsi.webapi.mcp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.security.authc.WebApiAuthenticationToken;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.User;
import org.ohdsi.webapi.security.identity.WebApiPrincipal;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.test.WebApiIT;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;

/**
 * End-to-end proof that the caller's {@link org.springframework.security.core.context.SecurityContext}
 * reaches a {@code @PreAuthorize}-gated backing service through the MCP tool-call path.
 *
 * <p>This is the linchpin test for the whole embedded-MCP effort: if the security
 * context did not propagate from the caller thread through
 * {@link ToolCallback#call(String)} into the AOP-proxied {@link org.ohdsi.webapi.service.VocabularyService}
 * method, {@code @PreAuthorize} could not fire and every "secured" MCP tool would
 * be a silent hole.
 *
 * <p>It boots the full WebAPI context (embedded Postgres via {@link WebApiIT}) with
 * the MCP {@code ToolCallbackProvider} enabled ({@code mcp.server.enabled=true}),
 * seeds a real CDM source, and invokes the vocabulary tools directly through the
 * registered {@link ToolCallback}s while manipulating the thread's
 * {@code SecurityContextHolder} — exactly the surface a SYNC-mode MCP server uses.
 */
@TestPropertySource(properties = {
    "mcp.server.enabled=true",
    "spring.ai.mcp.server.enabled=true"
})
public class McpSecurityPropagationIT extends WebApiIT {

    /** Non-admin, non-anonymous user with no roles/permissions and no source grant. */
    private static final long   LIMITED_USER_ID = -2L;
    private static final String LIMITED_LOGIN   = "mcp-limited";

    /** Built-in admin role, granted to the anonymous user (id -1) by {@link WebApiIT}. */
    private static final long   ADMIN_ROLE_ID   = 2L;

    @Autowired
    private ApplicationContext ctx;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private AuthorizationService authorizationService;

    private long sourceId;

    @Before
    public void seedSourceAndSchema() throws Exception {
        String schema = getOhdsiSchema();

        // A real CDM source so McpToolContext.requireSource(...) succeeds and the
        // @PreAuthorize gate (not an invalid-input guard) is what the tool hits.
        truncateTable(schema + ".source");
        resetSequence(schema + ".source_sequence");
        Source source = sourceRepository.saveAndFlush(getCdmSource());
        sourceId = source.getId().longValue();

        // Grant the admin role (which the anonymous user carries) READ on the source,
        // so hasSourceAccess(...) passes for the granted-path assertion.
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_source (role_id, source_id, access_type) " +
            "VALUES (" + ADMIN_ROLE_ID + ", " + sourceId + ", 'READ') ON CONFLICT DO NOTHING");

        // Empty-but-present CDM schema so the descendants query runs (returns 0 rows).
        prepareCdmSchema();

        authorizationService.clearCache();
    }

    @After
    public void clearThreadSecurity() {
        SecurityContextHolder.clearContext();
    }

    private ToolCallbackProvider provider() {
        // McpServerConfig registers exactly this bean when mcp.server.enabled=true.
        return ctx.getBean("webApiMcpTools", ToolCallbackProvider.class);
    }

    private ToolCallback tool(String name) {
        return Arrays.stream(provider().getToolCallbacks())
            .filter(t -> t.getToolDefinition().name().equals(name))
            .findFirst()
            .orElseThrow(() -> new AssertionError("No MCP tool named '" + name + "' is registered"));
    }

    private void setPrincipal(long userId, String login) {
        WebApiPrincipal principal = new WebApiPrincipal(new User(userId, login, login));
        Authentication auth = WebApiAuthenticationToken.authenticated(
            principal, UUID.randomUUID(), Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    // ------------------------------------------------------------------------
    // Assertion 1: the vocabulary tools are actually registered with the provider.
    // ------------------------------------------------------------------------
    @Test
    public void vocabularyToolsAreRegistered() {
        List<String> names = Arrays.stream(provider().getToolCallbacks())
            .map(t -> t.getToolDefinition().name())
            .toList();

        assertThat(names).contains(
            "vocabSearchConcepts", "vocabGetConcept", "vocabConceptDescendants");
    }

    @Test
    public void sourceAndJobToolsAreRegistered() {
        List<String> names = Arrays.stream(provider().getToolCallbacks())
            .map(t -> t.getToolDefinition().name())
            .toList();

        assertThat(names).contains("sourceList", "cdmResultsDashboard", "jobStatus");
    }

    // ------------------------------------------------------------------------
    // Assertion 2 (ESSENTIAL): with an authenticated-but-unprivileged principal on
    // the calling thread, a @PreAuthorize-gated tool must come back permission_denied.
    // This proves the SecurityContext propagated through the tool path AND the gate fired.
    // ------------------------------------------------------------------------
    @Test
    public void gatedToolDeniesWithoutPermission() {
        setPrincipal(LIMITED_USER_ID, LIMITED_LOGIN);

        String json = tool("vocabConceptDescendants")
            .call("{\"sourceKey\":\"" + SOURCE_KEY + "\",\"conceptId\":201826}");

        assertThat(json)
            .as("gated tool must deny an unprivileged caller via @PreAuthorize")
            .contains("permission_denied")
            .doesNotContain("invalid_input");
    }

    // ------------------------------------------------------------------------
    // Assertion 3: with a permitted principal (anonymous admin, id -1, holding the
    // admin role + a sec_source READ grant), the same gated tool returns ok.
    // ------------------------------------------------------------------------
    @Test
    public void gatedToolAllowsWithPermission() {
        setPrincipal(WebApiPrincipal.ANONYMOUS_USER_ID, "anonymous");

        String json = tool("vocabConceptDescendants")
            .call("{\"sourceKey\":\"" + SOURCE_KEY + "\",\"conceptId\":201826}");

        assertThat(json)
            .as("gated tool must allow a permitted caller (empty descendant set is fine)")
            .contains("\"status\":\"ok\"")
            .doesNotContain("permission_denied");
    }
}
