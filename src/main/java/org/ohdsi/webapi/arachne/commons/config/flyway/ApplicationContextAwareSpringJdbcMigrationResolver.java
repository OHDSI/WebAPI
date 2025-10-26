package org.ohdsi.webapi.arachne.commons.config.flyway;

import org.flywaydb.core.api.configuration.FlywayConfiguration;
import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Migration resolver that makes ApplicationContext available to Spring JDBC migrations.
 */
public class ApplicationContextAwareSpringJdbcMigrationResolver implements MigrationResolver {

    private final ApplicationContext applicationContext;

    public ApplicationContextAwareSpringJdbcMigrationResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public Collection<ResolvedMigration> resolveMigrations() {
        List<ResolvedMigration> migrations = new ArrayList<>();

        // Find all ApplicationContextAware migrations
        Map<String, ApplicationContextAwareSpringMigration> migrationBeans =
            applicationContext.getBeansOfType(ApplicationContextAwareSpringMigration.class);

        // Set ApplicationContext on each migration
        for (ApplicationContextAwareSpringMigration migration : migrationBeans.values()) {
            migration.setApplicationContext(applicationContext);
        }

        return migrations;
    }
}
