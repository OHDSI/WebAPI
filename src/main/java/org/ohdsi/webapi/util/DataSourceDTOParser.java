package org.ohdsi.webapi.util;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.ohdsi.webapi.source.Source;

public final class DataSourceDTOParser {

    public static DataSourceUnsecuredDTO parseDTO(Source source) {
        ConnectionParams params = parse(source);
        DataSourceUnsecuredDTO dto = new DataSourceUnsecuredDTO();
        dto.setType(getDbmsType(source));
        dto.setConnectionString(params.getConnectionString());
        dto.setUsername(params.getUser());
        dto.setPassword(params.getPassword());

        return dto;
    }

    public static ConnectionParams parse(Source source) {

        Objects.requireNonNull(source, "Source should not be null");
        return getParser(getDbmsType(source)).parse(source);
    }

    private static DBMSType getDbmsType(Source source) {
        return Arrays.stream(DBMSType.values())
                    .filter(type -> Objects.equals(type.getOhdsiDB(), source.getSourceDialect()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Unsupported data source dialect"));
    }

    private static ConnectionStringParser getParser(DBMSType dbmsType) {

        switch (dbmsType) {
            case POSTGRESQL:
            case MS_SQL_SERVER:
            case NETEZZA:
            case PDW:
                return new GenericParser();
            case REDSHIFT:
                return new RedshiftParser();
            case ORACLE:
                return new OracleParser();
            case IMPALA:
                return new ImpalaParser();
            default:
                return source -> {
                    ConnectionParams dto = new ConnectionParams();
                    dto.setConnectionString(source.getSourceConnection());
                    return dto;
                };
        }
    }

    interface ConnectionStringParser {
        ConnectionParams parse(Source source);
    }

    static class GenericParser implements ConnectionStringParser {

        private Pattern pattern = Pattern.compile("^jdbc:\\w+(:\\w+)?://([\\w.\\d]+)(:\\d+)?(/(\\w+))?[?;]?(.*)");

        @Override
        public ConnectionParams parse(Source source) {

            final String connString = source.getSourceConnection();
            ConnectionParams dto = new ConnectionParams();
            dto.setDbms(source.getSourceDialect());
            dto.setConnectionString(connString);
            dto.setUser(source.getUsername());
            dto.setPassword(source.getPassword());
            parseParams(connString, dto);

            return dto;
        }

        protected void parseParams(String connString, ConnectionParams dto) {
            Matcher matcher = pattern.matcher(connString);
            if (matcher.matches() && matcher.groupCount() == 6){
                dto.setServer(matcher.group(2));
                dto.setPort(matcher.group(3));
                dto.setSchema(matcher.group(5));
                String paramString = matcher.group(6); //params
                dto.setExtraSettings(paramString);
                if (Objects.nonNull(paramString)) {
                    List<String> paramValues = Arrays.asList(paramString.split("[&;]"));
                    Map<String, String> params = paramValues.stream()
                            .filter(v -> Objects.nonNull(v) && v.contains("="))
                            .map(v -> v.split("=")).collect(Collectors.toMap(x -> x[0], x -> x[1]));
                    parseCredentials(dto, params);
                }
            }
        }

        protected void parseCredentials(ConnectionParams dto, Map<String, String> params) {
            dto.setUser(params.getOrDefault(getUserParamName(), dto.getUser()));
            dto.setPassword(params.getOrDefault(getPasswordParamName(), dto.getPassword()));
        }

        protected String getUserParamName() {
            return "user";
        }

        protected String getPasswordParamName() {
            return "password";
        }
    }

    static class ImpalaParser extends RedshiftParser {
        @Override
        protected void parseCredentials(ConnectionParams dto, Map<String, String> params) {
            try {
                Integer authMech = Integer.valueOf(params.getOrDefault("AuthMech", "0"));
                if (3 == authMech) {
                    super.parseCredentials(dto, params);
                }
            }catch (NumberFormatException ignored){
            }
        }
    }

    static class RedshiftParser extends GenericParser {
        @Override
        protected String getUserParamName() {
            return "UID";
        }

        @Override
        protected String getPasswordParamName() {
            return "PWD";
        }
    }

    static class OracleParser extends GenericParser {

        private Pattern pattern = Pattern.compile("^jdbc:oracle:\\w+:((\\w+)/(\\w+))?@(\\S+)");

        @Override
        protected void parseParams(String connString, ConnectionParams dto) {
            Matcher matcher = pattern.matcher(connString);
            if (matcher.matches() && matcher.groupCount() == 4) {
                dto.setUser(matcher.group(2));
                dto.setPassword(matcher.group(3));
                dto.setServer(matcher.group(4));
            }
        }
    }

}
