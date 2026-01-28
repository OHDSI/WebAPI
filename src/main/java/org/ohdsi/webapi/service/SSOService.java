package org.ohdsi.webapi.service;

import com.google.common.net.HttpHeaders;
import org.apache.commons.io.IOUtils;
import org.pac4j.core.context.HttpConstants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * SSO Service providing SAML metadata and logout functionality
 */
@RestController
@RequestMapping("/saml")
public class SSOService {

    @Value("${security.auth.saml.metadataLocation}")
    private String metadataLocation;

    @Value("${security.auth.saml.sloUrl}")
    private String sloUri;

    @Value("${security.origin}")
    private String origin;

    /**
     * Get the SAML metadata
     *
     * @summary Get metadata
     * @param response The response context
     * @throws IOException
     */
    @GetMapping(value = "/saml-metadata")
    public void samlMetadata(HttpServletResponse response) throws IOException {
        ClassPathResource resource = new ClassPathResource(metadataLocation);
        final InputStream is = resource.getInputStream();
        response.setContentType(MediaType.APPLICATION_XML_VALUE);
        response.setHeader(HttpHeaders.CONTENT_TYPE, "application/xml");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        response.setHeader(HttpHeaders.PRAGMA, "no-cache");
        response.setHeader(HttpHeaders.EXPIRES, "0");
        IOUtils.copy(is, response.getOutputStream());
        response.flushBuffer();
    }

    /**
     * Log out of the service
     *
     * @summary Log out
     * @return Response
     * @throws URISyntaxException
     */
    @GetMapping(value = "/slo")
    public ResponseEntity<Void> logout() throws URISyntaxException {
        return ResponseEntity.status(HttpConstants.TEMPORARY_REDIRECT)
                .header(HttpConstants.ACCESS_CONTROL_ALLOW_ORIGIN_HEADER, this.origin)
                .location(new URI(sloUri))
                .build();
    }
}
