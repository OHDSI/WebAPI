package org.ohdsi.webapi.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for generating and comparing HTTP ETags.
 */
public final class EtagUtil {

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private EtagUtil() {
        // Utility class
    }

    /**
     * Generates a quoted ETag from the given content bytes using SHA-256.
     *
     * @param content the response body bytes
     * @return a quoted ETag string (e.g., {@code "a1b2c3d4..."})
     */
    public static String generateEtag(byte[] content) {
        if (content == null || content.length == 0) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            return "\"" + bytesToHex(hash) + "\"";
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Checks if the {@code If-None-Match} header value matches the given ETag.
     * <p>
     * Supports multiple comma-separated ETags and the wildcard {@code *}.
     * </p>
     *
     * @param ifNoneMatch the value of the If-None-Match header (may be null)
     * @param etag        the generated ETag to compare against
     * @return true if the ETag matches and a 304 response should be returned
     */
    public static boolean matches(String ifNoneMatch, String etag) {
        if (ifNoneMatch == null || ifNoneMatch.isBlank() || etag == null) {
            return false;
        }

        String trimmed = ifNoneMatch.trim();

        // Wildcard matches any ETag
        if ("*".equals(trimmed)) {
            return true;
        }

        // Handle multiple ETags: "etag1", "etag2", "etag3"
        for (String candidate : trimmed.split(",")) {
            String normalized = candidate.trim();
            // Handle weak ETags (W/"...") by stripping the prefix for comparison
            if (normalized.startsWith("W/")) {
                normalized = normalized.substring(2);
            }
            if (normalized.equals(etag)) {
                return true;
            }
        }

        return false;
    }

    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_CHARS[v >>> 4];
            hexChars[i * 2 + 1] = HEX_CHARS[v & 0x0F];
        }
        return new String(hexChars);
    }
}
