package org.ohdsi.webapi.arachne.commons.config.flyway;

import org.flywaydb.core.api.migration.spring.SpringJdbcMigration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class ApplicationContextAwareSpringMigration implements SpringJdbcMigration, ApplicationContextAware {

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    protected <T> T getBean(Class<T> requiredType) {
        return applicationContext.getBean(requiredType);
    }

    protected Object getBean(String name) {
        return applicationContext.getBean(name);
    }

    @Override
    public void migrate(JdbcTemplate jdbcTemplate) throws Exception {
        migrate();
    }

    public abstract void migrate() throws Exception;
}
