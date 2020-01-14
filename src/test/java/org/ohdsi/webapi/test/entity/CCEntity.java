package org.ohdsi.webapi.test.entity;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;

public class CCEntity extends TestImport {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected CcController ccController;
    @Autowired
    protected CcRepository ccRepository;
    @Autowired
    protected CcService ccService;
    private CohortCharacterizationDTO firstSavedDTO;

    @Override
    public void tearDownDB() {

        ccRepository.deleteAll();
    }

    @Override
    protected Object createCopy(Object dto) {

        return ccController.copy(((CohortCharacterizationDTO) dto).getId());
    }

    @Override
    protected String getDtoName(Object dto) {

        return ((CohortCharacterizationDTO) dto).getName();
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

        return "uq_cc_name";
    }

    @Override
    protected CohortCharacterizationDTO createEntity(String name) {

        return createEntity(createAndInitDTO(name));
    }

    @Override
    protected CohortCharacterizationDTO createEntity(Object dto) {

        return ccController.create((CohortCharacterizationDTO) dto);
    }

    @Override
    protected CohortCharacterizationDTO createAndInitDTO(String name) {

        CohortCharacterizationDTO dto = new CohortCharacterizationDTO();
        dto.setName(name);
        return dto;
    }

    @Override
    protected Integer getDtoId(Object dto) {

        return ((CohortCharacterizationDTO) dto).getId().intValue();
    }

    @Override
    protected Object getEntity(int id) {

        return ccService.findByIdWithLinkedEntities((long) id);
    }

    @Override
    protected Object convertToDTO(Object entity) {

        return conversionService.convert(entity, CcExportDTO.class);
    }

    @Override
    protected void setDtoName(Object dto, String name) {

        ((CcExportDTO) dto).setName(name);
    }

    @Override
    protected Object doImport(Object dto) {

        return ccController.doImport((CcExportDTO) dto);
    }
}
