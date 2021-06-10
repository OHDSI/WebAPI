package org.ohdsi.webapi.util;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AuthMethod;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.*;

import com.odysseusinc.arachne.execution_engine_common.util.BigQueryUtils;
import com.odysseusinc.arachne.execution_engine_common.util.ConnectionParams;
import com.odysseusinc.arachne.execution_engine_common.util.ConnectionParamsParser;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.KerberosUtils;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;

import static com.odysseusinc.arachne.commons.types.DBMSType.BIGQUERY;
import static com.odysseusinc.arachne.commons.types.DBMSType.IMPALA;

public final class DataSourceDTOParser {

    public static DataSourceUnsecuredDTO parseDTO(Source source) {
        ConnectionParams params = parse(source);
        DataSourceUnsecuredDTO dto = new DataSourceUnsecuredDTO();
        dto.setName(source.getSourceName());
        dto.setType(getDbmsType(source));
        dto.setConnectionString(params.getConnectionString());
        dto.setUsername(params.getUser());
        dto.setPassword(params.getPassword());
        if (Objects.equals(IMPALA, dto.getType()) && AuthMethod.KERBEROS == params.getAuthMethod()) {
            KerberosUtils.setKerberosParams(source, params, dto);
        }
        if (Objects.equals(BIGQUERY, dto.getType())) {
            try {
                dto.setKeyfile(getKeyfile(source));
            } catch (IOException ignored) {
            }
        }

        dto.setCdmSchema(source.getTableQualifierOrNull(SourceDaimon.DaimonType.CDM));
        dto.setVocabularySchema(source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary));
        dto.setResultSchema(source.getTableQualifierOrNull(SourceDaimon.DaimonType.Results));

        return dto;
    }

    static private byte[] getKeyfile(Source source) throws IOException {
        if (ArrayUtils.isNotEmpty(source.getKeyfile())) {
            return source.getKeyfile();
        } else {
            String keyPath = BigQueryUtils.getBigQueryKeyPath(source.getSourceConnection());
            if (StringUtils.isNotEmpty(keyPath) && Paths.get(keyPath).toFile().exists()) {
                try(Reader r = new FileReader(new File(keyPath))) {
                    return IOUtils.toByteArray(r, Charset.defaultCharset());
                }
            }
        }
        return null;
    }

    private static ConnectionParams parse(Source source) {

        Objects.requireNonNull(source, "Source should not be null");

        ConnectionParams dto = ConnectionParamsParser.parse(getDbmsType(source), source.getSourceConnection());
        if (Objects.isNull(dto.getUser())) {
            dto.setUser(source.getUsername());
        }
        if (Objects.isNull(dto.getPassword())) {
            dto.setPassword(source.getPassword());
        }
        return dto;
    }

    private static DBMSType getDbmsType(Source source) {
        return Arrays.stream(DBMSType.values())
                    .filter(type -> Objects.equals(type.getOhdsiDB(), source.getSourceDialect()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(String.format("Unsupported data source dialect: %s", source.getSourceDialect())));
    }

}
