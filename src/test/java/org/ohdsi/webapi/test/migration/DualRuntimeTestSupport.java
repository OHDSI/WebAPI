package org.ohdsi.webapi.test.migration;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.Assert.*;

/**
 * Test support utilities for Spring MVC endpoints.
 * Jersey has been removed - Spring MVC now serves all /WebAPI/* endpoints.
 */
public class DualRuntimeTestSupport {

    private final TestRestTemplate restTemplate;
    private final String baseUri;

    public DualRuntimeTestSupport(TestRestTemplate restTemplate, String baseUri) {
        this.restTemplate = restTemplate;
        this.baseUri = baseUri;
    }

    /**
     * Test endpoint (deprecated - kept for backward compatibility)
     * @deprecated Use verifyEndpoint instead
     */
    @Deprecated
    public <T> void assertEndpointParity(String endpoint, Class<T> responseType) {
        verifyEndpoint(endpoint, 200, responseType);
    }

    /**
     * Test endpoint with request body (deprecated - kept for backward compatibility)
     * @deprecated Use verifyEndpoint with custom assertions instead
     */
    @Deprecated
    public <T, R> void assertEndpointParity(String endpoint, R requestBody, HttpMethod method, Class<T> responseType) {
        String url = baseUri + endpoint;
        HttpEntity<R> request = requestBody != null ? new HttpEntity<>(requestBody) : null;
        ResponseEntity<T> response = restTemplate.exchange(url, method, request, responseType);
        assertNotNull("Response should not be null for endpoint: " + endpoint, response);
    }

    /**
     * Verify an endpoint exists and returns expected status
     */
    public <T> ResponseEntity<T> verifyEndpoint(String endpoint, int expectedStatus, Class<T> responseType) {
        String url = baseUri + endpoint;
        ResponseEntity<T> response = restTemplate.getForEntity(url, responseType);

        assertEquals("Expected status code " + expectedStatus + " for endpoint: " + endpoint,
            expectedStatus, response.getStatusCodeValue());

        return response;
    }

    /**
     * Check if endpoint is available (returns non-404)
     */
    public boolean isMvcEndpointAvailable(String endpoint) {
        String url = baseUri + endpoint;
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCodeValue() != 404;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get the base URI for constructing test URLs
     */
    public String getBaseUri() {
        return baseUri;
    }

    /**
     * Get Jersey endpoint URL (deprecated - Jersey removed)
     * @deprecated Use getMvcUrl instead
     */
    @Deprecated
    public String getJerseyUrl(String endpoint) {
        return baseUri + endpoint;
    }

    /**
     * Get Spring MVC endpoint URL
     * baseUri already includes context path (/WebAPI), so result is /WebAPI/endpoint
     */
    public String getMvcUrl(String endpoint) {
        return baseUri + endpoint;
    }
}
