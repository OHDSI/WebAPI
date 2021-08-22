package org.ohdsi.webapi.entity;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.KerberosAuthMechanism;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.AbstractDatabaseTest;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.EstimationController;
import org.ohdsi.webapi.estimation.EstimationService;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.repository.EstimationRepository;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.analysis.estimation.design.EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

public class EstimationEntityTest extends AbstractDatabaseTest implements TestCreate, TestCopy<EstimationDTO>, TestImport<EstimationDTO, EstimationAnalysisImpl> {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected EstimationController pleController;
    @Autowired
    protected EstimationRepository pleRepository;
    @Autowired
    protected EstimationService pleService;
    @Autowired
    protected CohortDefinitionDetailsRepository cdRepository;
    @Autowired
    private SourceRepository sourceRepository;

    private EstimationDTO firstSavedDTO;
    private static String PLE_SPECIFICATION;

    @BeforeClass
    public static void setPleSpecification() throws IOException {

        File ple_spec = new File("src/test/resources/ple-specification.json");
        PLE_SPECIFICATION = FileUtils.readFileToString(ple_spec, StandardCharsets.UTF_8);
    }

    // in JUnit 4 it's impossible to mark methods inside interface with annotations, it was implemented in JUnit 5. After upgrade it's needed
    // to mark interface methods with @Test, @Before, @After and to remove them from this class
    @After
    @Override
    public void tearDownDB() {

        pleRepository.deleteAll();
    }

    @Before
    @Override
    public void init() throws Exception {
        TestCreate.super.init();
        truncateTable(String.format("%s.%s", "public", "source"));
        resetSequence(String.format("%s.%s", "public", "source_sequence"));
        sourceRepository.saveAndFlush(getCdmSource());
    }

    @Test
    @Override
    public void shouldNotCreateEntityWithDuplicateName() {

        TestCreate.super.shouldNotCreateEntityWithDuplicateName();
    }

    @Test
    @Override
    public void shouldCopyWithUniqueName() throws Exception {

        TestCopy.super.shouldCopyWithUniqueName();
    }

    @Test
    @Override
    public void shouldCopyFromCopy() throws Exception {

        TestCopy.super.shouldCopyFromCopy();
    }

    @Test
    @Override
    public void shouldCopySeveralTimesOriginal() throws Exception {

        TestCopy.super.shouldCopySeveralTimesOriginal();
    }

    @Test
    @Override
    public void shouldCopyOfPartlySameName() throws Exception {

        TestCopy.super.shouldCopyOfPartlySameName();
    }

    @Test
    @Override
    public void shouldImportUniqueName() throws Exception {

        TestImport.super.shouldImportUniqueName();
    }

    @Test
    @Override
    public void shouldImportWithTheSameName() throws Exception {

        TestImport.super.shouldImportWithTheSameName();
    }

    @Test
    @Override
    public void shouldImportWhenEntityWithNameExists() throws Exception {

        TestImport.super.shouldImportWhenEntityWithNameExists();
    }

    @Test
    public void shouldImportWhenHashcodesOfCDsAndCSsAreDifferent() throws Exception {

        //Arrange
        File pleFile = new File("src/test/resources/ple-example-for-import.json");
        String pleStr = FileUtils.readFileToString(pleFile, StandardCharsets.UTF_8);

        EstimationAnalysisImpl ple = Utils.deserialize(pleStr, EstimationAnalysisImpl.class);

        //Action
        pleController.importAnalysis(ple);

        cdRepository.findAll().forEach(cd -> {
            cd.setExpression(cd.getExpression().replaceAll("5.0.0", "6.0.0"));
            cdRepository.save(cd);
        });
        EstimationDTO importedEs = pleController.importAnalysis(ple);

        //Assert
        assertEquals("Comparative effectiveness of ACE inhibitors vs Thiazide diuretics as first-line monotherapy for hypertension (1)",
                pleController.getAnalysis(importedEs.getId()).getName());
        EstimationAnalysisImpl importedExpression = pleService.getAnalysisExpression(importedEs.getId());
        List<AnalysisCohortDefinition> cds = importedExpression.getCohortDefinitions();
        assertEquals("New users of ACE inhibitors as first-line monotherapy for hypertension (1)", cds.get(0).getName());
        assertEquals("New users of Thiazide-like diuretics as first-line monotherapy for hypertension (1)", cds.get(1).getName());
        assertEquals("Acute myocardial infarction events (1)", cds.get(2).getName());
        assertEquals("Angioedema events (1)", cds.get(3).getName());
    }

    @Override
    public EstimationDTO createCopy(EstimationDTO dto) throws Exception {

        return pleController.copy(dto.getId());
    }

    @Override
    public void initFirstDTO() throws Exception {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    public EstimationDTO getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    public EstimationDTO createEntity(String name) throws Exception {

        return createEntity(createAndInitIncomingEntity(name));
    }

    @Override
    public EstimationDTO createEntity(EstimationDTO dto) throws Exception {

        Estimation estimation = createEstimation(dto.getName());
        return pleController.createEstimation(estimation);
    }

    @Override
    public EstimationDTO createAndInitIncomingEntity(String name) {

        Estimation estimation = createEstimation(name);
        return conversionService.convert(estimation, EstimationDTO.class);
    }

    private Estimation createEstimation(String name) {

        Estimation estimation = new Estimation();
        estimation.setName(name);
        estimation.setType(COMPARATIVE_COHORT_ANALYSIS);
        estimation.setSpecification(PLE_SPECIFICATION);
        return estimation;
    }

    @Override
    public String getConstraintName() {

        return "uq_es_name";
    }

    @Override
    public CommonEntity getEntity(int id) {

        return pleService.getAnalysis(id);
    }

    @Override
    public EstimationAnalysisImpl getExportEntity(CommonEntity entity) {

        return pleController.exportAnalysis(entity.getId().intValue());
    }

    @Override
    public EstimationDTO doImport(EstimationAnalysisImpl dto) throws Exception {

        return pleController.importAnalysis(dto);
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
        cdmDaimon.setTableQualifier("cdm");
        cdmDaimon.setSource(source);

        SourceDaimon vocabDaimon = new SourceDaimon();
        vocabDaimon.setPriority(1);
        vocabDaimon.setDaimonType(SourceDaimon.DaimonType.Vocabulary);
        vocabDaimon.setTableQualifier("cdm");
        vocabDaimon.setSource(source);

        SourceDaimon resultsDaimon = new SourceDaimon();
        resultsDaimon.setPriority(1);
        resultsDaimon.setDaimonType(SourceDaimon.DaimonType.Results);
        resultsDaimon.setTableQualifier("results");
        resultsDaimon.setSource(source);

        source.setDaimons(Arrays.asList(cdmDaimon, vocabDaimon, resultsDaimon));

        return source;
    }
}
