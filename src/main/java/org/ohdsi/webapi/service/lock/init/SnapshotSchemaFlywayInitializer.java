package org.ohdsi.webapi.service.lock.init;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Collections;

@Component
public class SnapshotSchemaFlywayInitializer {
	private static final Logger LOG = LoggerFactory.getLogger(SnapshotSchemaFlywayInitializer.class);
	private static final String SNAPSHOT_HISTORY_SCHEMA_PLACEHOLDER = "snapshotHistorySchema";

	@Value("${snapshot.history.flyway.locations}")
	private String snapshotHistoryFlywayLocations;

	@Value("${snapshot.history.source.connection}")
	private String snapshotHistorySourceConnection;
	@Value("${snapshot.history.source.schema}")
	private String snapshotHistorySourceSchema;
	@Value("${snapshot.history.source.username}")
	private String snapshotHistorySourceUsername;
	@Value("${snapshot.history.source.password}")
	private String snapshotHistorySourcePassword;

	@PostConstruct
	public void tryToInitializeSnapshotStorageSchemaIfEmpty() {
		try {
			initializeSnapshotStorageSchemaIfEmpty();
		} catch (Exception e) {
			LOG.warn("Could not initialize the snapshot history schema", e);
		}
	}

	private void initializeSnapshotStorageSchemaIfEmpty() {
		DataSource externalDataSource = new DriverManagerDataSource(snapshotHistorySourceConnection, snapshotHistorySourceUsername, snapshotHistorySourcePassword);
		Flyway flyway = new Flyway();
		flyway.setSchemas(snapshotHistorySourceSchema);
		flyway.setPlaceholders(Collections.singletonMap(SNAPSHOT_HISTORY_SCHEMA_PLACEHOLDER, snapshotHistorySourceSchema));
		flyway.setDataSource(externalDataSource);
		flyway.setLocations(snapshotHistoryFlywayLocations);
		flyway.migrate();
	}

}