package org.ohdsi.webapi.test.entity.conceptset;

import org.junit.After;
import org.junit.Before;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.test.entity.BaseTestEntity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseCSTestEntity extends BaseTestEntity {
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
