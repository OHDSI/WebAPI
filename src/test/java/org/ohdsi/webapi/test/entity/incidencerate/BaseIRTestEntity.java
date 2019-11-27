package org.ohdsi.webapi.test.entity.incidencerate;

import static org.ohdsi.webapi.test.entity.TestConstants.NEW_TEST_ENTITY;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.ohdsi.webapi.WebApi;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.service.IRAnalysisResource;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApi.class)
@TestPropertySource(locations = "/in-memory-webapi.properties")
public abstract class BaseIRTestEntity {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected IRAnalysisResource irAnalysisResource;
    @Autowired
    protected IncidenceRateAnalysisRepository irRepository;
    protected IRAnalysisDTO firstIncomingDTO;
    protected IRAnalysisDTO firstSavedDTO;

    @Before
    public void setupDB() {
        firstIncomingDTO = new IRAnalysisDTO();
        firstIncomingDTO.setName(NEW_TEST_ENTITY);
        firstSavedDTO = irAnalysisResource.createAnalysis(firstIncomingDTO);
    }

    @After
    public void tearDownDB() {
        irRepository.deleteAll();
    }
}
