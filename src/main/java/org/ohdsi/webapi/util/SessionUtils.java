package org.ohdsi.webapi.util;

import org.apache.commons.lang3.RandomStringUtils;


/**
 *
 */
public final class SessionUtils {
    public static final String sessionId(){
        return RandomStringUtils.randomAlphanumeric(10);
    }
}
