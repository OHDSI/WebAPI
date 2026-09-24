package org.ohdsi.webapi.util;


import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SpringResourceHelper {

    /**
     * Load a classpath resource and return its contents as a UTF-8 string.
     *
     * @param resourcePath path to the resource, e.g. "/resources/security/getPermissionsForUser.sql"
     * @return resource content as UTF-8 string
     * @throws RuntimeException if the resource cannot be found or read
     */
    public static String getResourceAsString(String resourcePath) {
        try (InputStream inputStream = new ClassPathResource(resourcePath).getInputStream()) {
            return StreamUtils.copyToString(inputStream, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Resource not found or cannot be read: " + resourcePath, e);
        }
    }
}
