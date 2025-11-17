package org.ohdsi.webapi.mvc.controller;

import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.webapi.common.DBMSType;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.service.SqlRenderService;
import org.ohdsi.webapi.sqlrender.SourceStatement;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * Spring MVC version of DDLService
 *
 * Migration Status: Replaces /service/DDLService.java (Jersey)
 * Endpoints: 3 GET endpoints with query parameters
 * Complexity: Simple - GET operations generating DDL SQL
 */
@RestController
@RequestMapping("/ddl")
public class DDLMvcController extends AbstractMvcController {

    public static final String VOCAB_SCHEMA = "vocab_schema";
    public static final String RESULTS_SCHEMA = "results_schema";
    public static final String CEM_SCHEMA = "cem_results_schema";
    public static final String TEMP_SCHEMA = "oracle_temp_schema";

    private static final Collection<String> RESULT_DDL_FILE_PATHS = Arrays.asList(
        "/ddl/results/cohort.sql",
        "/ddl/results/cohort_censor_stats.sql",
        "/ddl/results/cohort_inclusion.sql",
        "/ddl/results/cohort_inclusion_result.sql",
        "/ddl/results/cohort_inclusion_stats.sql",
        "/ddl/results/cohort_summary_stats.sql",
        "/ddl/results/cohort_cache.sql",
        "/ddl/results/cohort_censor_stats_cache.sql",
        "/ddl/results/cohort_inclusion_result_cache.sql",
        "/ddl/results/cohort_inclusion_stats_cache.sql",
        "/ddl/results/cohort_summary_stats_cache.sql",
        "/ddl/results/feas_study_inclusion_stats.sql",
        "/ddl/results/feas_study_index_stats.sql",
        "/ddl/results/feas_study_result.sql",
        "/ddl/results/heracles_analysis.sql",
        "/ddl/results/heracles_heel_results.sql",
        "/ddl/results/heracles_results.sql",
        "/ddl/results/heracles_results_dist.sql",
        "/ddl/results/heracles_periods.sql",
        "/ddl/results/cohort_sample_element.sql",
        "/ddl/results/ir_analysis_dist.sql",
        "/ddl/results/ir_analysis_result.sql",
        "/ddl/results/ir_analysis_strata_stats.sql",
        "/ddl/results/ir_strata.sql",
        "/ddl/results/cohort_characterizations.sql",
        "/ddl/results/pathway_analysis_codes.sql",
        "/ddl/results/pathway_analysis_events.sql",
        "/ddl/results/pathway_analysis_paths.sql",
        "/ddl/results/pathway_analysis_stats.sql"
    );

    private static final String INIT_HERACLES_PERIODS = "/ddl/results/init_heracles_periods.sql";

    public static final Collection<String> RESULT_INIT_FILE_PATHS = Arrays.asList(
            "/ddl/results/init_heracles_analysis.sql", INIT_HERACLES_PERIODS
    );

    public static final Collection<String> HIVE_RESULT_INIT_FILE_PATHS = Arrays.asList(
            "/ddl/results/init_hive_heracles_analysis.sql", INIT_HERACLES_PERIODS
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

    public static final Collection<String> CEMRESULT_INIT_FILE_PATHS = Collections.emptyList();
    private static final Collection<String> CEMRESULT_INDEX_FILE_PATHS = Collections.emptyList();

    private static final Collection<String> ACHILLES_DDL_FILE_PATHS = Arrays.asList(
            "/ddl/achilles/achilles_result_concept_count.sql"
    );

    private static final Collection<String> DBMS_NO_INDEXES = Arrays.asList("redshift", "impala", "netezza", "spark");

    /**
     * Get DDL for results schema
     *
     * Jersey: GET /WebAPI/ddl/results
     * Spring MVC: GET /WebAPI/v2/ddl/results
     */
    @GetMapping(value = "/results", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> generateResultSQL(
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
        Map<String, String> params = new HashMap<>() {{
            put(VOCAB_SCHEMA, vocabSchema);
            put(RESULTS_SCHEMA, resultSchema);
            put(TEMP_SCHEMA, oracleTempSchema);
        }};

        String sql = generateSQL(dialect, params, resultDDLFilePaths, getResultInitFilePaths(dialect), RESULT_INDEX_FILE_PATHS);
        return ok(sql);
    }

    /**
     * Get DDL for Common Evidence Model results schema
     *
     * Jersey: GET /WebAPI/ddl/cemresults
     * Spring MVC: GET /WebAPI/v2/ddl/cemresults
     */
    @GetMapping(value = "/cemresults", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> generateCemResultSQL(
            @RequestParam(value = "dialect", required = false) String dialect,
            @RequestParam(value = "schema", defaultValue = "cemresults") String schema) {

        Map<String, String> params = new HashMap<>() {{
            put(CEM_SCHEMA, schema);
        }};

        String sql = generateSQL(dialect, params, CEMRESULT_DDL_FILE_PATHS, CEMRESULT_INIT_FILE_PATHS, CEMRESULT_INDEX_FILE_PATHS);
        return ok(sql);
    }

    /**
     * Get DDL for Achilles results tables
     *
     * Jersey: GET /WebAPI/ddl/achilles
     * Spring MVC: GET /WebAPI/v2/ddl/achilles
     */
    @GetMapping(value = "/achilles", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> generateAchillesSQL(
            @RequestParam(value = "dialect", required = false) String dialect,
            @RequestParam(value = "vocabSchema", defaultValue = "vocab") String vocabSchema,
            @RequestParam(value = "schema", defaultValue = "results") String resultSchema) {

        final Collection<String> achillesDDLFilePaths = new ArrayList<>(ACHILLES_DDL_FILE_PATHS);

        Map<String, String> params = new HashMap<>() {{
            put(VOCAB_SCHEMA, vocabSchema);
            put(RESULTS_SCHEMA, resultSchema);
        }};

        String sql = generateSQL(dialect, params, achillesDDLFilePaths, Collections.emptyList(), Collections.emptyList());
        return ok(sql);
    }

    // Helper methods (same logic as original)

    private Collection<String> getResultInitFilePaths(String dialect) {
        if (Objects.equals(DBMSType.HIVE.getOhdsiDB(), dialect)) {
            return HIVE_RESULT_INIT_FILE_PATHS;
        } else {
            return RESULT_INIT_FILE_PATHS;
        }
    }

    private String generateSQL(String dialect, Map<String, String> params, Collection<String> filePaths,
                               Collection<String> initFilePaths, Collection<String> indexFilePaths) {
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
        statement.setTargetDialect(dialect.toLowerCase());
        statement.setOracleTempSchema(params.get(TEMP_SCHEMA));
        statement.setSql(sql);
        statement.getParameters().putAll(params);

        TranslatedStatement translatedStatement = SqlRenderService.translateSQL(statement);
        return translatedStatement.getTargetSQL();
    }
}
