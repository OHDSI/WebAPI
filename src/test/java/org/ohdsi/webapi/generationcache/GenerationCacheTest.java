package org.ohdsi.webapi.generationcache;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
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
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
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
import static org.ohdsi.webapi.Constants.Params.GENERATION_ID;
import static org.ohdsi.webapi.Constants.Params.RESULTS_DATABASE_SCHEMA;
import static org.ohdsi.webapi.Constants.Tables.COHORT_GENERATIONS_TABLE;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public class GenerationCacheTest {

    private static final String CDM_SQL = ResourceHelper.GetResourceAsString("/cdm-postgresql-ddl.sql");
    private static final String COHORT_JSON = ResourceHelper.GetResourceAsString("/generationcache/cohort/cohortIbuprofenOlder50.json");
    private static final String INSERT_COHORT_RESULTS_SQL = ResourceHelper.GetResourceAsString("/generationcache/cohort/insertCohortResults.sql");
    private static final String COHORT_COUNTS_SQL = ResourceHelper.GetResourceAsString("/generationcache/cohort/queryCohortCounts.sql");
    private static final Integer INITIAL_ENTITY_ID = 2;
    private static final String EUNOMIA_CSV_ZIP = "/eunomia.csv.zip";
    private static final String CDM_SCHEMA_NAME = "cdm";
    private static final String RESULT_SCHEMA_NAME = "results";

    @ClassRule
    public static SingleInstancePostgresRule pg = EmbeddedPostgresRules.singleInstance();

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

    private static JdbcTemplate jdbcTemplate;

    private static final Collection<String> COHORT_DDL_FILE_PATHS = Arrays.asList(
            "/ddl/results/cohort.sql",
            "/ddl/results/cohort_generations.sql",
            "/ddl/results/cohort_inclusion.sql",
            "/ddl/results/cohort_inclusion_result.sql",
            "/ddl/results/cohort_inclusion_stats.sql",
            "/ddl/results/cohort_summary_stats.sql",
            "/ddl/results/cohort_censor_stats.sql"
    );

    private CohortGenerationRequestBuilder cohortGenerationRequestBuilder;

    @BeforeClass
    public static void beforeClass() {

//        prepareCdmSchema();
        jdbcTemplate = new JdbcTemplate(getDataSource());
        try {
            System.setProperty("datasource.url", getDataSource().getConnection().getMetaData().getURL());
            System.setProperty("flyway.datasource.url", System.getProperty("datasource.url"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Before
    public void setUp() throws SQLException {

        prepareResultSchema();
        generationCacheRepository.deleteAll();

        if (cohortDefinitionRepository.findOne(INITIAL_ENTITY_ID) == null) {
            cohortDefinitionRepository.save(getCohortDefinition());
        }

        if (sourceRepository.findOne(INITIAL_ENTITY_ID) == null) {
            sourceRepository.saveAndFlush(getCdmSource());
        }

        cohortGenerationRequestBuilder = getCohortGenerationRequestBuilder(sourceRepository.findBySourceId(INITIAL_ENTITY_ID));
    }

    @Test
    public void generateCohort() {

        AtomicBoolean isSqlExecuted = new AtomicBoolean();
        CohortDefinition cohortDefinition = cohortDefinitionRepository.findOneWithDetail(INITIAL_ENTITY_ID);
        Source source = sourceRepository.findBySourceId(INITIAL_ENTITY_ID);

        // Run first-time generation

        GenerationCacheHelper.CacheResult res = generationCacheHelper.computeCacheIfAbsent(
                cohortDefinition,
                source,
                cohortGenerationRequestBuilder,
                (resId, sqls) -> executeCohort(isSqlExecuted, resId)
        );

        Assert.assertTrue("Cohort SQL is executed in case of empty cache", isSqlExecuted.get());

        Map<String, Long> counts = retrieveCohortGenerationCounts(res.getIdentifier());

        Assert.assertTrue("Cohort generation properly fills tables", checkCohortCounts(counts));

        // Second time generation. Cached results

        isSqlExecuted.set(false);

        res = generationCacheHelper.computeCacheIfAbsent(
                cohortDefinition,
                source,
                cohortGenerationRequestBuilder,
                (resId, sqls) -> isSqlExecuted.set(true)
        );

        Assert.assertFalse("Cohort results are retrieved from cache", isSqlExecuted.get());

        // Generation after results were corrupted

        jdbcTemplate.execute(String.format("DELETE FROM %s.cohort_generations;", RESULT_SCHEMA_NAME));

        isSqlExecuted.set(false);

        res = generationCacheHelper.computeCacheIfAbsent(
                cohortDefinition,
                source,
                cohortGenerationRequestBuilder,
                (resId, sqls) -> executeCohort(isSqlExecuted, resId)
        );

        Assert.assertTrue("Cohort SQL is executed in case of invalid cache", isSqlExecuted.get());

        counts = retrieveCohortGenerationCounts(res.getIdentifier());

        Assert.assertTrue("Cohort generation properly fills tables after invalid cache", checkCohortCounts(counts));
    }

    @Test
    public void checkCachingWithEmptyResultSet() {

        CacheableGenerationType type = CacheableGenerationType.COHORT;
        CohortDefinition cohortDefinition = cohortDefinitionRepository.findOneWithDetail(INITIAL_ENTITY_ID);
        Source source = sourceRepository.findBySourceId(INITIAL_ENTITY_ID);

        generationCacheHelper.computeCacheIfAbsent(
                cohortDefinition,
                source,
                cohortGenerationRequestBuilder,
                (resId, sqls) -> {}
        );

        Integer nextId = generationCacheService.getNextResultIdentifier(type, source);
        Assert.assertEquals("Generation cache sequence moves forward in case of empty result set", 2, (int) nextId);

        GenerationCache generationCache = generationCacheService.getCacheOrEraseInvalid(type, generationCacheService.getDesignHash(type, cohortDefinition.getDetails().getExpression()), source.getSourceId());
        Assert.assertNotNull("Empty result set is cached", generationCache);
    }

    @Test
    public void checkCachingWithPrefilledResults() {

        CacheableGenerationType type = CacheableGenerationType.COHORT;
        CohortDefinition cohortDefinition = cohortDefinitionRepository.findOneWithDetail(INITIAL_ENTITY_ID);
        Source source = sourceRepository.findBySourceId(INITIAL_ENTITY_ID);

        executeCohort(new AtomicBoolean(), 10);

        generationCacheHelper.computeCacheIfAbsent(
                cohortDefinition,
                source,
                cohortGenerationRequestBuilder,
                (resId, sqls) -> {}
        );

        GenerationCache generationCache = generationCacheService.getCacheOrEraseInvalid(type, generationCacheService.getDesignHash(type, cohortDefinition.getDetails().getExpression()), source.getSourceId());
        Assert.assertEquals("Generation sequence respects existing results", 11, (int) generationCache.getResultIdentifier());
    }

    private void executeCohort(AtomicBoolean isSqlExecuted, Integer resId) {

        String mockSqlList = SqlRender.renderSql(
                INSERT_COHORT_RESULTS_SQL,
                new String[]{RESULTS_DATABASE_SCHEMA, GENERATION_ID},
                new String[]{RESULT_SCHEMA_NAME, resId.toString()}
        );

        String[] mockSqls = SqlSplit.splitSql(mockSqlList);
        jdbcTemplate.batchUpdate(mockSqls);
        isSqlExecuted.set(true);
    }

    private Map<String, Long> retrieveCohortGenerationCounts(Integer generationId) {

        String cohortCountsSql = SqlRender.renderSql(
                COHORT_COUNTS_SQL,
                new String[]{RESULTS_DATABASE_SCHEMA, GENERATION_ID},
                new String[]{RESULT_SCHEMA_NAME, generationId.toString()}
        );
        return jdbcTemplate.queryForMap(cohortCountsSql)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> (Long) e.getValue()));
    }

    private boolean checkCohortCounts(Map<String, Long> counts) {

        return counts.get("cohort_generations_record_count") == 1
                && counts.get("cohort_inclusion_count") == 1
                && counts.get("cohort_inclusion_result_count") == 1
                && counts.get("cohort_inclusion_stats_count") == 1
                && counts.get("cohort_summary_stats_count") == 1;
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
        source.setSourceKey("Embedded_PG");
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
                SourceUtils.getResultsQualifier(source),
                COHORT_GENERATIONS_TABLE,
                GENERATION_ID,
                true
        );
    }

    // NOTE:
    // Not used in the current test set. Will be utilized for cohort generation testing
    private static void prepareCdmSchema() throws IOException, ZipException {

        DataSource dataSource = getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String cdmSql = getCdmSql();
        jdbcTemplate.batchUpdate("CREATE SCHEMA cdm;", cdmSql);
    }

    private static void prepareResultSchema() {

        DataSource dataSource = getDataSource();
        String resultSql = getResultTablesSql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.batchUpdate(SqlSplit.splitSql(resultSql));
    }

    private static DataSource getDataSource() {

        return pg.getEmbeddedPostgres().getPostgresDatabase();
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
                String sql = String.format("INSERT INTO @cdm_database_schema.%s SELECT * FROM CSVREAD('%s');", tableName, file.getAbsolutePath());
                cdmSqlBuilder.append(sql).append("\n\n");
            }
        }

        return cdmSqlBuilder.toString().replaceAll("@cdm_database_schema", CDM_SCHEMA_NAME);
    }

    private static String getResultTablesSql() {

        StringBuilder ddl = new StringBuilder();
        ddl.append(String.format("DROP SCHEMA IF EXISTS %s CASCADE;", RESULT_SCHEMA_NAME)).append("\n");
        ddl.append(String.format("CREATE SCHEMA %s;", RESULT_SCHEMA_NAME)).append("\n");
        COHORT_DDL_FILE_PATHS.forEach(sqlPath -> ddl.append(ResourceHelper.GetResourceAsString(sqlPath)).append("\n"));
        String resultSql = SqlRender.renderSql(ddl.toString(), new String[]{"results_schema"}, new String[]{RESULT_SCHEMA_NAME});
        return SqlTranslate.translateSql(resultSql, DBMSType.POSTGRESQL.getOhdsiDB());
    }
}
