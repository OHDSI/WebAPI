package org.ohdsi.webapi;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 *
 */
@Configuration
public class DataAccessConfig {
    
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "datasource")
    public DataSource primaryDataSource() {
        return DataSourceBuilder.create().build();
    }
    
}
