package org.ohdsi.webapi.util;

import org.ohdsi.sql.SqlTranslate;

/**
 *
 */
public final class SessionUtils {
    
    public static final String sessionId() {
        return SqlTranslate.generateSessionId();
    }
}
