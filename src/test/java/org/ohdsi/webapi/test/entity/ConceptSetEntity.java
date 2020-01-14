package org.ohdsi.webapi.test.entity;

import static org.ohdsi.webapi.service.ConceptSetService.COPY_NAME;
import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public class ConceptSetEntity extends TestCopy {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected ConceptSetService csService;
    @Autowired
    protected ConceptSetRepository csRepository;
    private ConceptSetDTO firstSavedDTO;

    @Override
    public void tearDownDB() {

        csRepository.deleteAll();
    }

    @Override
    protected Object createCopy(Object dto) {

        ConceptSetDTO castedDTO = (ConceptSetDTO) dto;
        castedDTO.setName(csService.getNameForCopy(castedDTO.getId()).get(COPY_NAME));
        return csService.createConceptSet(castedDTO);
    }

    @Override
    protected String getDtoName(Object dto) {

        return ((ConceptSetDTO) dto).getName();
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
    protected ConceptSetDTO createEntity(String name) {

        ConceptSetDTO dto = new ConceptSetDTO();
        dto.setName(name);
        return csService.createConceptSet(dto);
    }

    @Override
    protected String getConstraintName() {

        return "uq_cs_name";
    }
}
