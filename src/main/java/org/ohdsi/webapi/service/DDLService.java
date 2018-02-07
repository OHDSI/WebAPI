/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Authors: Maria Pozhidaeva
 *
 */
package org.ohdsi.webapi.service;

import static org.ohdsi.webapi.service.SqlRenderService.translateSQL;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.webapi.sqlrender.SourceStatement;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;
import org.springframework.stereotype.Component;

@Path("/ddl/")
@Component
public class DDLService {

    private static final Collection<String> COMMON_COHORT_RESULT_FILE_PATHS = Arrays.asList(
            "/db/cohort_results/cohort_feasiblity.sql",
            "/db/cohort_results/cohort_features_results.sql",
            "/db/cohort_results/ir_analysis.sql",
            "/db/cohort_results/createHeraclesTables.sql");

    private static final String INDEXES_COHORT_RESULT_FILE_PATHS = "/db/cohort_results/heracles_indexes.sql";
    private static final Collection<String> DBMS_NO_INDEXES = Arrays.asList("redshift", "impala", "netezza");

    @GET
    @Path("results")
    @Produces("text/plain")
    public String generateResultSQL(@QueryParam("dialect") String dialect, @DefaultValue("results") @QueryParam("schema") String schema) {

        StringBuilder sqlBuilder = new StringBuilder();
        for (String fileName : COMMON_COHORT_RESULT_FILE_PATHS){
            sqlBuilder.append("\n").append(ResourceHelper.GetResourceAsString(fileName));
        }
        if (dialect == null || DBMS_NO_INDEXES.stream().noneMatch(dbms -> dbms.equals(dialect.toLowerCase()))) {
            sqlBuilder.append("\n").append(ResourceHelper.GetResourceAsString(INDEXES_COHORT_RESULT_FILE_PATHS));
        }
        String result = sqlBuilder.toString();
        if(dialect != null){
           result = translateSqlFile(result, dialect, schema);
        }
        return result.replaceAll(";", ";\n");
    }

    private String translateSqlFile(String sql, String dialect, String schema) {

        SourceStatement statement = new SourceStatement();
        statement.targetDialect = dialect.toLowerCase();
        statement.sql = sql;
        HashMap<String, String> parameters = new HashMap<>();
        parameters.put("results_schema", schema);
        statement.parameters = parameters;
        TranslatedStatement translatedStatement = translateSQL(statement);
        return translatedStatement.targetSQL;
    }

}
