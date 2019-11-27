package org.ohdsi.webapi.test.entity.cohortdefinition;

import static org.ohdsi.webapi.test.entity.TestConstants.NEW_TEST_ENTITY;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public abstract class BaseCDTestEntity {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected CohortDefinitionService cdService;
    @Autowired
    protected CohortDefinitionRepository cdRepository;
    protected CohortDTO firstIncomingDTO;
    protected CohortDTO firstSavedDTO;

    @Before
    public void setupDB() {
        firstIncomingDTO = new CohortDTO();
        firstIncomingDTO.setName(NEW_TEST_ENTITY);
        firstSavedDTO = cdService.createCohortDefinition(firstIncomingDTO);
    }

    @After
    public void tearDownDB() {
        cdRepository.deleteAll();
    }
}
