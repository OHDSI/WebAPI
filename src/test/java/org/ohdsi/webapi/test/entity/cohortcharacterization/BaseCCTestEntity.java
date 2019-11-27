package org.ohdsi.webapi.test.entity.cohortcharacterization;

import static org.ohdsi.webapi.test.entity.TestConstants.NEW_TEST_ENTITY;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public class BaseCCTestEntity {
    @Autowired
    protected ConversionService conversionService;
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
