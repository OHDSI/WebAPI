package org.ohdsi.webapi.util;

import org.springframework.util.StringUtils;

public class SecurityUtils {

    public static Object whitelist(Object object) {

        return object;
    }
    public static int whitelist(int object) {

        return object;
    }
    public static String whitelist(String object) {

        return object;
    }

    public static String whitelist(Exception object) {

        return object.getMessage();
    }

    public static void sleep(int ms) {

        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public static String cleanWhiteSpace(String resourceAsString) {

        return StringUtils.trimAllWhitespace(resourceAsString);
    }
}
