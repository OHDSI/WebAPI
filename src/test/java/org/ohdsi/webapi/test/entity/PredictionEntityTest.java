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
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.PredictionController;
import org.ohdsi.webapi.prediction.PredictionService;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.repository.PredictionAnalysisRepository;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;
import org.ohdsi.webapi.test.ITStarter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;

@RunWith(JUnitParamsRunner.class)
@SpringBootTest(classes = WebApi.class)
public class PredictionEntityTest extends ITStarter implements TestCreate, TestCopy<PredictionAnalysisDTO>, TestImport<PredictionAnalysisDTO, PatientLevelPredictionAnalysisImpl> {
    @Autowired
    protected ConversionService conversionService;
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

    @Override
    public PredictionAnalysisDTO createCopy(PredictionAnalysisDTO dto) {

        return plpController.copy(dto.getId());
    }

    @Override
    public void initFirstDTO() {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    public PredictionAnalysisDTO getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    public PredictionAnalysisDTO createEntity(String name) {

        return createEntity(createAndInitIncomingEntity(name));
    }

    @Override
    public PredictionAnalysisDTO createEntity(PredictionAnalysisDTO dto) {

        PredictionAnalysis prediction = createPrediction(dto.getName());
        return plpController.createAnalysis(prediction);
    }

    @Override
    public PredictionAnalysisDTO createAndInitIncomingEntity(String name) {

        PredictionAnalysis predictionAnalysis = createPrediction(name);
        return conversionService.convert(predictionAnalysis, PredictionAnalysisDTO.class);
    }

    private PredictionAnalysis createPrediction(String name) {

        PredictionAnalysis prediction = new PredictionAnalysis();
        prediction.setName(name);
        prediction.setSpecification(PLP_SPECIFICATION);
        return prediction;
    }

    @Override
    public String getConstraintName() {

        return "uq_pd_name";
    }

    @Override
    public CommonEntity getEntity(int id) {

        return plpService.getAnalysis(id);
    }

    @Override
    public PatientLevelPredictionAnalysisImpl getExportEntity(CommonEntity entity) {

        return plpController.exportAnalysis(entity.getId().intValue());
    }

    @Override
    public PredictionAnalysisDTO doImport(PatientLevelPredictionAnalysisImpl dto) throws Exception {

        return plpController.importAnalysis(dto);
    }
}
