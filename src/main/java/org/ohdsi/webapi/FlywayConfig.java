package org.ohdsi.webapi;

import org.ohdsi.webapi.arachne.commons.config.flyway.ApplicationContextAwareSpringJdbcMigrationResolver;
import javax.sql.DataSource;
import org.flywaydb.core.Flyway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationInitializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.zaxxer.hikari.HikariDataSource;


/**
 * Flyway configuration for database migrations
 */
@Configuration
@ConditionalOnProperty(prefix = "flyway", name = "enabled", matchIfMissing = true)
public class FlywayConfig {
 
    @Value("${flyway.datasource.url:#{null}}")
    private String flywayUrl;
    
    @Value("${flyway.datasource.username:#{null}}")
    private String flywayUsername;
    
    @Value("${flyway.datasource.password:#{null}}")
    private String flywayPassword;
    
    @Value("${flyway.datasource.driverClassName:org.postgresql.Driver}")
    private String flywayDriverClassName;

    @Bean
    @FlywayDataSource
    public DataSource secondaryDataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        if (flywayUrl != null) {
            dataSource.setJdbcUrl(flywayUrl);
        }
        if (flywayUsername != null) {
            dataSource.setUsername(flywayUsername);
        }
        if (flywayPassword != null) {
            dataSource.setPassword(flywayPassword);
        }
        dataSource.setDriverClassName(flywayDriverClassName);
        return dataSource;
    }

    @Bean(initMethod = "migrate", name = "flyway")
    @ConfigurationProperties(prefix="flyway")
    public Flyway flyway() {
      Flyway flyway = new Flyway();
      flyway.setDataSource(secondaryDataSource());
      return flyway;
    }

    @Bean
    public FlywayMigrationInitializer flywayInitializer(ApplicationContext context, Flyway flyway) {

        ApplicationContextAwareSpringJdbcMigrationResolver contextAwareResolver = new ApplicationContextAwareSpringJdbcMigrationResolver(context);
        flyway.setResolvers(contextAwareResolver);

        return new FlywayMigrationInitializer(flyway, null);
    }

}
