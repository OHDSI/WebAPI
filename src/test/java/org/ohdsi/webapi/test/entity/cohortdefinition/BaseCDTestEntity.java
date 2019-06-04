package org.ohdsi.webapi.test.entity.cohortdefinition;

import org.junit.After;
import org.junit.Before;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.test.entity.BaseTestEntity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseCDTestEntity extends BaseTestEntity {
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
