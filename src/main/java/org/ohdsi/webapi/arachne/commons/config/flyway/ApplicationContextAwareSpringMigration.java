package org.ohdsi.webapi.arachne.commons.config.flyway;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Base class for Flyway Java migrations that need access to Spring ApplicationContext.
 * Compatible with Flyway 11.7 API.
 *
 * The ApplicationContext is retrieved from FlywayConfig.ApplicationContextHolder if not already set.
 */
public abstract class ApplicationContextAwareSpringMigration extends BaseJavaMigration implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected <T> T getBean(Class<T> requiredType) {
        ensureApplicationContext();
        return applicationContext.getBean(requiredType);
    }

    protected Object getBean(String name) {
        ensureApplicationContext();
        return applicationContext.getBean(name);
    }

    /**
     * Ensure ApplicationContext is available, retrieving from static holder if necessary.
     */
    private void ensureApplicationContext() {
        if (applicationContext == null) {
            try {
                // Get ApplicationContext from FlywayConfig's static holder
                Class<?> holderClass = Class.forName("org.ohdsi.webapi.FlywayConfig$ApplicationContextHolder");
                java.lang.reflect.Method getContextMethod = holderClass.getMethod("getApplicationContext");
                applicationContext = (ApplicationContext) getContextMethod.invoke(null);
            } catch (Exception e) {
                throw new RuntimeException("Failed to get ApplicationContext from FlywayConfig.ApplicationContextHolder", e);
            }
        }
    }

    @Override
    public void migrate(Context context) throws Exception {
        // Ensure ApplicationContext is available before migration
        ensureApplicationContext();
        // Delegate to the simpler migrate() method for backward compatibility
        migrate();
    }

    /**
     * Implement this method to perform the migration.
     * You have access to the Spring ApplicationContext via the applicationContext field
     * or by calling getBean() methods.
     */
    public abstract void migrate() throws Exception;
}
