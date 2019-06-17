package org.ohdsi.webapi.test.entity.prediction;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.PredictionController;
import org.ohdsi.webapi.prediction.PredictionService;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.repository.PredictionAnalysisRepository;
import org.ohdsi.webapi.test.entity.BaseTestEntity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BasePredictionTestEntity extends BaseTestEntity {
    @Autowired
    protected PredictionController plpController;
    @Autowired
    protected PredictionAnalysisRepository plpRepository;
    @Autowired
    protected PredictionService plpService;
    protected PredictionAnalysis firstIncomingEntity;
    protected PredictionAnalysisDTO firstSavedDTO;
    protected static String PLP_SPECIFICATION;
    
    @BeforeClass
    public static void setPlpSpecification() throws IOException {

        File plp_spec = new File("src/test/resources/plp-specification.json");
        PLP_SPECIFICATION = FileUtils.readFileToString(plp_spec, StandardCharsets.UTF_8);
    }

    @Before
    public void setupDB() {
        firstIncomingEntity = new PredictionAnalysis();
        firstIncomingEntity.setName(NEW_TEST_ENTITY);
        firstIncomingEntity.setSpecification(PLP_SPECIFICATION);
        firstSavedDTO = plpController.createAnalysis(firstIncomingEntity);
    }

    @After
    public void tearDownDB() {
        plpRepository.deleteAll();
    }
}
