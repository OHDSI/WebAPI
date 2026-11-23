package com.odysseusinc.datasourcemanager.jdbc.auth;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import java.io.File;
import java.util.Optional;

public interface DataSourceAuthResolver<T> {

	boolean supports(DataSourceUnsecuredDTO dataSourceData);

	Optional<T> resolveAuth(DataSourceUnsecuredDTO dataSourceData, File workDir);
}
