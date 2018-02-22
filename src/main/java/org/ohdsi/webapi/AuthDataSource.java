/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Mikhail Mironov, Vitaly Koulakov
 *
 */
package org.ohdsi.webapi;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

@Configuration("authDataSourceConfig")
@ConditionalOnProperty(name = "security.enabled", havingValue = "true")
public class AuthDataSource {

    @Value("${security.db.datasource.driverClassName}")
    private String driverClassName;
    @Value("${security.db.datasource.url}")
    private String url;
    @Value("${security.db.datasource.username}")
    private String username;
    @Value("${security.db.datasource.password}")
    private String password;
    @Value("${security.db.datasource.schema}")
    private String schema;
    @Value("${spring.datasource.hikari.connection-test-query}")
    private String testQuery;
    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maxPoolSize;
    @Value("${spring.datasource.hikari.minimum-idle}")
    private int minPoolIdle;

    @Bean(name = "authDataSource")
    public DataSource authDataSource() {

        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setConnectionTestQuery(testQuery);
        config.setMaximumPoolSize(maxPoolSize);
        config.setMinimumIdle(minPoolIdle);
        return new HikariDataSource(config);
    }
}
