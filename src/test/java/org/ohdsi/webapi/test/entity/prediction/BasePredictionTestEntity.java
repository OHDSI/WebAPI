package org.ohdsi.webapi.test.entity.prediction;

import org.junit.After;
import org.junit.Before;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.PredictionController;
import org.ohdsi.webapi.prediction.PredictionService;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.repository.PredictionAnalysisRepository;
import org.ohdsi.webapi.test.entity.BaseTestEntity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BasePredictionTestEntity extends BaseTestEntity {
    @Autowired
    protected PredictionController prController;
    @Autowired
    protected PredictionAnalysisRepository prRepository;
    @Autowired
    protected PredictionService prService;
    protected PredictionAnalysis firstIncomingEntity;
    protected PredictionAnalysisDTO firstSavedDTO;
    protected static final String PR_SPECIFICATION =
            "{\"id\":null,\"name\":\"\",\"version\":\"v2.7.0\"," +
                    "\"description\":null,\"skeletonType\":\"PatientLevelPredictionStudy\",\"skeletonVersion\":\"v0.0.1\"," +
                    "\"createdBy\":null,\"createdDate\":null,\"modifiedBy\":null,\"modifiedDate\":null,\"cohortDefinitions\":[]," +
                    "\"conceptSets\":[],\"conceptSetCrossReference\":[],\"targetIds\":[],\"outcomeIds\":[],\"covariateSettings\":[]," +
                    "\"populationSettings\":[],\"modelSettings\":[],\"getPlpDataArgs\":{\"washoutPeriod\":0,\"maxSampleSize\":null}," +
                    "\"runPlpArgs\":{\"minCovariateFraction\":0.001,\"normalizeData\":true,\"testSplit\":\"person\",\"testFraction\":0.25," +
                    "\"splitSeed\":null,\"nfold\":3}}\"\n";

    @Before
    public void setupDB() {
        firstIncomingEntity = new PredictionAnalysis();
        firstIncomingEntity.setName(NEW_TEST_ENTITY);
        firstIncomingEntity.setSpecification(PR_SPECIFICATION);
        firstSavedDTO = prController.createAnalysis(firstIncomingEntity);
    }

    @After
    public void tearDownDB() {
        prRepository.deleteAll();
    }
}
