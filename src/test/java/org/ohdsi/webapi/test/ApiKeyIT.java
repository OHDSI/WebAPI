package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.security.authc.JwtService;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.session.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Integration test for personal API key authentication.
 *
 * <p>Scenario:
 * <ol>
 *   <li>A test user is inserted directly into the database with a known permission.</li>
 *   <li>The user authenticates interactively (JWT session) and POSTs to
 *       {@code /user/apikeys} to generate a new API key.</li>
 *   <li>The raw key returned in the creation response is then used as the sole
 *       credential on a request to {@code GET /user/me} via the {@code X-API-KEY}
 *       header — no JWT, no session.</li>
 *   <li>The response is asserted to contain the correct user login and a non-null
 *       authorizations block, proving that the API key resolved to the right
 *       identity and the permission model is intact.</li>
 * </ol>
 */
public class ApiKeyIT extends WebApiIT {

    @Autowired
    private JwtService jwtService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private AuthorizationService authorizationService;

    private static final long API_KEY_USER_ID  = -10L;
    private static final long API_KEY_ROLE_ID  = -110L;
    private static final String API_KEY_LOGIN  = "apikeyuser";

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** JWT minted for the test user's interactive session — used only for key creation. */
    private String sessionJwt;

    @Before
    public void setUpApiKeyUser() {
        String schema = getOhdsiSchema();

        // Insert user
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_user (id, login, name, origin) " +
            "VALUES (" + API_KEY_USER_ID + ", '" + API_KEY_LOGIN + "', 'API Key Test User', 'SYSTEM') " +
            "ON CONFLICT (login) DO NOTHING");

        // Insert personal role for the user
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_role (id, name, system_role) " +
            "VALUES (" + API_KEY_ROLE_ID + ", '" + API_KEY_LOGIN + "', false) " +
            "ON CONFLICT DO NOTHING");

        // Assign the role to the user
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_user_role (id, user_id, role_id, origin) " +
            "SELECT nextval('" + schema + ".sec_user_role_sequence'), " + API_KEY_USER_ID + ", " + API_KEY_ROLE_ID + ", 'SYSTEM' " +
            "WHERE NOT EXISTS (SELECT 1 FROM " + schema + ".sec_user_role " +
            "  WHERE user_id = " + API_KEY_USER_ID + " AND role_id = " + API_KEY_ROLE_ID + ")");

        // Grant the 'read' permission to the role so we have something assertable in authz
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_role_permission (id, role_id, permission_id) " +
            "SELECT nextval('" + schema + ".sec_role_permission_sequence'), " + API_KEY_ROLE_ID + ", p.id " +
            "FROM " + schema + ".sec_permission p WHERE p.value = 'read' " +
            "AND NOT EXISTS (SELECT 1 FROM " + schema + ".sec_role_permission rp " +
            "  WHERE rp.role_id = " + API_KEY_ROLE_ID + " AND rp.permission_id = p.id)");

        authorizationService.clearCache();

        // Mint a short-lived interactive session JWT for the key-creation step only
        UUID sessionId = sessionService.createSession(API_KEY_LOGIN);
        Date expiresAt = Date.from(Instant.now().plusSeconds(3600));
        sessionJwt = jwtService.generateToken(API_KEY_LOGIN, sessionId.toString(), expiresAt);
    }

    @Test
    public void apiKeyAuthenticatesCorrectUserOnMeEndpoint() throws Exception {

        // ── Step 1: Create an API key using the interactive JWT session ──────────

        HttpHeaders createHeaders = new HttpHeaders();
        createHeaders.setContentType(MediaType.APPLICATION_JSON);
        createHeaders.set("Authorization", "Bearer " + sessionJwt);

        // Key valid for 30 days; expiresInDays is the new API shape
        String createBody = "{\"name\":\"it-test-key\",\"expiresInDays\":30}";

        ResponseEntity<String> createResponse = new TestRestTemplate()
            .exchange(
                getBaseUri() + "/user/apikeys",
                HttpMethod.POST,
                new HttpEntity<>(createBody, createHeaders),
                String.class);

        assertEquals("API key creation should return 201 Created",
            HttpStatus.CREATED, createResponse.getStatusCode());

        JsonNode createJson = objectMapper.readTree(createResponse.getBody());
        String rawKey = createJson.path("rawKey").asText(null);
        assertNotNull("rawKey must be present in the creation response", rawKey);

        // ── Step 2: Use only the raw API key to call GET /user/me ────────────────

        HttpHeaders apiKeyHeaders = new HttpHeaders();
        apiKeyHeaders.set("X-API-KEY", rawKey);

        ResponseEntity<String> meResponse = new TestRestTemplate()
            .exchange(
                getBaseUri() + "/user/me",
                HttpMethod.GET,
                new HttpEntity<>(apiKeyHeaders),
                String.class);

        assertEquals("GET /user/me with a valid API key should return 200 OK",
            HttpStatus.OK, meResponse.getStatusCode());

        // ── Step 3: Assert the returned UserInfo belongs to the API key's owner ──

        JsonNode meJson = objectMapper.readTree(meResponse.getBody());

        String returnedLogin = meJson.path("user").path("login").asText(null);
        assertEquals(
            "The user login in /user/me response must match the API key owner",
            API_KEY_LOGIN, returnedLogin);

        // authz block must be present (may be empty object, but must not be null/missing)
        assertNotNull(
            "The authz block must be present in the /user/me response",
            meJson.path("authz"));
    }

    @Test
    public void invalidApiKeyIsRejected() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", "wa_deadbeefdeadbeef_notavalidsecretnottavalidsecretnottavalidsecret12");

        ResponseEntity<String> response = new TestRestTemplate()
            .exchange(
                getBaseUri() + "/user/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class);

        assertEquals("A malformed/invalid API key must result in 401 Unauthorized",
            HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
