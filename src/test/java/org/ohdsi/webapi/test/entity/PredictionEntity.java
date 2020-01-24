package org.ohdsi.webapi.test.entity;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.PredictionController;
import org.ohdsi.webapi.prediction.PredictionService;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.repository.PredictionAnalysisRepository;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public class PredictionEntity implements TestCreate, TestCopy, TestImport {
    @Autowired
    protected PredictionController plpController;
    @Autowired
    protected PredictionAnalysisRepository plpRepository;
    @Autowired
    protected PredictionService plpService;
    private PredictionAnalysisDTO firstSavedDTO;
    private static String PLP_SPECIFICATION;

    @BeforeClass
    public static void setPlpSpecification() throws IOException {

        File plp_spec = new File("src/test/resources/plp-specification.json");
        PLP_SPECIFICATION = FileUtils.readFileToString(plp_spec, StandardCharsets.UTF_8);
    }

    // in JUnit 4 it's impossible to mark methods inside interface with annotations, it was implemented in JUnit 5. After upgrade it's needed
    // to mark interface methods with @Test, @Before, @After and to remove them from this class
    @After
    @Override
    public void tearDownDB() {

        plpRepository.deleteAll();
    }

    @Before
    @Override
    public void init() throws Exception {

        TestCreate.super.init();
    }

    //region test methods
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
    //endregion

    @Override
    public Object createCopy(Object dto) {

        return plpController.copy(((PredictionAnalysisDTO) dto).getId());
    }

    @Override
    public String getDtoName(Object dto) {

        return ((PredictionAnalysisDTO) dto).getName();
    }

    @Override
    public void initFirstDTO() {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    public Object getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    public PredictionAnalysisDTO createEntity(String name) {

        return createEntity(createAndInitIncomingEntity(name));
    }

    @Override
    public PredictionAnalysisDTO createEntity(Object dto) {

        return plpController.createAnalysis((PredictionAnalysis) dto);
    }

    @Override
    public Object createAndInitIncomingEntity(String name) {

        PredictionAnalysis predictionAnalysis = new PredictionAnalysis();
        predictionAnalysis.setName(name);
        predictionAnalysis.setSpecification(PLP_SPECIFICATION);
        return predictionAnalysis;
    }

    @Override
    public String getConstraintName() {

        return "uq_pd_name";
    }

    @Override
    public Integer getDtoId(Object dto) {

        return ((PredictionAnalysisDTO) dto).getId();
    }

    @Override
    public Object getEntity(int id) {

        return plpService.getAnalysis(id);
    }

    @Override
    public Object getExportEntity(Object entity) {

        return plpController.exportAnalysis(((PredictionAnalysis) entity).getId());
    }

    @Override
    public void setExportName(Object entity, String name) {

        ((PatientLevelPredictionAnalysisImpl) entity).setName(name);
    }

    @Override
    public Object doImport(Object dto) throws Exception {

        return plpController.importAnalysis((PatientLevelPredictionAnalysisImpl) dto);
    }
}
