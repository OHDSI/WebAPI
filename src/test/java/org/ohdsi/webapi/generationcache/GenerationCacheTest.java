package org.ohdsi.webapi.generationcache;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationRequestBuilder;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.ohdsi.webapi.Constants.Params.DESIGN_HASH;
import static org.ohdsi.webapi.Constants.Params.RESULTS_DATABASE_SCHEMA;
import org.springframework.beans.factory.annotation.Value;


public class GenerationCacheTest extends AbstractDatabaseTest {

    private static final String CDM_SQL = ResourceHelper.GetResourceAsString("/cdm-postgresql-ddl.sql");
    private static final String COHORT_JSON = ResourceHelper.GetResourceAsString("/generationcache/cohort/cohortIbuprofenOlder50.json");
    private static final String INSERT_COHORT_RESULTS_SQL = ResourceHelper.GetResourceAsString("/generationcache/cohort/insertCohortResults.sql");
    private static final String COHORT_COUNTS_SQL = ResourceHelper.GetResourceAsString("/generationcache/cohort/queryCohortCounts.sql");
    private static final String EUNOMIA_CSV_ZIP = "/eunomia.csv.zip";
    private static final String CDM_SCHEMA_NAME = "cdm";
    private static final String RESULT_SCHEMA_NAME = "results";
    private static final String SOURCE_KEY = "Embedded_PG";
    private static boolean isSetup = false;
    private int cohortId;  // will be set to the test cohort for each test excution
    
    @Autowired
    private GenerationCacheService generationCacheService;

    @Autowired
    private GenerationCacheRepository generationCacheRepository;

    @Autowired
    private GenerationCacheHelper generationCacheHelper;

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;
    
    @Value("${datasource.ohdsi.schema}")
    private String ohdsiSchema;
  
    private static final Collection<String> COHORT_DDL_FILE_PATHS = Arrays.asList(
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
		"/ddl/results/cohort_summary_stats_cache.sql"
    );

    private CohortGenerationRequestBuilder cohortGenerationRequestBuilder;

    @BeforeEach
    public void setUp() throws SQLException {

        if (!isSetup) { // one-time setup per class here
          truncateTable("%s.%s".formatted(ohdsiSchema, "source"));
          resetSequence("%s.%s".formatted(ohdsiSchema, "source_sequence"));         
          sourceRepository.saveAndFlush(getCdmSource());
          isSetup = true;
        }

        // reset cohort tables
        truncateTable("%s.%s".formatted(ohdsiSchema, "cohort_definition_details"));
        truncateTable("%s.%s".formatted(ohdsiSchema, "cohort_definition"));
        resetSequence("%s.%s".formatted(ohdsiSchema, "cohort_definition_sequence"));
        
        cohortId = cohortDefinitionRepository.save(getCohortDefinition()).getId();
        cohortGenerationRequestBuilder = getCohortGenerationRequestBuilder(sourceRepository.findBySourceKey(SOURCE_KEY));
        generationCacheRepository.deleteAll();
        prepareResultSchema();
    }

    @Test
    public void generateCohort() {

        AtomicBoolean isSqlExecuted = new AtomicBoolean();
        CohortDefinition cohortDefinition = cohortDefinitionRepository.findOneWithDetail(cohortId);
        Source source = sourceRepository.findBySourceKey(SOURCE_KEY);

        // Run first-time generation

        GenerationCacheHelper.CacheResult res = generationCacheHelper.computeCacheIfAbsent(
                cohortDefinition,
                source,
                cohortGenerationRequestBuilder,
                (resId, sqls) -> executeCohort(isSqlExecuted, resId)
        );

        Assertions.assertTrue(isSqlExecuted.get(), "Cohort SQL is executed in case of empty cache");

        Map<String, Long> counts = retrieveCohortGenerationCounts(res.getIdentifier());

        Assertions.assertTrue(checkCohortCounts(counts), "Cohort generation properly fills tables");

        // Second time generation. Cached results

        isSqlExecuted.set(false);

        res = generationCacheHelper.computeCacheIfAbsent(
                cohortDefinition,
                source,
                cohortGenerationRequestBuilder,
                (resId, sqls) -> isSqlExecuted.set(true)
        );

        Assertions.assertFalse(isSqlExecuted.get(), "Cohort results are retrieved from cache");

        // Generation after results were corrupted

        jdbcTemplate.execute("DELETE FROM %s.cohort_cache;".formatted(RESULT_SCHEMA_NAME));

        isSqlExecuted.set(false);

        res = generationCacheHelper.computeCacheIfAbsent(
                cohortDefinition,
                source,
                cohortGenerationRequestBuilder,
                (resId, sqls) -> executeCohort(isSqlExecuted, resId)
        );

        Assertions.assertTrue(isSqlExecuted.get(), "Cohort SQL is executed in case of invalid cache");

        counts = retrieveCohortGenerationCounts(res.getIdentifier());

        Assertions.assertTrue(checkCohortCounts(counts), "Cohort generation properly fills tables after invalid cache");
    }

    @Test
    public void checkCachingWithEmptyResultSet() {

        CacheableGenerationType type = CacheableGenerationType.COHORT;
        CohortDefinition cohortDefinition = cohortDefinitionRepository.findOneWithDetail(cohortId);
        Source source = sourceRepository.findBySourceKey(SOURCE_KEY);

        generationCacheHelper.computeCacheIfAbsent(
                cohortDefinition,
                source,
                cohortGenerationRequestBuilder,
                (resId, sqls) -> {}
        );

        GenerationCache generationCache = generationCacheService.getCacheOrEraseInvalid(type, generationCacheService.getDesignHash(type, cohortDefinition.getDetails().getExpression()), source.getSourceId());
        Assertions.assertNotNull(generationCache, "Empty result set is cached");
    }

    @Test
    public void checkHashEquivalence() {

        CohortDefinition cohortDefinition = cohortDefinitionRepository.findOneWithDetail(cohortId);
        
        Integer originalHash = generationCacheHelper.computeHash(cohortDefinition.getDetails().getExpression());

        // modify the inclusion rule name/description, but should lead to same hash result
        CohortExpression expression = CohortExpression.fromJson(cohortDefinition.getDetails().getExpression());
        expression.inclusionRules.get(0).name += "...updated name";
        expression.inclusionRules.get(0).description += "..updated description";

        Integer updatedHash = generationCacheHelper.computeHash(Utils.serialize(expression));
        
        Assertions.assertEquals(originalHash,updatedHash,"Expression with different name and descritpion results in same hash");
    }

    private void executeCohort(AtomicBoolean isSqlExecuted, Integer resId) {

        String mockSqlList = SqlRender.renderSql(
                INSERT_COHORT_RESULTS_SQL,
                new String[]{RESULTS_DATABASE_SCHEMA, DESIGN_HASH},
                new String[]{RESULT_SCHEMA_NAME, resId.toString()}
        );

        String[] mockSqls = SqlSplit.splitSql(mockSqlList);
        jdbcTemplate.batchUpdate(mockSqls);
        isSqlExecuted.set(true);
    }

    private Map<String, Long> retrieveCohortGenerationCounts(Integer generationId) {

        String cohortCountsSql = SqlRender.renderSql(
                COHORT_COUNTS_SQL,
                new String[]{RESULTS_DATABASE_SCHEMA, DESIGN_HASH},
                new String[]{RESULT_SCHEMA_NAME, generationId.toString()}
        );
        return jdbcTemplate.queryForMap(cohortCountsSql)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Long) e.getValue()));
    }

    private boolean checkCohortCounts(Map<String, Long> counts) {

        return counts.get("cohort_record_count") == 1
                && counts.get("cohort_inclusion_result_count") == 1
                && counts.get("cohort_inclusion_stats_count") == 1
                && counts.get("cohort_summary_stats_count") == 1
                && counts.get("cohort_censor_stats_count") == 0;
    }

    private CohortDefinition getCohortDefinition() {

        CohortDefinitionDetails cohortDefinitionDetails = new CohortDefinitionDetails();
        cohortDefinitionDetails.setExpression(COHORT_JSON);

        CohortDefinition cohortDefinition = new CohortDefinition();
        cohortDefinition.setName("Unit test");
        cohortDefinition.setDetails(cohortDefinitionDetails);

        cohortDefinitionDetails.setCohortDefinition(cohortDefinition);

        return cohortDefinition;
    }

    private Source getCdmSource() throws SQLException {

        Source source = new Source();
        source.setSourceName("Embedded PG");
        source.setSourceKey(SOURCE_KEY);
        source.setSourceDialect(DBMSType.POSTGRESQL.getOhdsiDB());
        source.setSourceConnection(getDataSource().getConnection().getMetaData().getURL());
        source.setUsername("postgres");
        source.setPassword("postgres");
        source.setKrbAuthMethod(KerberosAuthMechanism.PASSWORD);

        SourceDaimon cdmDaimon = new SourceDaimon();
        cdmDaimon.setPriority(1);
        cdmDaimon.setDaimonType(SourceDaimon.DaimonType.CDM);
        cdmDaimon.setTableQualifier(CDM_SCHEMA_NAME);
        cdmDaimon.setSource(source);

        SourceDaimon vocabDaimon = new SourceDaimon();
        vocabDaimon.setPriority(1);
        vocabDaimon.setDaimonType(SourceDaimon.DaimonType.Vocabulary);
        vocabDaimon.setTableQualifier(CDM_SCHEMA_NAME);
        vocabDaimon.setSource(source);

        SourceDaimon resultsDaimon = new SourceDaimon();
        resultsDaimon.setPriority(1);
        resultsDaimon.setDaimonType(SourceDaimon.DaimonType.Results);
        resultsDaimon.setTableQualifier(RESULT_SCHEMA_NAME);
        resultsDaimon.setSource(source);

        source.setDaimons(Arrays.asList(cdmDaimon, vocabDaimon, resultsDaimon));

        return source;
    }

    private CohortGenerationRequestBuilder getCohortGenerationRequestBuilder(Source source) {

        return new CohortGenerationRequestBuilder(
                SessionUtils.sessionId(),
                SourceUtils.getResultsQualifier(source)
        );
    }

    // NOTE:
    // Not used in the current test set. Will be utilized for cohort generation testing
    private static void prepareCdmSchema() throws IOException, ZipException {

        String cdmSql = getCdmSql();
        jdbcTemplate.batchUpdate("CREATE SCHEMA cdm;", cdmSql);
    }

    private static void prepareResultSchema() {

        String resultSql = getResultTablesSql();
        jdbcTemplate.batchUpdate(SqlSplit.splitSql(resultSql));
    }

    private static String getCdmSql() throws IOException, ZipException {

        StringBuilder cdmSqlBuilder = new StringBuilder(CDM_SQL);

        cdmSqlBuilder.append("ALTER TABLE @cdm_database_schema.vocabulary ALTER COLUMN vocabulary_reference DROP NOT NULL;\n");
        cdmSqlBuilder.append("ALTER TABLE @cdm_database_schema.vocabulary ALTER COLUMN vocabulary_version DROP NOT NULL;\n");

        Path tempDir = Files.createTempDirectory("");
        tempDir.toFile().deleteOnExit();

        // Direct Files.copy into new file in the temp folder throws Access Denied
        File eunomiaZip = File.createTempFile("eunomia", "", tempDir.toFile());

        try (InputStream is = GenerationCacheTest.class.getResourceAsStream(EUNOMIA_CSV_ZIP)) {
            Files.copy(is, eunomiaZip.toPath(), REPLACE_EXISTING);
        }

        ZipFile zipFile = new ZipFile(eunomiaZip);
        zipFile.extractAll(tempDir.toAbsolutePath().toString());

        for (final File file : tempDir.toFile().listFiles()) {
            if (file.getName().endsWith(".csv")) {
                String tableName = file.getName().replace(".csv", "");
                String sql = "INSERT INTO @cdm_database_schema.%s SELECT * FROM CSVREAD('%s');".formatted(tableName, file.getAbsolutePath());
                cdmSqlBuilder.append(sql).append("\n\n");
            }
        }

        return cdmSqlBuilder.toString().replaceAll(Constants.SqlSchemaPlaceholders.CDM_DATABASE_SCHEMA_PLACEHOLDER, CDM_SCHEMA_NAME);
    }

    private static String getResultTablesSql() {

        StringBuilder ddl = new StringBuilder();
        ddl.append("DROP SCHEMA IF EXISTS %s CASCADE;".formatted(RESULT_SCHEMA_NAME)).append("\n");
        ddl.append("CREATE SCHEMA %s;".formatted(RESULT_SCHEMA_NAME)).append("\n");
        COHORT_DDL_FILE_PATHS.forEach(sqlPath -> ddl.append(ResourceHelper.GetResourceAsString(sqlPath)).append("\n"));
        String resultSql = SqlRender.renderSql(ddl.toString(), new String[]{"results_schema"}, new String[]{RESULT_SCHEMA_NAME});
        return SqlTranslate.translateSql(resultSql, DBMSType.POSTGRESQL.getOhdsiDB());
    }
}
