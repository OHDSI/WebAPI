package org.ohdsi.webapi.test.entity.estimation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.EstimationController;
import org.ohdsi.webapi.estimation.EstimationService;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.repository.EstimationRepository;
import org.ohdsi.webapi.test.entity.BaseTestEntity;
import org.springframework.beans.factory.annotation.Autowired;

import static org.ohdsi.analysis.estimation.design.EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;

public abstract class BaseEstimationTestEntity extends BaseTestEntity {
    @Autowired
    protected EstimationController pleController;
    @Autowired
    protected EstimationRepository pleRepository;
    @Autowired
    protected EstimationService pleService;
    @Autowired
    protected CohortDefinitionDetailsRepository cdRepository;
    protected Estimation firstIncomingEntity;
    protected EstimationDTO firstSavedDTO;
    protected static String PLE_SPECIFICATION;

    @BeforeClass
    public static void setPleSpecification() throws IOException {
        File ple_spec = new File("src/test/resources/ple-specification.json");
        PLE_SPECIFICATION = FileUtils.readFileToString(ple_spec, StandardCharsets.UTF_8);
    }

    @Before
    public void setupDB() throws Exception {
        firstIncomingEntity = new Estimation();
        firstIncomingEntity.setName(NEW_TEST_ENTITY);
        firstIncomingEntity.setType(COMPARATIVE_COHORT_ANALYSIS);
        firstIncomingEntity.setSpecification(PLE_SPECIFICATION);
        firstSavedDTO = pleController.createEstimation(firstIncomingEntity);
    }

    @After
    public void tearDownDB() {

        pleRepository.deleteAll();
    }
}
