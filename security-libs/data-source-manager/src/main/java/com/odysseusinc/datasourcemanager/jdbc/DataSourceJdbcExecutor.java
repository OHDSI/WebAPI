package com.odysseusinc.datasourcemanager.jdbc;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import com.odysseusinc.datasourcemanager.jdbc.auth.BigQueryAuthResolver;
import com.odysseusinc.datasourcemanager.jdbc.auth.DataSourceAuthResolver;
import com.odysseusinc.datasourcemanager.jdbc.auth.KerberosAuthResolver;
import com.odysseusinc.datasourcemanager.krblogin.KerberosService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.io.FileUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

public class DataSourceJdbcExecutor {

	private final KerberosService kerberosService;
	private List<DataSourceAuthResolver> authResolvers;

	public DataSourceJdbcExecutor(KerberosService kerberosService) {

		this.kerberosService = kerberosService;
		initDefaultResolvers();
	}

	public DataSourceJdbcExecutor(KerberosService kerberosService, List<DataSourceAuthResolver> authResolvers) {

		if (Objects.isNull(authResolvers)) {
			throw new IllegalArgumentException("authResolvers should not be null");
		}
		this.kerberosService = kerberosService;
		this.authResolvers = authResolvers;
	}

	private void initDefaultResolvers() {

		this.authResolvers = new ArrayList<>();
		authResolvers.add(new BigQueryAuthResolver());
		authResolvers.add(new KerberosAuthResolver(kerberosService));
	}

	public <T> T executeOnSource(DataSourceUnsecuredDTO dataSourceData, JdbcTemplateConsumer<T> consumer) throws IOException {

		if (Objects.isNull(consumer)) {
			throw new IllegalArgumentException("consumer is required");
		}

		File tempDir = Files.createTempDirectory("gis").toFile();

		// Resolve authentication
		authResolvers.stream().filter(r -> r.supports(dataSourceData)).forEach(r -> r.resolveAuth(dataSourceData, tempDir));

		DriverManagerDataSource dataSource = new DriverManagerDataSource(
						dataSourceData.getConnectionString(),
						dataSourceData.getUsername(),
						dataSourceData.getPassword()
		);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		try {
			return consumer.execute(jdbcTemplate);
		} finally {
			FileUtils.deleteQuietly(tempDir);
		}
	}
}
