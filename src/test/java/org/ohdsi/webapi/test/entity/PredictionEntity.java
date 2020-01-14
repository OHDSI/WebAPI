package org.ohdsi.webapi.test.entity;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.PredictionController;
import org.ohdsi.webapi.prediction.PredictionService;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.repository.PredictionAnalysisRepository;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public class PredictionEntity extends TestImport {
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

    @Override
    public void tearDownDB() {

        plpRepository.deleteAll();
    }

    @Override
    protected Object createCopy(Object dto) throws Exception {

        return plpController.copy(((PredictionAnalysisDTO) dto).getId());
    }

    @Override
    protected String getDtoName(Object dto) {

        return ((PredictionAnalysisDTO) dto).getName();
    }

    @Override
    protected void initFirstDTO() throws Exception {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    protected Object getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    protected PredictionAnalysisDTO createEntity(String name) throws Exception {

        return createEntity(createAndInitDTO(name));
    }

    @Override
    protected PredictionAnalysisDTO createEntity(Object dto) throws Exception {

        return plpController.createAnalysis((PredictionAnalysis) dto);
    }

    @Override
    protected Object createAndInitDTO(String name) {

        PredictionAnalysis predictionAnalysis = new PredictionAnalysis();
        predictionAnalysis.setName(name);
        predictionAnalysis.setSpecification(PLP_SPECIFICATION);
        return predictionAnalysis;
    }

    @Override
    protected String getConstraintName() {

        return "uq_pd_name";
    }

    @Override
    protected Integer getDtoId(Object dto) {

        return ((PredictionAnalysisDTO) dto).getId();
    }

    @Override
    protected Object getEntity(int id) {

        return plpService.getAnalysis(id);
    }

    @Override
    protected Object convertToDTO(Object entity) {

        return plpController.exportAnalysis(((PredictionAnalysis) entity).getId());
    }

    @Override
    protected void setDtoName(Object dto, String name) {

        ((PatientLevelPredictionAnalysisImpl) dto).setName(name);
    }

    @Override
    protected Object doImport(Object dto) throws Exception {

        return plpController.importAnalysis((PatientLevelPredictionAnalysisImpl) dto);
    }
}
