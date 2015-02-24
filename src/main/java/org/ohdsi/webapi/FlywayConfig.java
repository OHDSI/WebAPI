package org.ohdsi.webapi;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.flyway.FlywayDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 *
 */
@Configuration
public class FlywayConfig {
 
    @Bean
    @ConfigurationProperties(prefix="flyway.datasource")
    @FlywayDataSource
    public DataSource secondaryDataSource() {
        return DataSourceBuilder.create().build();
    }
}
