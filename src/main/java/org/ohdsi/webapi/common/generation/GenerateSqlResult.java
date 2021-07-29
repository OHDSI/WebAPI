package org.ohdsi.webapi.common.generation;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GenerateSqlResult {
    @JsonProperty("templateSql")
    public String templateSql;
}
