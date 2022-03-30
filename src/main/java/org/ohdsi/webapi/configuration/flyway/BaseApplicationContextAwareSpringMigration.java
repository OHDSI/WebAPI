package org.ohdsi.webapi.configuration.flyway;

import org.flywaydb.core.api.configuration.ConfigurationAware;
import org.flywaydb.core.api.configuration.FlywayConfiguration;

public abstract class BaseApplicationContextAwareSpringMigration implements ApplicationContextAwareSpringMigration, ConfigurationAware {
    protected FlywayConfiguration flywayConfiguration;

    public BaseApplicationContextAwareSpringMigration() {
    }

    public void setFlywayConfiguration(FlywayConfiguration flywayConfiguration) {
        this.flywayConfiguration = flywayConfiguration;
    }
}
