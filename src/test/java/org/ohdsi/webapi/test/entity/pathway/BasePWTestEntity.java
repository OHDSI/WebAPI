package org.ohdsi.webapi.test.entity.pathway;

import static org.ohdsi.webapi.test.entity.TestConstants.NEW_TEST_ENTITY;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.pathway.PathwayController;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public abstract class BasePWTestEntity {
    @Autowired
    protected ConversionService conversionService;
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
