package org.ohdsi.webapi.test.entity.cohortcharacterization;

import org.junit.After;
import org.junit.Before;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.ohdsi.webapi.test.entity.BaseTestEntity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseCCTestEntity extends BaseTestEntity {
    @Autowired
    protected CcController ccController;
    @Autowired
    protected CcRepository ccRepository;
    @Autowired
    protected CcService ccService;
    protected CohortCharacterizationDTO firstIncomingDTO;
    protected CohortCharacterizationDTO firstSavedDTO;

    @Before
    public void setupDB() {
        firstIncomingDTO = new CohortCharacterizationDTO();
        firstIncomingDTO.setName(NEW_TEST_ENTITY);
        firstSavedDTO = ccController.create(firstIncomingDTO);
    }

    @After
    public void tearDownDB() {
        ccRepository.deleteAll();
    }
}
