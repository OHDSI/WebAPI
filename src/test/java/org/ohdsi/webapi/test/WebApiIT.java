package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;
import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DbUnitConfiguration(databaseConnection = {"primaryDataSource"})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, DbUnitTestExecutionListener.class, DirtiesContextTestExecutionListener.class})
public abstract class WebApiIT {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final String SOURCE_KEY = "Embedded_PG";
    protected static final String CDM_SCHEMA_NAME = "cdm";
    protected static final String RESULT_SCHEMA_NAME = "results";

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
            "/ddl/results/pathway_analysis_stats.sql"
    );


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

    protected void truncateTable(final String tableName) {
        jdbcTemplate.execute(String.format("TRUNCATE %s CASCADE",tableName));
    }
    protected void resetSequence(final String sequenceName) {
        jdbcTemplate.execute(String.format("ALTER SEQUENCE %s RESTART WITH 1", sequenceName));
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
}
