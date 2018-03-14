package org.ohdsi.webapi.executionengine.util;

import java.util.UUID;

public class StringGenerationUtil {

    public static String generateFileName(String extension) {

        return generateRandomString() + "." + extension;
    }

    public static String generateRandomString() {

        return UUID.randomUUID().toString().replace("-", "");
    }
}
