package org.ohdsi.webapi.test.migration;

import org.junit.Test;
import org.ohdsi.webapi.info.Info;
import org.ohdsi.webapi.test.WebApiIT;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

/**
 * Integration tests for Phase 3: Simple Controllers Migration
 *
 * Verifies that all 6 migrated simple controllers work correctly:
 * 1. InfoMvcController
 * 2. CacheMvcController
 * 3. ActivityMvcController
 * 4. SqlRenderMvcController
 * 5. DDLMvcController
 * 6. I18nMvcController
 *
 * Each test verifies:
 * - Controller is accessible at /WebAPI/v2/* URL
 * - Returns expected HTTP status
 * - Response body is valid (where applicable)
 */
public class MigrationPhase3IT extends WebApiIT {

    private final TestRestTemplate restTemplate = new TestRestTemplate();
    private DualRuntimeTestSupport support;

    @Test
    public void testInfoMvcController() {
        log.info("Testing InfoMvcController...");

        // Initialize support with baseUri from parent class
        support = new DualRuntimeTestSupport(restTemplate, getBaseUri());

        String mvcUrl = support.getMvcUrl("/info/");
        ResponseEntity<Info> response = restTemplate.getForEntity(mvcUrl, Info.class);

        log.info("InfoMvcController status: {}", response.getStatusCode());
        assertEquals("Should return 200 OK", HttpStatus.OK, response.getStatusCode());

        Info info = response.getBody();
        assertNotNull("Info should not be null", info);
        assertNotNull("Version should not be null", info.getVersion());
        log.info("WebAPI version: {}", info.getVersion());
    }

    @Test
    public void testCacheMvcController() {
        log.info("Testing CacheMvcController...");

        support = new DualRuntimeTestSupport(restTemplate, getBaseUri());

        // Test GET /cache/
        String mvcUrl = support.getMvcUrl("/cache/");
        ResponseEntity<String> response = restTemplate.getForEntity(mvcUrl, String.class);

        log.info("CacheMvcController status: {}", response.getStatusCode());
        assertEquals("Should return 200 OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Response body should not be null", response.getBody());
    }

    @Test
    public void testActivityMvcController() {
        log.info("Testing ActivityMvcController...");

        support = new DualRuntimeTestSupport(restTemplate, getBaseUri());

        String mvcUrl = support.getMvcUrl("/activity/latest");
        ResponseEntity<Object[]> response = restTemplate.getForEntity(mvcUrl, Object[].class);

        log.info("ActivityMvcController status: {}", response.getStatusCode());
        assertEquals("Should return 200 OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Activity array should not be null", response.getBody());
    }

    @Test
    public void testSqlRenderMvcController() {
        log.info("Testing SqlRenderMvcController...");

        support = new DualRuntimeTestSupport(restTemplate, getBaseUri());

        // This requires a POST request with JSON body
        // For now, just verify the endpoint exists
        String mvcUrl = support.getMvcUrl("/sqlrender/translate");

        // POST without body should still return a response (not 404)
        ResponseEntity<String> response = restTemplate.postForEntity(mvcUrl, null, String.class);

        log.info("SqlRenderMvcController status: {}", response.getStatusCode());
        // Should get 200 or 400, not 404
        assertTrue("Should not return 404",
                response.getStatusCode() != HttpStatus.NOT_FOUND);
    }

    @Test
    public void testDDLMvcController() {
        log.info("Testing DDLMvcController...");

        support = new DualRuntimeTestSupport(restTemplate, getBaseUri());

        // Test GET /ddl/results
        String mvcUrl = support.getMvcUrl("/ddl/results?dialect=postgresql&schema=results");
        ResponseEntity<String> response = restTemplate.getForEntity(mvcUrl, String.class);

        log.info("DDLMvcController status: {}", response.getStatusCode());
        assertEquals("Should return 200 OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("DDL SQL should not be null", response.getBody());
        assertTrue("DDL should contain SQL", response.getBody().length() > 0);
    }

    @Test
    public void testI18nMvcController() {
        log.info("Testing I18nMvcController...");

        support = new DualRuntimeTestSupport(restTemplate, getBaseUri());

        // Test GET /i18n/
        String mvcUrl = support.getMvcUrl("/i18n/");
        ResponseEntity<String> response = restTemplate.getForEntity(mvcUrl, String.class);

        log.info("I18nMvcController status: {}", response.getStatusCode());
        assertEquals("Should return 200 OK", HttpStatus.OK, response.getStatusCode());
        assertNotNull("i18n resources should not be null", response.getBody());

        // Test GET /i18n/locales
        mvcUrl = support.getMvcUrl("/i18n/locales");
        response = restTemplate.getForEntity(mvcUrl, String.class);

        assertEquals("Should return 200 OK for locales", HttpStatus.OK, response.getStatusCode());
        assertNotNull("Locales should not be null", response.getBody());
    }

    @Test
    public void testAllMigratedControllersAccessible() {
        log.info("Testing all Phase 3 controllers are accessible...");

        support = new DualRuntimeTestSupport(restTemplate, getBaseUri());

        String[] endpoints = {
            "/info/",
            "/cache/",
            "/activity/latest",
            "/ddl/results",
            "/i18n/",
            "/i18n/locales"
        };

        int successCount = 0;
        for (String endpoint : endpoints) {
            if (support.isMvcEndpointAvailable(endpoint)) {
                successCount++;
                log.info("✓ {} is available", endpoint);
            } else {
                log.warn("✗ {} is NOT available", endpoint);
            }
        }

        assertEquals("All 6 endpoints should be accessible", endpoints.length, successCount);
    }
}
