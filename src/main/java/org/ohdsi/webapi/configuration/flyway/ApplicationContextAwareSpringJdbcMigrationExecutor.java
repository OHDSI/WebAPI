package org.ohdsi.webapi.configuration.flyway;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.resolver.MigrationExecutor;
import org.springframework.context.ApplicationContext;

import java.sql.Connection;

public class ApplicationContextAwareSpringJdbcMigrationExecutor implements MigrationExecutor {
    private final ApplicationContext applicationContext;
    private final String className;

    public ApplicationContextAwareSpringJdbcMigrationExecutor(ApplicationContext applicationContext, String className) {
        this.applicationContext = applicationContext;
        this.className = className;
    }

    public void execute(Connection connection) {
        try {
            ApplicationContextAwareSpringMigration springJdbcMigration = (ApplicationContextAwareSpringMigration)this.applicationContext.getBean(this.className);
            springJdbcMigration.migrate();
        } catch (Exception e) {
            throw new FlywayException("Migration failed !", e);
        }
    }

    public boolean executeInTransaction() {
        return true;
    }
}
