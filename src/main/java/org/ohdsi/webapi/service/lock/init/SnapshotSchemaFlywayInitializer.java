package org.ohdsi.webapi.service.lock.init;

import org.flywaydb.core.Flyway;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.util.Collections;

@Component
public class SnapshotSchemaFlywayInitializer {

	private static final String SNAPSHOT_HISTORY_SCHEMA_PLACEHOLDER = "snapshotHistorySchema";

	protected final Logger log = LoggerFactory.getLogger(getClass());

	@Value("${snapshot.history.flyway.locations}")
	private String snapshotHistoryFlywayLocations;

	@Value("${snapshot.history.sourceKey}")
	private String snapshotHistorySourceKey;

	@Value("${snapshot.history.sourceSchema}")
	private String snapshotHistorySourceSchema;

	@Autowired
	private SourceRepository sourceRepository;

	@PostConstruct
	public void initializeSnapshotStorageSchemaIfEmpty() {
		Source snapshotHistorySource = sourceRepository.findBySourceKey(snapshotHistorySourceKey);

		if (snapshotHistorySource == null) {
			log.error("Snapshot source was not found by source key: {}", snapshotHistorySourceKey);
			return;
		}

		String externalDbUrl = snapshotHistorySource.getSourceConnection();
		String user = snapshotHistorySource.getUsername();
		String password = snapshotHistorySource.getPassword();

		DataSource externalDataSource = new DriverManagerDataSource(externalDbUrl, user, password);

		Flyway flyway = new Flyway();
		flyway.setSchemas(snapshotHistorySourceSchema);
		flyway.setPlaceholders(Collections.singletonMap(SNAPSHOT_HISTORY_SCHEMA_PLACEHOLDER, snapshotHistorySourceSchema));
		flyway.setDataSource(externalDataSource);
		flyway.setLocations(snapshotHistoryFlywayLocations);
		flyway.migrate();
	}
}