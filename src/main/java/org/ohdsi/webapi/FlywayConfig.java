package org.ohdsi.webapi;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.zaxxer.hikari.HikariDataSource;


/**
 * Flyway configuration for database migrations (Flyway 11.7 / Spring Boot 3.x)
 *
 * Spring Boot auto-configuration handles Flyway initialization.
 * Java-based migrations marked with @Component are automatically discovered
 * and their dependencies are autowired by Spring.
 */
@Configuration
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
public class FlywayConfig {

    @Value("${spring.flyway.url:#{null}}")
    private String flywayUrl;

    @Value("${spring.flyway.user:#{null}}")
    private String flywayUsername;

    @Value("${spring.flyway.password:#{null}}")
    private String flywayPassword;

    @Value("${spring.flyway.driver-class-name:org.postgresql.Driver}")
    private String flywayDriverClassName;

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

}
