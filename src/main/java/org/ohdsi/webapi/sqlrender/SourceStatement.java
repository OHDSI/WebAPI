package org.ohdsi.webapi.sqlrender;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Lee Evans
 */
public class SourceStatement {

    @JsonProperty("targetdialect")
    private String targetDialect;

    @JsonProperty("oracleTempSchema")
    private String oracleTempSchema;

    @JsonProperty("SQL")
    private String sql;
    
    @JsonProperty("parameters")
    private Map<String,String> parameters = new HashMap<>();

    public String getTargetDialect() {

        return targetDialect;
    }

    public void setTargetDialect(String targetDialect) {

        this.targetDialect = targetDialect;
    }

    public String getOracleTempSchema() {

        return oracleTempSchema;
    }

    public void setOracleTempSchema(String oracleTempSchema) {

        this.oracleTempSchema = oracleTempSchema;
    }

    public String getSql() {

        return sql;
    }

    public void setSql(String sql) {

        this.sql = sql;
    }

    public Map<String, String> getParameters() {

        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {

        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceStatement that = (SourceStatement) o;
        return Objects.equals(targetDialect, that.targetDialect) &&
                Objects.equals(oracleTempSchema, that.oracleTempSchema) &&
                Objects.equals(sql, that.sql) &&
                Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {

        return Objects.hash(targetDialect, oracleTempSchema, sql, parameters);
    }
}
