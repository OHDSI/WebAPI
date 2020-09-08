package org.ohdsi.webapi.test.entity;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.EstimationController;
import org.ohdsi.webapi.estimation.EstimationService;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.repository.EstimationRepository;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.test.ITStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;

import static org.junit.Assert.assertEquals;
import static org.ohdsi.analysis.estimation.design.EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest(classes = WebApi.class)
public class EstimationEntityTest extends ITStarter implements TestCreate, TestCopy<EstimationDTO>, TestImport<EstimationDTO,EstimationAnalysisImpl> {
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
    @Parameters({
            "abcde, abc, abc", "abcde (1), abcde, abcde (2)"
    })
    @Override
    public void shouldCopyOfPartlySameName(String firstName, String secondName, String assertionName) throws Exception {

        TestCopy.super.shouldCopyOfPartlySameName(firstName, secondName, assertionName);
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
}
