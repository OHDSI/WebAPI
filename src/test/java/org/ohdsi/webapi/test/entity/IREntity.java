package org.ohdsi.webapi.test.entity;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.service.IRAnalysisResource;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public class IREntity extends TestCopy {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected IRAnalysisResource irAnalysisResource;
    @Autowired
    protected IncidenceRateAnalysisRepository irRepository;
    private IRAnalysisDTO firstSavedDTO;

    @Override
    public void tearDownDB() {

        irRepository.deleteAll();
    }

    @Override
    protected Object createCopy(Object dto) {

        return irAnalysisResource.copy(((IRAnalysisDTO) dto).getId());
    }

    @Override
    protected String getDtoName(Object dto) {

        return ((IRAnalysisDTO) dto).getName();
    }

    @Override
    protected void initFirstDTO() {

        firstSavedDTO = createEntity(NEW_TEST_ENTITY);
    }

    @Override
    protected Object getFirstSavedDTO() {

        return firstSavedDTO;
    }

    @Override
    protected IRAnalysisDTO createEntity(String name) {

        IRAnalysisDTO dto = new IRAnalysisDTO();
        dto.setName(name);
        return irAnalysisResource.createAnalysis(dto);
    }

    @Override
    protected String getConstraintName() {

        return "uq_ir_name";
    }
}
