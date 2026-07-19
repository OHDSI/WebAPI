package org.ohdsi.webapi.service;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import com.odysseusinc.datasourcemanager.krblogin.KerberosService;
import com.odysseusinc.datasourcemanager.krblogin.KrbConfig;
import com.odysseusinc.datasourcemanager.krblogin.RuntimeServiceMode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.DataSourceDTOParser;
import org.ohdsi.webapi.source.SourceHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public abstract class CachingDataSourceAbstractDaoService {

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

	@Autowired
	private SourceHelper sourceHelper;

	@Autowired
	private KerberosService kerberosService;

	// Cache: key is source ID (or name), value is HikariDataSource
	private final ConcurrentMap<String, HikariDataSource> dataSourceCache = new ConcurrentHashMap<>();

	/**
	 * Returns a JdbcTemplate for the given Source, using a cached HikariDataSource.
	 * If not cached, creates and caches a new HikariDataSource.
	 */
	public JdbcTemplate getSourceJdbcTemplate(Source source) {
		String cacheKey = getSourceCacheKey(source);

		HikariDataSource hikariDataSource = dataSourceCache.computeIfAbsent(cacheKey, key -> {
			DataSourceUnsecuredDTO dataSourceData = DataSourceDTOParser.parseDTO(source);

			if (dataSourceData.getUseKerberos()) {
				loginToKerberos(dataSourceData);
			}

			String connectionString = sourceHelper.getSourceConnectionString(source);

			HikariConfig config = new HikariConfig();
			config.setJdbcUrl(connectionString);
			config.setConnectionTestQuery(testQuery);
			config.setValidationTimeout(validationTimeout);
			config.setMaximumPoolSize(maxPoolSize);
			config.setMinimumIdle(minPoolIdle);
			config.setConnectionTimeout(connectionTimeout);
			config.setRegisterMbeans(registerMbeans);
			config.setPoolName(mbeanName);

			if (dataSourceData.getUsername() != null && dataSourceData.getPassword() != null) {
				config.setUsername(dataSourceData.getUsername());
				config.setPassword(dataSourceData.getPassword());
			}

			// Optionally set schema, etc. if needed

			return new HikariDataSource(config);
		});

		// You can use CancelableJdbcTemplate if needed, or just JdbcTemplate
		return new JdbcTemplate(hikariDataSource);
	}

	/**
	 * Generates a unique cache key for a Source.
	 * You can use source.getSourceKey(), source.getSourceId(), or source.getSourceName().
	 */
	private String getSourceCacheKey(Source source) {
		// Prefer a unique, immutable identifier
		return String.valueOf(source.getSourceId());
	}

	private void loginToKerberos(DataSourceUnsecuredDTO dataSourceData) {
		File temporaryDir = com.google.common.io.Files.createTempDir();
		try {
			kerberosService.runKinit(dataSourceData, RuntimeServiceMode.SINGLE, temporaryDir);
		} catch (RuntimeException | IOException e) {
			// log error if needed
		}
		try {
			org.apache.commons.io.FileUtils.forceDelete(temporaryDir);
		} catch (IOException e) {
			// log warning if needed
		}
	}

	/**
	 * Clean up all cached DataSources on shutdown.
	 */
	@PreDestroy
	public void closeAllDataSources() {
		dataSourceCache.values().forEach(HikariDataSource::close);
		dataSourceCache.clear();
	}
}