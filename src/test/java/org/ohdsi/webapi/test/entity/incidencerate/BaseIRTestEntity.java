package org.ohdsi.webapi.test.entity.incidencerate;

import org.junit.After;
import org.junit.Before;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.service.IRAnalysisResource;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.ohdsi.webapi.test.entity.BaseTestEntity;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseIRTestEntity extends BaseTestEntity {
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
