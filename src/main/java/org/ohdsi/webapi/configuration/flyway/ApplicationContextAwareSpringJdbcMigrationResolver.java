package org.ohdsi.webapi.configuration.flyway;

import org.flywaydb.core.api.FlywayException;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.migration.MigrationChecksumProvider;
import org.flywaydb.core.api.migration.MigrationInfoProvider;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resolver.ResolvedMigrationComparator;
import org.flywaydb.core.internal.resolver.ResolvedMigrationImpl;
import org.flywaydb.core.internal.util.ClassUtils;
import org.flywaydb.core.internal.util.Pair;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class ApplicationContextAwareSpringJdbcMigrationResolver implements MigrationResolver {
    private final ApplicationContext applicationContext;

    public ApplicationContextAwareSpringJdbcMigrationResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Collection<ResolvedMigration> resolveMigrations() {
        String[] springJdbcMigrationBeanNames = this.applicationContext.getBeanNamesForType(ApplicationContextAwareSpringMigration.class);
        ArrayList<ResolvedMigration> resolvedMigrations = new ArrayList();
        String[] beanNames = springJdbcMigrationBeanNames;

        for(int i = 0; i < springJdbcMigrationBeanNames.length; ++i) {
            String springJdbcMigrationBeanName = beanNames[i];
            BeanDefinition beanDefinition = ((BeanDefinitionRegistry)this.applicationContext).getBeanDefinition(springJdbcMigrationBeanName);
            ResolvedMigrationImpl resolvedMigration = this.extractMigrationInfo(beanDefinition);
            resolvedMigration.setExecutor(new ApplicationContextAwareSpringJdbcMigrationExecutor(this.applicationContext, springJdbcMigrationBeanName));
            resolvedMigrations.add(resolvedMigration);
        }

        resolvedMigrations.sort(new ResolvedMigrationComparator());
        return resolvedMigrations;
    }

    ResolvedMigrationImpl extractMigrationInfo(BeanDefinition springJdbcMigration) {
        Integer checksum = null;
        if (springJdbcMigration instanceof MigrationChecksumProvider) {
            MigrationChecksumProvider checksumProvider = (MigrationChecksumProvider)springJdbcMigration;
            checksum = checksumProvider.getChecksum();
        }

        String description;
        MigrationVersion version;
        if (springJdbcMigration instanceof MigrationInfoProvider) {
            MigrationInfoProvider infoProvider = (MigrationInfoProvider)springJdbcMigration;
            version = infoProvider.getVersion();
            description = infoProvider.getDescription();
            if (!StringUtils.hasText(description)) {
                throw new FlywayException("Missing description for migration " + version);
            }
        } else {
            Class<?> beanClass;
            try {
                beanClass = Class.forName(springJdbcMigration.getBeanClassName());
            } catch (ClassNotFoundException e) {
                throw new FlywayException("Cannot find bean by class name : " + springJdbcMigration.getBeanClassName());
            }

            String shortName = ClassUtils.getShortName(beanClass);
            boolean repeatable = shortName.startsWith("R");
            if (!shortName.startsWith("V") && !repeatable) {
                throw new FlywayException("Invalid Spring migration class name: " + springJdbcMigration.getClass().getName() + " => ensure it starts with V or R, or implement org.flywaydb.core.api.migration.MigrationInfoProvider for non-default naming");
            }

            String prefix = shortName.substring(0, 1);
            Pair<MigrationVersion, String> info = MigrationInfoHelper.extractVersionAndDescription(shortName, prefix, "__", "", repeatable);
            version = info.getLeft();
            description = info.getRight();
        }

        ResolvedMigrationImpl resolvedMigration = new ResolvedMigrationImpl();
        resolvedMigration.setVersion(version);
        resolvedMigration.setDescription(description);
        resolvedMigration.setScript(springJdbcMigration.getClass().getName());
        resolvedMigration.setChecksum(checksum);
        resolvedMigration.setType(MigrationType.SPRING_JDBC);
        return resolvedMigration;
    }
}
