package org.ohdsi.webapi;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import com.zaxxer.hikari.HikariDataSource;


/**
 * Flyway configuration for database migrations (Flyway 11.7 / Spring Boot 3.x)
 *
 * Spring Boot auto-configuration handles Flyway initialization.
 * Java-based migrations can access Spring beans via the static ApplicationContextHolder.
 */
@Configuration
@Lazy(false)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
public class FlywayConfig {

    /**
     * Static holder for ApplicationContext to be accessed by Flyway migrations.
     * Set during bean initialization to avoid circular dependencies.
     */
    public static class ApplicationContextHolder {
        private static ApplicationContext context;

        public static void setApplicationContext(ApplicationContext ctx) {
            context = ctx;
        }

        public static ApplicationContext getApplicationContext() {
            return context;
        }
    }

    @Value("${spring.flyway.url:#{null}}")
    private String flywayUrl;

    @Value("${spring.flyway.user:#{null}}")
    private String flywayUsername;

    @Value("${spring.flyway.password:#{null}}")
    private String flywayPassword;

    @Value("${spring.flyway.driver-class-name:org.postgresql.Driver}")
    private String flywayDriverClassName;

    @Bean
    public FlywayConfigurationCustomizer ohdsiSchemaPlaceholderCustomizer(
            @Value("${datasource.ohdsi.schema:webapi}") String ohdsiSchema) {
        return configuration -> {
            Map<String, String> placeholders = new HashMap<>(configuration.getPlaceholders());
            placeholders.putIfAbsent("ohdsiSchema", ohdsiSchema);
            configuration.placeholders(placeholders);
        };
    }

    /**
     * DataSource for Flyway migrations.
     * Can be different from the main application DataSource.
     */
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

    /**
     * Store ApplicationContext in static holder for Flyway migrations to access.
     */
    @Bean
    public ApplicationContextHolder applicationContextHolder(ApplicationContext applicationContext) {
        ApplicationContextHolder.setApplicationContext(applicationContext);
        return new ApplicationContextHolder();
    }

}
