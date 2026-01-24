package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.context.TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.bean.DatabaseConfigBean;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.flywaydb.core.Flyway;
import org.ohdsi.webapi.common.DBMSType;
import org.ohdsi.webapi.arachne.datasource.dto.KerberosAuthMechanism;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;

import javax.sql.DataSource;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {WebApi.class, WebApiIT.DbUnitConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DbUnitConfiguration(databaseConnection = "dbUnitDatabaseConnection")
@TestExecutionListeners(value = {DbUnitTestExecutionListener.class}, mergeMode = MERGE_WITH_DEFAULTS)
public abstract class WebApiIT {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final String SOURCE_KEY = "Embedded_PG";
    protected static final String CDM_SCHEMA_NAME = "cdm";
    protected static final String RESULT_SCHEMA_NAME = "results";

    @Value("${datasource.ohdsi.schema:public}")
    private String ohdsiSchema;

    @Value("${spring.flyway.locations:classpath:db/migration/postgresql}")
    private String flywayLocations;

    @Value("${spring.flyway.table:schema_version}")
    private String flywayTable;

    @Value("${spring.flyway.baseline-on-migrate:true}")
    private boolean flywayBaselineOnMigrate;

    @Value("${spring.flyway.out-of-order:true}")
    private boolean flywayOutOfOrder;

    private static final AtomicBoolean OHDSI_SCHEMA_INITIALIZED = new AtomicBoolean(false);
    private static final Object OHDSI_SCHEMA_LOCK = new Object();

    private static final Collection<String> CDM_DDL_FILE_PATHS = Arrays.asList("/cdm-postgresql-ddl.sql");
    private static final Collection<String> RESULTS_DDL_FILE_PATHS = Arrays.asList(
            "/ddl/results/cohort.sql",
            "/ddl/results/cohort_cache.sql",
            "/ddl/results/cohort_inclusion.sql",
            "/ddl/results/cohort_inclusion_result.sql",
            "/ddl/results/cohort_inclusion_stats.sql",
            "/ddl/results/cohort_inclusion_result_cache.sql",
            "/ddl/results/cohort_inclusion_stats_cache.sql",
            "/ddl/results/cohort_summary_stats.sql",
            "/ddl/results/cohort_summary_stats_cache.sql",
            "/ddl/results/cohort_censor_stats.sql",
            "/ddl/results/cohort_censor_stats_cache.sql",
            "/ddl/results/pathway_analysis_codes.sql",
            "/ddl/results/pathway_analysis_events.sql",
            "/ddl/results/pathway_analysis_paths.sql",
            "/ddl/results/pathway_analysis_stats.sql",
            "/ddl/results/achilles_result_concept_count.sql"
    );

		@TestConfiguration
		public static class DbUnitConfiguration {
            @Bean
            DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection(DatabaseConfigBean dbUnitDatabaseConfig,
                                                                             @Value("${datasource.ohdsi.schema:public}") String ohdsiSchema) {
				// Use the embedded PostgreSQL datasource from ITStarter
				DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection = new DatabaseDataSourceConnectionFactoryBean(ITStarter.getDataSource());
                dbUnitDatabaseConnection.setSchema(ohdsiSchema);
                dbUnitDatabaseConnection.setDatabaseConfig(dbUnitDatabaseConfig);
				return dbUnitDatabaseConnection;
			}

            @Bean
            DatabaseConfigBean dbUnitDatabaseConfig() {
                DatabaseConfigBean config = new DatabaseConfigBean();
                config.setDatatypeFactory(new PostgresqlDataTypeFactory());
                return config;
            }
		}

    @Value("${baseUri}")
    private String baseUri;

    private final TestRestTemplate restTemplate = new TestRestTemplate();

    protected static JdbcTemplate jdbcTemplate;

    @BeforeClass
    public static void before() throws IOException {
        TomcatURLStreamHandlerFactory.disable();
        ITStarter.before();
        jdbcTemplate = new JdbcTemplate(ITStarter.getDataSource());
    }

    @Before
    public void ensureOhdsiSchemaInitialized() {
        if (OHDSI_SCHEMA_INITIALIZED.get()) {
            return;
        }
        synchronized (OHDSI_SCHEMA_LOCK) {
            if (OHDSI_SCHEMA_INITIALIZED.get()) {
                return;
            }
            initializeOhdsiSchemaIfNeeded();
            OHDSI_SCHEMA_INITIALIZED.set(true);
        }
    }

    @AfterClass
    public static void after() {
        ITStarter.tearDownSubject();
    }

    public TestRestTemplate getRestTemplate() {

        return this.restTemplate;
    }

    public String getBaseUri() {

        return this.baseUri;
    }

    public void setBaseUri(final String baseUri) {

        this.baseUri = baseUri;
    }

    public void assertOK(ResponseEntity<?> entity) {

        assertEquals(HttpStatus.OK, entity.getStatusCode());
        if (log.isDebugEnabled()) {
            log.debug("Body: {}", entity.getBody());
        }
    }

    protected String getOhdsiSchema() {
        return ohdsiSchema;
    }

    protected String qualifyOhdsiTable(String tableName) {
        return String.format("%s.%s", ohdsiSchema, tableName);
    }

    protected void truncateTable(final String tableName) {
        String qualifiedName = tableName.contains(".") ? tableName : String.format("%s.%s", ohdsiSchema, tableName);
        jdbcTemplate.execute(String.format("TRUNCATE %s CASCADE", qualifiedName));
    }

    protected void resetSequence(final String sequenceName) {
        String qualifiedName = sequenceName.contains(".") ? sequenceName : String.format("%s.%s", ohdsiSchema, sequenceName);
        jdbcTemplate.execute(String.format("ALTER SEQUENCE %s RESTART WITH 1", qualifiedName));
    }

    protected Source getCdmSource() throws SQLException {
        Source source = new Source();
        source.setSourceName("Embedded PG");
        source.setSourceKey(SOURCE_KEY);
        source.setSourceDialect(DBMSType.POSTGRESQL.getOhdsiDB());
        source.setSourceConnection(ITStarter.getDataSource().getConnection().getMetaData().getURL());
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

    protected void prepareResultSchema() {
        prepareSchema(RESULT_SCHEMA_NAME, "results_schema", RESULTS_DDL_FILE_PATHS);
    }

    protected void prepareCdmSchema() {
        prepareSchema(CDM_SCHEMA_NAME, "cdm_database_schema", CDM_DDL_FILE_PATHS);
    }

    private void prepareSchema(final String schemaName, final String schemaToken, final Collection<String> schemaPaths) {
        StringBuilder ddl = new StringBuilder();

        ddl.append(String.format("DROP SCHEMA IF EXISTS %s CASCADE;", schemaName));
        ddl.append(String.format("CREATE SCHEMA %s;", schemaName));
        schemaPaths.forEach(sqlPath -> ddl.append(ResourceHelper.GetResourceAsString(sqlPath)).append("\n"));
        String resultSql = SqlRender.renderSql(ddl.toString(), new String[]{schemaToken}, new String[]{schemaName});
        String ddlSql = SqlTranslate.translateSql(resultSql, DBMSType.POSTGRESQL.getOhdsiDB());
        jdbcTemplate.batchUpdate(SqlSplit.splitSql(ddlSql));
    }

    private void initializeOhdsiSchemaIfNeeded() {
        if (tableExists(ohdsiSchema, "source")) {
            return;
        }
        runFlywayMigrationsWithPrefix("B");
        runFlywayMigrationsWithPrefix("V");
    }

    private void runFlywayMigrationsWithPrefix(String migrationPrefix) {
        Map<String, String> placeholders = Collections.singletonMap("ohdsiSchema", ohdsiSchema);
        Flyway.configure()
                .dataSource(ITStarter.getDataSource())
                .locations(resolveFlywayLocations())
                .schemas(ohdsiSchema)
                .table(flywayTable)
                .baselineOnMigrate(flywayBaselineOnMigrate)
                .outOfOrder(flywayOutOfOrder)
                .placeholders(placeholders)
                .sqlMigrationPrefix(migrationPrefix)
                .load()
                .migrate();
    }

    private String[] resolveFlywayLocations() {
        return Arrays.stream(flywayLocations.split(","))
                .map(String::trim)
                .filter(location -> !location.isEmpty())
                .toArray(String[]::new);
    }

    private boolean tableExists(String schema, String tableName) {
        String sql = "SELECT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_schema = ? AND table_name = ?)";
        Boolean exists = jdbcTemplate.queryForObject(sql, Boolean.class, schema, tableName);
        return Boolean.TRUE.equals(exists);
    }
}
