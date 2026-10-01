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
package org.ohdsi.webapi.security.authc.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration("authDataSourceConfig")
@ConditionalOnProperty(prefix = "security.auth.db", name = "enabled", havingValue = "true")
public class AuthDataSource {
    private final Logger logger = LoggerFactory.getLogger(AuthDataSource.class);

    @Bean
    @ConfigurationProperties(prefix = "security.auth.db.datasource")
    public AuthDataSourceProperties authDataSourceProperties() {
        return new AuthDataSourceProperties();
    }

    @Bean(name = "authDataSource")
    public DataSource authDataSource(AuthDataSourceProperties props) {

        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName(props.getDriverClassName());
            config.setJdbcUrl(props.getUrl());
            config.setUsername(props.getUsername());
            config.setPassword(props.getPassword());
            config.setSchema(props.getSchema());
            config.setConnectionTestQuery(props.getConnectionTestQuery());
            config.setConnectionTimeout(props.getConnectionTimeout());
            config.setMaximumPoolSize(props.getMaximumPoolSize());
            config.setMinimumIdle(props.getMinimumIdle());
            config.setValidationTimeout(props.getConnectionTestQueryTimeout());
            config.setPoolName(props.getPoolName());
            config.setRegisterMbeans(props.isRegisterMbeans());
            return new HikariDataSource(config);
        } catch (Exception ex) {
            logger.error("Failed to initialize connection to DB used for authentication: {}", ex.getMessage());
            return null;
        }
    }
}
