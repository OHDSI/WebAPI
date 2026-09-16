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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.webapi.sqlrender.SourceStatement;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

@RestController
@RequestMapping("/ddl")
public class DDLService {

	public static final String VOCAB_SCHEMA = "vocab_schema";
	public static final String RESULTS_SCHEMA = "results_schema";
	public static final String CEM_SCHEMA = "cem_results_schema";
	public static final String TEMP_SCHEMA = "oracle_temp_schema";

	private static final Collection<String> RESULT_DDL_FILE_PATHS = Arrays.asList(
		// cohort generation results
		"/ddl/results/cohort.sql",
		"/ddl/results/cohort_censor_stats.sql",
		"/ddl/results/cohort_inclusion.sql",
		"/ddl/results/cohort_inclusion_result.sql",
		"/ddl/results/cohort_inclusion_stats.sql",
		"/ddl/results/cohort_summary_stats.sql",
		// cohort generation cache
		"/ddl/results/cohort_cache.sql",
		"/ddl/results/cohort_censor_stats_cache.sql",
		"/ddl/results/cohort_inclusion_result_cache.sql",
		"/ddl/results/cohort_inclusion_stats_cache.sql",
		"/ddl/results/cohort_summary_stats_cache.sql",
		// cohort sampling
		"/ddl/results/cohort_sample_element.sql",
		// incidence rates
		"/ddl/results/ir_analysis_dist.sql",
		"/ddl/results/ir_analysis_result.sql",
		"/ddl/results/ir_analysis_strata_stats.sql",
		"/ddl/results/ir_strata.sql",
		// characterization
		"/ddl/results/cohort_characterizations.sql",
		// pathways
		"/ddl/results/pathway_analysis_codes.sql",
		"/ddl/results/pathway_analysis_events.sql",
		"/ddl/results/pathway_analysis_paths.sql",
		"/ddl/results/pathway_analysis_stats.sql"
	);

	public static final Collection<String> INIT_CONCEPT_HIERARCHY_FILE_PATHS = Arrays.asList(
			"/ddl/results/concept_hierarchy.sql",
			"/ddl/results/init_concept_hierarchy.sql"
	);

	private static final Collection<String> RESULT_INDEX_FILE_PATHS = Arrays.asList(
		"/ddl/results/create_index.sql",
		"/ddl/results/pathway_analysis_events_indexes.sql"
	);

	private static final Collection<String> CEMRESULT_DDL_FILE_PATHS = Arrays.asList(
		"/ddl/cemresults/nc_results.sql"
	);

	public static final Collection<String> CEMRESULT_INIT_FILE_PATHS = Arrays.asList();

	private static final Collection<String> CEMRESULT_INDEX_FILE_PATHS = Arrays.asList();

	private static final Collection<String> ACHILLES_DDL_FILE_PATHS = Arrays.asList(
			"/ddl/achilles/achilles_result_concept_count.sql"
	);

	private static final Collection<String> DBMS_NO_INDEXES = Arrays.asList("redshift", "impala", "netezza", "spark");

	/**
	 * Get DDL for results schema
	 * @param dialect SQL dialect (e.g. sql server)
	 * @param vocabSchema
	 * @param resultSchema
	 * @param initConceptHierarchy
	 * @param tempSchema
	 * @return SQL to create tables in results schema
	 */
	@PreAuthorize("isPermitted('list')")
	@GetMapping(value = "/results", produces = MediaType.TEXT_PLAIN_VALUE)
	public String generateResultSQL(
			@RequestParam(value = "dialect", required = false) String dialect,
			@RequestParam(value = "vocabSchema", defaultValue = "vocab") String vocabSchema,
			@RequestParam(value = "schema", defaultValue = "results") String resultSchema,
			@RequestParam(value = "initConceptHierarchy", defaultValue = "true") Boolean initConceptHierarchy,
			@RequestParam(value = "tempSchema", required = false) String tempSchema) {

		Collection<String> resultDDLFilePaths = new ArrayList<>(RESULT_DDL_FILE_PATHS);

		if (initConceptHierarchy) {
			resultDDLFilePaths.addAll(INIT_CONCEPT_HIERARCHY_FILE_PATHS);
		}
		String oracleTempSchema = ObjectUtils.firstNonNull(tempSchema, resultSchema);
		Map<String, String> params = new HashMap<String, String>() {{
			put(VOCAB_SCHEMA, vocabSchema);
			put(RESULTS_SCHEMA, resultSchema);
			put(TEMP_SCHEMA, oracleTempSchema);
		}};

		return generateSQL(dialect, params, resultDDLFilePaths, getResultInitFilePaths(dialect), RESULT_INDEX_FILE_PATHS);
	}

	private Collection<String> getResultInitFilePaths(String dialect) {
		return new ArrayList<>();
	}

	/**
	 * Get DDL for Common Evidence Model results schema
	 * @param dialect SQL dialect
	 * @param schema schema name
	 * @return SQL
	 */
	@PreAuthorize("isPermitted('list')")
	@GetMapping(value = "/cemresults", produces = MediaType.TEXT_PLAIN_VALUE)
	public String generateCemResultSQL(
			@RequestParam(value = "dialect", required = false) String dialect,
			@RequestParam(value = "schema", defaultValue = "cemresults") String schema) {

		Map<String, String> params = new HashMap<String, String>() {{
			put(CEM_SCHEMA, schema);
		}};

		return generateSQL(dialect, params, CEMRESULT_DDL_FILE_PATHS, CEMRESULT_INIT_FILE_PATHS, CEMRESULT_INDEX_FILE_PATHS);
	}

	/**
	 * Get DDL for Achilles results tables
	 * @param dialect SQL dialect
	 * @param vocabSchema OMOP vocabulary schema
	 * @param resultSchema results schema
	 * @return SQL
	 */
	@PreAuthorize("isPermitted('list')")
	@GetMapping(value = "/achilles", produces = MediaType.TEXT_PLAIN_VALUE)
	public String generateAchillesSQL(
			@RequestParam(value = "dialect", required = false) String dialect,
			@RequestParam(value = "vocabSchema", defaultValue = "vocab") String vocabSchema,
			@RequestParam(value = "schema", defaultValue = "results") String resultSchema) {

		final Collection<String> achillesDDLFilePaths = new ArrayList<>(ACHILLES_DDL_FILE_PATHS);

		Map<String, String> params = new HashMap<String, String>() {{
			put(VOCAB_SCHEMA, vocabSchema);
			put(RESULTS_SCHEMA, resultSchema);
		}};

		return generateSQL(dialect, params, achillesDDLFilePaths, Collections.emptyList(), Collections.emptyList());
	}

	private String generateSQL(String dialect, Map<String, String> params, Collection<String> filePaths, Collection<String> initFilePaths, Collection<String> indexFilePaths) {
		StringBuilder sqlBuilder = new StringBuilder();
		for (String fileName : filePaths) {
			sqlBuilder.append("\n").append(ResourceHelper.GetResourceAsString(fileName));
		}

		for (String fileName : initFilePaths) {
			sqlBuilder.append("\n").append(ResourceHelper.GetResourceAsString(fileName));
		}

		if (dialect == null || DBMS_NO_INDEXES.stream().noneMatch(dbms -> dbms.equals(dialect.toLowerCase()))) {
			for (String fileName : indexFilePaths) {
				sqlBuilder.append("\n").append(ResourceHelper.GetResourceAsString(fileName));
			}
		}
		String result = sqlBuilder.toString();
		if (dialect != null) {
			result = translateSqlFile(result, dialect, params);
		}
		return result.replaceAll(";", ";\n");
	}

	private String translateSqlFile(String sql, String dialect, Map<String, String> params) {

		SourceStatement statement = new SourceStatement();
		statement.setTargetDialect(dialect.toLowerCase()) ;
		statement.setOracleTempSchema(params.get(TEMP_SCHEMA));
		statement.setSql(sql);
		statement.getParameters().putAll(params);

		TranslatedStatement translatedStatement = translateSQL(statement);
		return translatedStatement.getTargetSQL();
	}

}
