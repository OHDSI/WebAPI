package org.ohdsi.webapi.cohortcharacterization;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;
import com.opentable.db.postgres.junit.EmbeddedPostgresRules;
import com.opentable.db.postgres.junit.SingleInstancePostgresRule;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcharacterization.converter.SerializedCcToCcConverter;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.ExportExecutionResultRequest;
import org.ohdsi.webapi.generationcache.GenerationCacheTest;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@SpringBootTest
@RunWith(SpringRunner.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public class CohortCharacterizationServiceTest {
    private static final String CDM_SQL = ResourceHelper.GetResourceAsString("/cdm-postgresql-ddl.sql");
    private static final String PARAM_JSON = ResourceHelper.GetResourceAsString("/cohortcharacterization/reportData.json");
    private static final String PARAM_JSON_WITH_STRATA = ResourceHelper.GetResourceAsString("/cohortcharacterization/reportDataWithStrata.json");
    private static final String CC_JSON = ResourceHelper.GetResourceAsString("/cohortcharacterization/ibuprofenVsAspirin.json");
    private static final String CC_WITH_STRATA_JSON = ResourceHelper.GetResourceAsString("/cohortcharacterization/ibuprofenVsAspirinWithStrata.json");
    private static final String RESULT_SCHEMA_NAME = "results";
    private static final String CDM_SCHEMA_NAME = "cdm";
    private static final String SOURCE_KEY = "Embedded_PG";
    private static final Integer INITIAL_ENTITY_ID = 2;
    private static final String EUNOMIA_CSV_ZIP = "/eunomia.csv.zip";

    @Autowired
    private CcService ccService;

    @Autowired
    private CcController ccController;

    @Autowired
    private SourceRepository sourceRepository;

    @ClassRule
    public static SingleInstancePostgresRule pg = EmbeddedPostgresRules.singleInstance();

    private static JdbcTemplate jdbcTemplate;

    private static final Collection<String> COHORT_DDL_FILE_PATHS = Arrays.asList(
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
            "/ddl/results/cohort_characterizations.sql"
    );

    @BeforeClass
    public static void beforeClass() {
        jdbcTemplate = new JdbcTemplate(getDataSource());
        try {
            System.setProperty("datasource.url", getDataSource().getConnection().getMetaData().getURL());
            System.setProperty("flyway.datasource.url", System.getProperty("datasource.url"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Before
    public void setUp() throws Exception {
        prepareResultSchema();

        if (sourceRepository.findOne(INITIAL_ENTITY_ID) == null) {
            sourceRepository.saveAndFlush(getCdmSource());
        }

        prepareCdmSchema();
    }

    @Test
    public void testExportGeneration() throws Exception {
        doTestExportGeneration(CC_JSON, PARAM_JSON);
    }

    @Test
    public void testExportGenerationWithStrata() throws Exception {
        doTestExportGeneration(CC_WITH_STRATA_JSON, PARAM_JSON_WITH_STRATA);
    }

    public void doTestExportGeneration(String entityStr, String paramData) throws Exception {
        CohortCharacterizationEntity entity = new SerializedCcToCcConverter().convertToEntityAttribute(entityStr);
        entity = ccService.importCc(entity);
        JobExecutionResource executionResource = ccService.generateCc(entity.getId(), SOURCE_KEY);
        CcGenerationEntity generationEntity;
        while (true) {
            generationEntity = ccService.findGenerationById(executionResource.getExecutionId());
            if (generationEntity.getStatus().equals("FAILED") || generationEntity.getStatus().equals("COMPLETED")) {
                break;
            }
            Thread.sleep(2000L);
        }
        assertEquals("COMPLETED", generationEntity.getStatus());

        TypeReference<Data> typeRef = new TypeReference<Data>() {
        };
        Data data = Utils.deserialize(paramData, typeRef);

        for (ParamItem paramItem : data.paramItems) {
            checkRequest(generationEntity.getId(), paramItem);
        }
    }

    private void checkRequest(Long id, ParamItem paramItem) throws IOException {
        String dataItemMessage = String.format("Checking dataitem %s", paramItem.toString());
        ZipFile zipFile = getZipFile(id, paramItem);
        if (paramItem.fileItems.isEmpty()) {
            // File is empty
            assertFalse(dataItemMessage, zipFile.isValidZipFile());
        } else {
            assertTrue(dataItemMessage, zipFile.isValidZipFile());

            Path tempDir = Files.createTempDirectory(String.valueOf(System.currentTimeMillis()));
            tempDir.toFile().deleteOnExit();
            zipFile.extractAll(tempDir.toAbsolutePath().toString());
            assertEquals(dataItemMessage, paramItem.fileItems.size(), tempDir.toFile().listFiles().length);

            for (File file : tempDir.toFile().listFiles()) {
                String fileMessage = String.format("Checking filename %s for dataitem %s", file.getName(), paramItem.toString());
                Optional<FileItem> fileItem = paramItem.fileItems.stream()
                        .filter(f -> f.fileName.equals(file.getName()))
                        .findAny();
                assertTrue(fileMessage, fileItem.isPresent());

                long count = Files.lines(file.toPath()).count();
                // include header line
                assertEquals(fileMessage, fileItem.get().lineCount + 1, count);
            }
        }
    }

    private ZipFile getZipFile(Long id, ParamItem paramItem) throws IOException {
        ExportExecutionResultRequest request = new ExportExecutionResultRequest();
        request.setCohortIds(paramItem.cohortIds);
        request.setAnalysisIds(paramItem.analysisIds);
        request.setDomainIds(paramItem.domainIds);
        request.setSummary(paramItem.isSummary);
        request.setComparative(paramItem.isComparative);

        Response response = ccController.exportGenerationsResults(id, request);
        assertEquals(200, response.getStatus());

        ByteArrayOutputStream baos = (ByteArrayOutputStream) response.getEntity();
        File tempFile = File.createTempFile("reports", ".zip");
        tempFile.deleteOnExit();
        try (OutputStream outputStream = new FileOutputStream(tempFile)) {
            baos.writeTo(outputStream);
        }
        return new ZipFile(tempFile);
    }

    private static DataSource getDataSource() {
        return pg.getEmbeddedPostgres().getPostgresDatabase();
    }

    private static void prepareResultSchema() {
        DataSource dataSource = getDataSource();
        String resultSql = getResultTablesSql();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.batchUpdate(SqlSplit.splitSql(resultSql));
    }

    private static void prepareCdmSchema() throws Exception {
        DataSource dataSource = getDataSource();
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
        String cdmSql = getCdmSql();
        jdbcTemplate.batchUpdate(SqlSplit.splitSql(cdmSql));
    }

    private static String getResultTablesSql() {
        StringBuilder ddl = new StringBuilder();
        ddl.append(String.format("DROP SCHEMA IF EXISTS %s CASCADE;", RESULT_SCHEMA_NAME)).append("\n");
        ddl.append(String.format("CREATE SCHEMA %s;", RESULT_SCHEMA_NAME)).append("\n");
        COHORT_DDL_FILE_PATHS.forEach(sqlPath -> ddl.append(ResourceHelper.GetResourceAsString(sqlPath)).append("\n"));
        String resultSql = SqlRender.renderSql(ddl.toString(), new String[]{"results_schema"}, new String[]{RESULT_SCHEMA_NAME});
        return SqlTranslate.translateSql(resultSql, DBMSType.POSTGRESQL.getOhdsiDB());
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

    private static String getCdmSql() throws IOException, ZipException {

        StringBuilder cdmSqlBuilder = new StringBuilder(
                "DROP SCHEMA IF EXISTS @cdm_database_schema CASCADE; CREATE SCHEMA @cdm_database_schema;");
        cdmSqlBuilder.append(CDM_SQL);

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
                String sql = String.format("COPY @cdm_database_schema.%s FROM '%s' DELIMITER ',' CSV HEADER;", tableName, file.getAbsolutePath());
                cdmSqlBuilder.append(sql).append("\n\n");
            }
        }

        return cdmSqlBuilder.toString().replaceAll("@cdm_database_schema", CDM_SCHEMA_NAME);
    }

    public static class Data {
        @JsonProperty("Params")
        public List<ParamItem> paramItems;
    }

    public static class ParamItem {
        @JsonProperty("analysisIds")
        public List<Integer> analysisIds;
        @JsonProperty("cohortIds")
        public List<Integer> cohortIds;
        @JsonProperty("domainIds")
        public List<String> domainIds;
        @JsonProperty("isSummary")
        public Boolean isSummary;
        @JsonProperty("fileDatas")
        public List<FileItem> fileItems;
        @JsonProperty("isComparative")
        public Boolean isComparative;

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .append("analysisIds", analysisIds)
                    .append("cohortIds", cohortIds)
                    .append("isSummary", isSummary)
                    .append("isComparative", isComparative)
                    .toString();
        }
    }

    public static class FileItem {
        @JsonProperty("fileName")
        public String fileName;
        @JsonProperty("lineCount")
        public Integer lineCount;
    }

}
