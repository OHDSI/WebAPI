package org.ohdsi.webapi.test.entity;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public class CohortDefinitionEntity extends TestCopy {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected CohortDefinitionService cdService;
    @Autowired
    protected CohortDefinitionRepository cdRepository;
    private CohortDTO firstSavedDTO;

    @Override
    public void tearDownDB() {

        cdRepository.deleteAll();
    }

    @Override
    protected Object createCopy(Object dto) {

        return cdService.copy(((CohortDTO) dto).getId());
    }

    @Override
    protected String getDtoName(Object dto) {

        return ((CohortDTO) dto).getName();
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
    protected String getConstraintName() {

        return "uq_cd_name";
    }

    @Override
    protected CohortDTO createEntity(String name) {

        CohortDTO dto = new CohortDTO();
        dto.setName(name);
        return cdService.createCohortDefinition(dto);
    }
}
