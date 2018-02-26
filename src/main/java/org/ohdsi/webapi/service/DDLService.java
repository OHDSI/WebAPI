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

	private static final Collection<String> RESULT_DDL_FILE_PATHS = Arrays.asList(
		"/ddl/results/cohort.sql",
		"/ddl/results/cohort_features.sql",
		"/ddl/results/cohort_features_analysis_ref.sql",
		"/ddl/results/cohort_features_dist.sql",
		"/ddl/results/cohort_features_ref.sql",
		"/ddl/results/cohort_inclusion.sql",
		"/ddl/results/cohort_inclusion_result.sql",
		"/ddl/results/cohort_inclusion_stats.sql",
		"/ddl/results/cohort_summary_stats.sql",
		"/ddl/results/feas_study_inclusion_stats.sql",
		"/ddl/results/feas_study_index_stats.sql",
		"/ddl/results/feas_study_result.sql",
		"/ddl/results/heracles_analysis.sql",
		"/ddl/results/heracles_heel_results.sql",
		"/ddl/results/heracles_results.sql",
		"/ddl/results/heracles_results_dist.sql",
		"/ddl/results/ir_analysis_dist.sql",
		"/ddl/results/ir_analysis_result.sql",
		"/ddl/results/ir_analysis_strata_stats.sql",
		"/ddl/results/ir_strata.sql"
	);

	public static final Collection<String> RESULT_INIT_FILE_PATHS = Arrays.asList(
		"/ddl/results/init_heracles_analysis.sql"
	);

	private static final Collection<String> RESULT_INDEX_FILE_PATHS = Arrays.asList(
		"/ddl/results/create_index.sql"
	);

	private static final Collection<String> DBMS_NO_INDEXES = Arrays.asList("redshift", "impala", "netezza");

	@GET
	@Path("results")
	@Produces("text/plain")
	public String generateResultSQL(@QueryParam("dialect") String dialect, @DefaultValue("results") @QueryParam("schema") String schema) {

		StringBuilder sqlBuilder = new StringBuilder();
		for (String fileName : RESULT_DDL_FILE_PATHS) {
			sqlBuilder.append("\n").append(ResourceHelper.GetResourceAsString(fileName));
		}

		for (String fileName : RESULT_INIT_FILE_PATHS) {
			sqlBuilder.append("\n").append(ResourceHelper.GetResourceAsString(fileName));
		}

		if (dialect == null || DBMS_NO_INDEXES.stream().noneMatch(dbms -> dbms.equals(dialect.toLowerCase()))) {
			for (String fileName : RESULT_INDEX_FILE_PATHS) {
				sqlBuilder.append("\n").append(ResourceHelper.GetResourceAsString(fileName));
			}
		}
		String result = sqlBuilder.toString();
		if (dialect != null) {
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
