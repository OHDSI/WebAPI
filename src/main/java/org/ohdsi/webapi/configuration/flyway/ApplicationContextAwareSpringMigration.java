package org.ohdsi.webapi.configuration.flyway;

public interface ApplicationContextAwareSpringMigration {
    void migrate() throws Exception;
}
