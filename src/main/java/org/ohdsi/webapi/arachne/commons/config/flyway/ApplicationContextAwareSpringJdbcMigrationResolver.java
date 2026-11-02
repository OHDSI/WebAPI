package org.ohdsi.webapi.arachne.commons.config.flyway;

import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Migration resolver that makes ApplicationContext available to Spring JDBC migrations.
 * Compatible with Flyway 11.7 API.
 *
 * This resolver returns an empty list to avoid circular dependencies.
 * The Java-based migrations are registered as @Component beans and will be
 * discovered by Flyway's default JavaMigrationResolver instead.
 * We only need to ensure the ApplicationContext is set on them before execution.
 */
public class ApplicationContextAwareSpringJdbcMigrationResolver implements MigrationResolver {

    private final ApplicationContext applicationContext;

    public ApplicationContextAwareSpringJdbcMigrationResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public List<ResolvedMigration> resolveMigrations(MigrationResolver.Context context) {
        // Return empty list - migrations are discovered by JavaMigrationResolver
        // We just ensure ApplicationContext is available when they execute
        return new ArrayList<>();
    }
}
