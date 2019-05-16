package com.jnj.honeur.webapi;

import com.jnj.honeur.webapi.source.SourceDaimonContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Holds the Source Daimon Context key for the running threads
 *
 * @author Peter Moorthamer
 * Date: 02/feb/2018
 */
public class SourceDaimonContextHolder {

    private static Log LOG = LogFactory.getLog(SourceDaimonContextHolder.class);

    private static ThreadLocal<String> contextHolder = new ThreadLocal<>();

    public static void setCurrentSourceDaimonContext(SourceDaimonContext sourceDaimonContext) {
        setCurrentSourceDaimonContextKey(sourceDaimonContext.getSourceDaimonContextKey());
    }

    public static void setCurrentSourceDaimonContextKey(String sourceDaimonContextKey) {
        LOG.debug("Setting context to " + sourceDaimonContextKey);
        contextHolder.set(sourceDaimonContextKey);
    }
    public static String getCurrentSourceDaimonContextKey() {
        return contextHolder.get();
    }

    public static void clear() {
        contextHolder.set(null);
    }

}
