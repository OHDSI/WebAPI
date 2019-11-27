package org.ohdsi.webapi.test.entity.estimation;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsRepository;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.EstimationController;
import org.ohdsi.webapi.estimation.EstimationService;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.repository.EstimationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import static org.ohdsi.analysis.estimation.design.EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;
import static org.ohdsi.webapi.test.entity.TestConstants.NEW_TEST_ENTITY;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public abstract class BaseEstimationTestEntity {
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
