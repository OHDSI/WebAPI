package com.odysseusinc.datasourcemanager.jdbc.auth;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import com.odysseusinc.datasourcemanager.krblogin.KerberosService;
import com.odysseusinc.datasourcemanager.krblogin.KrbConfig;
import com.odysseusinc.datasourcemanager.krblogin.RuntimeServiceMode;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KerberosAuthResolver implements DataSourceAuthResolver<KrbConfig> {

	private static final Logger logger = LoggerFactory.getLogger(KerberosAuthResolver.class);

	private final KerberosService kerberosService;

	public KerberosAuthResolver(KerberosService kerberosService) {

		this.kerberosService = kerberosService;
	}

	@Override
	public boolean supports(DataSourceUnsecuredDTO dataSourceData) {

		return Objects.nonNull(dataSourceData) && dataSourceData.getUseKerberos();
	}

	@Override
	public Optional<KrbConfig> resolveAuth(DataSourceUnsecuredDTO dataSourceData, File workDir) {

		try {
			KrbConfig krbConfig = kerberosService.runKinit(dataSourceData, RuntimeServiceMode.SINGLE, workDir);
			return Optional.of(krbConfig);
		} catch (IOException e) {
			logger.error("Failed to resolve Kerberos authentication for Source: {}", dataSourceData.getName(), e);
			throw new RuntimeException(e);
		}
	}
}
