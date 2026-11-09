package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;
import java.util.Date;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.security.apikey.ApiKeyService;
import org.ohdsi.webapi.security.authc.JwtService;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.session.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class AdminApiKeyIT extends WebApiIT {

    @Autowired
    private ApiKeyService apiKeyService;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private AuthorizationService authorizationService;

    private static final long OWNER_USER_ID = -20L;
    private static final long OWNER_ROLE_ID = -120L;
    private static final String OWNER_LOGIN = "apikeyowner";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Before
    public void setUpKeyOwner() {
        String schema = getOhdsiSchema();
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_user (id, login, name, origin) " +
            "VALUES (" + OWNER_USER_ID + ", '" + OWNER_LOGIN + "', 'API Key Owner', 'SYSTEM') " +
            "ON CONFLICT (login) DO NOTHING");
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_role (id, name, system_role) " +
            "VALUES (" + OWNER_ROLE_ID + ", '" + OWNER_LOGIN + "', false) ON CONFLICT DO NOTHING");
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_user_role (id, user_id, role_id, origin) " +
            "SELECT nextval('" + schema + ".sec_user_role_sequence'), " + OWNER_USER_ID + ", " + OWNER_ROLE_ID + ", 'SYSTEM' " +
            "WHERE NOT EXISTS (SELECT 1 FROM " + schema + ".sec_user_role " +
            "  WHERE user_id = " + OWNER_USER_ID + " AND role_id = " + OWNER_ROLE_ID + ")");
        jdbcTemplate.execute(
            "INSERT INTO " + schema + ".sec_role_permission (id, role_id, permission_id) " +
            "SELECT nextval('" + schema + ".sec_role_permission_sequence'), " + OWNER_ROLE_ID + ", p.id " +
            "FROM " + schema + ".sec_permission p WHERE p.value = 'read' " +
            "AND NOT EXISTS (SELECT 1 FROM " + schema + ".sec_role_permission rp " +
            "  WHERE rp.role_id = " + OWNER_ROLE_ID + " AND rp.permission_id = p.id)");
        authorizationService.clearCache();
    }

    @Test
    public void permissionIsSeeded() {
        Integer count = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM " + getOhdsiSchema() + ".sec_permission WHERE value = 'admin:apikeys'", Integer.class);
        assertEquals(Integer.valueOf(1), count);
        Integer granted = jdbcTemplate.queryForObject(
            "SELECT count(*) FROM " + getOhdsiSchema() + ".sec_role_permission rp " +
            "JOIN " + getOhdsiSchema() + ".sec_role r ON r.id = rp.role_id " +
            "JOIN " + getOhdsiSchema() + ".sec_permission p ON p.id = rp.permission_id " +
            "WHERE r.name = 'admin' AND p.value = 'admin:apikeys'", Integer.class);
        assertEquals(Integer.valueOf(1), granted);
    }

    @Test
    public void adminListsOtherUsersKeysWithoutSecrets() throws Exception {
        ApiKeyService.ApiKeyResult key = apiKeyService.generate(OWNER_LOGIN, "listed-key", null, null);

        ResponseEntity<String> response = getRestTemplate().getForEntity(getBaseUri() + "/admin/apikeys", String.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        JsonNode listed = findKey(objectMapper.readTree(response.getBody()), key.keyIdentifier());
        assertEquals(OWNER_LOGIN, listed.path("ownerLogin").asText());
        assertEquals("API Key Owner", listed.path("ownerName").asText());
        assertFalse(listed.has("rawKey"));
        assertFalse(listed.has("keyHash"));
        assertFalse(response.getBody().contains(key.rawKey().substring(key.rawKey().lastIndexOf('_') + 1)));
    }

    @Test
    public void adminRevocationStopsTheKeyFromAuthenticating() throws Exception {
        ApiKeyService.ApiKeyResult key = apiKeyService.generate(OWNER_LOGIN, "revoked-key", null, null);
        assertEquals(HttpStatus.OK, callMeWithApiKey(key.rawKey()).getStatusCode());

        ResponseEntity<Void> revoke = getRestTemplate().exchange(
            getBaseUri() + "/admin/apikeys/" + key.keyIdentifier(), HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, revoke.getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, callMeWithApiKey(key.rawKey()).getStatusCode());
        JsonNode listed = findKey(listAsAdmin(), key.keyIdentifier());
        assertTrue(listed.path("disabled").asBoolean());
    }

    @Test
    public void adminCanPermanentlyDeleteAKey() throws Exception {
        ApiKeyService.ApiKeyResult key = apiKeyService.generate(OWNER_LOGIN, "deleted-key", null, null);

        ResponseEntity<Void> delete = getRestTemplate().exchange(
            getBaseUri() + "/admin/apikeys/" + key.keyIdentifier() + "?remove=true", HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, delete.getStatusCode());
        for (JsonNode listed : listAsAdmin()) {
            assertFalse(key.keyIdentifier().equals(listed.path("keyIdentifier").asText()));
        }
    }

    @Test
    public void unknownKeyIsNotFound() {
        ResponseEntity<Void> response = getRestTemplate().exchange(
            getBaseUri() + "/admin/apikeys/0000000000000000", HttpMethod.DELETE, null, Void.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void usersWithoutThePermissionAreForbidden() {
        ApiKeyService.ApiKeyResult key = apiKeyService.generate(OWNER_LOGIN, "not-yours", null, null);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + jwtFor(OWNER_LOGIN));

        ResponseEntity<String> list = new TestRestTemplate().exchange(
            getBaseUri() + "/admin/apikeys", HttpMethod.GET, new HttpEntity<>(headers), String.class);
        ResponseEntity<String> revoke = new TestRestTemplate().exchange(
            getBaseUri() + "/admin/apikeys/" + key.keyIdentifier(), HttpMethod.DELETE, new HttpEntity<>(headers), String.class);

        assertEquals(HttpStatus.FORBIDDEN, list.getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, revoke.getStatusCode());
    }

    private JsonNode listAsAdmin() throws Exception {
        ResponseEntity<String> response = getRestTemplate().getForEntity(getBaseUri() + "/admin/apikeys", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return objectMapper.readTree(response.getBody());
    }

    private static JsonNode findKey(JsonNode keys, String keyIdentifier) {
        for (JsonNode key : keys) {
            if (keyIdentifier.equals(key.path("keyIdentifier").asText())) {
                return key;
            }
        }
        throw new AssertionError("Key " + keyIdentifier + " not listed");
    }

    private ResponseEntity<String> callMeWithApiKey(String rawKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-API-KEY", rawKey);
        return new TestRestTemplate().exchange(
            getBaseUri() + "/user/me", HttpMethod.GET, new HttpEntity<>(headers), String.class);
    }

    private String jwtFor(String login) {
        UUID sessionId = sessionService.createSession(login);
        return jwtService.generateToken(login, sessionId.toString(), Date.from(Instant.now().plusSeconds(3600)));
    }
}
