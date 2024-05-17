package org.ohdsi.webapi;

import com.odysseusinc.arachne.commons.config.flyway.ApplicationContextAwareSpringJdbcMigrationResolver;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.sql.init.dependency.DependsOnDatabaseInitialization;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 *
 */
@Configuration
@ConditionalOnProperty(prefix = "flyway", name = "enabled", matchIfMissing = true)
public class FlywayConfig {

    @Bean
    @ConfigurationProperties(prefix = "flyway.datasource")
    @FlywayDataSource
    DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean(initMethod = "migrate", name = "flyway")
    @ConfigurationProperties(prefix = "flyway")
    @DependsOnDatabaseInitialization
    Flyway flyway() {
      Flyway flyway = new Flyway();
      flyway.setDataSource(secondaryDataSource());
      return flyway;
    }

    @Bean
    FlywayMigrationInitializer flywayInitializer(ApplicationContext context, Flyway flyway) {

        ApplicationContextAwareSpringJdbcMigrationResolver contextAwareResolver = new ApplicationContextAwareSpringJdbcMigrationResolver(context);
        flyway.setResolvers(contextAwareResolver);

        return new FlywayMigrationInitializer(flyway, null);
    }

}
