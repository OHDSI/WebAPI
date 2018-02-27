package org.ohdsi.webapi.util;

import java.util.UUID;

public final class GUIDUtil {

    private GUIDUtil() {

    }

    public static String newGuid() {

        return UUID.randomUUID().toString();
    }
}
