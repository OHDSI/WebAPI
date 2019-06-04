package org.ohdsi.webapi.test.entity.pathway;

import org.junit.After;
import org.junit.Before;
import org.ohdsi.webapi.pathway.PathwayController;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.ohdsi.webapi.test.entity.BaseTestEntity;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;

public abstract class BasePWTestEntity extends BaseTestEntity {
    @Autowired
    protected PathwayController pwController;
    @Autowired
    protected PathwayAnalysisEntityRepository pwRepository;
    @Autowired
    protected PathwayService pwService;
    protected PathwayAnalysisDTO firstIncomingDTO;
    protected PathwayAnalysisDTO firstSavedDTO;

    @Before
    public void setupDB() {
        firstIncomingDTO = new PathwayAnalysisDTO();
        firstIncomingDTO.setName(NEW_TEST_ENTITY);
        firstIncomingDTO.setEventCohorts(new ArrayList<>());
        firstIncomingDTO.setTargetCohorts(new ArrayList<>());
        firstSavedDTO = pwController.create(firstIncomingDTO);
    }

    @After
    public void tearDownDB() {
        pwRepository.deleteAll();
    }
}
