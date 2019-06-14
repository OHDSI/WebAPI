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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration("authDataSourceConfig")
@ConditionalOnProperty(name = "security.provider", havingValue = "AtlasRegularSecurity")
public class AuthDataSource {
    private final Logger logger = LoggerFactory.getLogger(AuthDataSource.class);

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
    @Value("${spring.datasource.hikari.connection-test-query-timeout}")
    private Long validationTimeout;
    @Value("${spring.datasource.hikari.maximum-pool-size}")
    private int maxPoolSize;
    @Value("${spring.datasource.hikari.minimum-idle}")
    private int minPoolIdle;
    @Value("${spring.datasource.hikari.connection-timeout}")
    private int connectionTimeout;
    @Value("${spring.datasource.hikari.register-mbeans}")
    private boolean registerMbeans;
    @Value("${spring.datasource.hikari.mbean-name}")
    private String mbeanName;

    @Bean(name = "authDataSource")
    public DataSource authDataSource() {

        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(driverClassName);
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setSchema(schema);
            config.setConnectionTestQuery(testQuery);
            config.setConnectionTimeout(connectionTimeout);
            config.setMaximumPoolSize(maxPoolSize);
            config.setMinimumIdle(minPoolIdle);
            config.setValidationTimeout(validationTimeout);
            config.setPoolName(mbeanName);
            config.setRegisterMbeans(registerMbeans);
            return new HikariDataSource(config);
        } catch (Exception ex) {
            logger.error("Failed to initialize connection to DB used for authentication: {}", ex.getMessage());
            return null;
        }
    }
}
