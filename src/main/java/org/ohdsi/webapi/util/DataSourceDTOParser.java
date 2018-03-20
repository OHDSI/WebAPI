package org.ohdsi.webapi.util;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import java.util.Arrays;
import org.ohdsi.webapi.source.Source;

public class DataSourceDTOParser {

    private static final String SCHEMA_SEPARATOR = "://";

    public static DataSourceUnsecuredDTO parseDTO(Source source) {
        DBMSType dbmsType = Arrays.asList(DBMSType.values()).stream()
                .filter(type -> type.getOhdsiDB().equals(source.getSourceDialect()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unsupported data source dialect"));

        DataSourceUnsecuredDTO dto = parseConnectionString(source.getSourceConnection());
        dto.setType(dbmsType);

        return dto;
    }

    public static DataSourceUnsecuredDTO parseConnectionString(String connString) {
        int iHostStart = connString.indexOf(SCHEMA_SEPARATOR)
                + SCHEMA_SEPARATOR.length();
        int iHostEnd = connString.indexOf(":", iHostStart);
        String host = connString.substring(iHostStart, iHostEnd);

        int iPortStart = iHostEnd + 1;
        int iPortEnd = connString.indexOf("/", iHostEnd);
        Integer port = Integer.parseInt(
                connString.substring(iPortStart, iPortEnd));

        int iNameStart = iPortEnd + 1;
        int iNameEnd = connString.indexOf("?", iNameStart);
        if (iNameEnd < 0){
            iNameEnd = connString.indexOf(";", iNameStart);
        }
        String dbName = connString.substring(iNameStart, iNameEnd);

        String username = "", password = "";

        if (!connString.contains("AuthMech=0")) {

            int iUserNameStart = connString.indexOf("=", iNameEnd) + 1;
            int iUserNameEnd = connString.indexOf("&", iUserNameStart);
            username = connString.substring(iUserNameStart, iUserNameEnd);

            int iPassStart = connString.indexOf("=", iUserNameEnd) + 1;
            int iPassEnd = connString.indexOf("&", iPassStart);
            if (iPassEnd == -1) {
                iPassEnd = connString.length();
            }
            password = connString.substring(iPassStart, iPassEnd);
        }

        DataSourceUnsecuredDTO dto = new DataSourceUnsecuredDTO();
        dto.setConnectionString(connString);
        dto.setUsername(username);
        dto.setPassword(password);
        return dto;
    }
}
