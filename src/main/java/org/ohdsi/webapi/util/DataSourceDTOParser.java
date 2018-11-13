package org.ohdsi.webapi.util;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AuthMethod;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;

import java.util.*;

import com.odysseusinc.arachne.execution_engine_common.util.ConnectionParams;
import com.odysseusinc.arachne.execution_engine_common.util.ConnectionParamsParser;
import org.ohdsi.webapi.KerberosUtils;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;

import static com.odysseusinc.arachne.commons.types.DBMSType.IMPALA;

public final class DataSourceDTOParser {

    public static DataSourceUnsecuredDTO parseDTO(Source source) {
        ConnectionParams params = parse(source);
        DataSourceUnsecuredDTO dto = new DataSourceUnsecuredDTO();
        dto.setType(getDbmsType(source));
        dto.setConnectionString(params.getConnectionString());
        dto.setUsername(params.getUser());
        dto.setPassword(params.getPassword());
        if (Objects.equals(IMPALA, dto.getType()) && AuthMethod.KERBEROS == params.getAuthMethod()) {
            KerberosUtils.setKerberosParams(source, params, dto);
        }

        dto.setCdmSchema(source.getTableQualifierOrNull(SourceDaimon.DaimonType.CDM));
        dto.setVocabularySchema(source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary));
        dto.setResultSchema(source.getTableQualifierOrNull(SourceDaimon.DaimonType.Results));

        return dto;
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
                    .orElseThrow(() -> new RuntimeException("Unsupported data source dialect"));
    }

}
