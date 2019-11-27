package org.ohdsi.webapi.test.entity.conceptset;

import static org.ohdsi.webapi.test.entity.TestConstants.NEW_TEST_ENTITY;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public abstract class BaseCSTestEntity {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected ConceptSetService csService;
    @Autowired
    protected ConceptSetRepository csRepository;
    protected ConceptSetDTO firstIncomingDTO;
    protected ConceptSetDTO firstSavedDTO;

    @Before
    public void setupDB() {
        firstIncomingDTO = new ConceptSetDTO();
        firstIncomingDTO.setName(NEW_TEST_ENTITY);
        firstSavedDTO = csService.createConceptSet(firstIncomingDTO);
    }

    @After
    public void tearDownDB() {
        csRepository.deleteAll();
    }
}
