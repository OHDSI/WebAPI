package org.ohdsi.webapi.arachne.commons.config.flyway;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Base class for Flyway Java migrations that need access to Spring ApplicationContext.
 * Compatible with Flyway 11.7 API.
 */
public abstract class ApplicationContextAwareSpringMigration extends BaseJavaMigration implements ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    protected Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    @Override
    public void migrate(Context context) throws Exception {
        // Delegate to the simpler migrate() method for backward compatibility
        migrate();
    }

    /**
     * Implement this method to perform the migration.
     * You have access to the Spring ApplicationContext via the applicationContext field.
     */
    public abstract void migrate() throws Exception;
}
