package org.ohdsi.webapi.test.entity;

import static org.ohdsi.webapi.test.TestConstants.NEW_TEST_ENTITY;

import org.ohdsi.webapi.pathway.PathwayController;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import org.springframework.core.convert.ConversionService;

public class PathwayEntity extends TestImport {
    @Autowired
    protected ConversionService conversionService;
    @Autowired
    protected PathwayController pwController;
    @Autowired
    protected PathwayAnalysisEntityRepository pwRepository;
    @Autowired
    protected PathwayService pwService;
    private PathwayAnalysisDTO firstSavedDTO;

    @Override
    public void tearDownDB() {

        pwRepository.deleteAll();
    }

    @Override
    protected Object createCopy(Object dto) {

        return pwController.copy(((PathwayAnalysisDTO) dto).getId());
    }

    @Override
    protected String getDtoName(Object dto) {

        return ((PathwayAnalysisDTO) dto).getName();
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
    protected PathwayAnalysisDTO createEntity(String name) {

        return createEntity(createAndInitDTO(name));
    }

    @Override
    protected PathwayAnalysisDTO createEntity(Object dto) {

        return pwController.create((PathwayAnalysisDTO) dto);
    }

    @Override
    protected PathwayAnalysisDTO createAndInitDTO(String name) {

        PathwayAnalysisDTO dto = new PathwayAnalysisDTO();
        dto.setName(name);
        dto.setEventCohorts(new ArrayList<>());
        dto.setTargetCohorts(new ArrayList<>());
        return dto;
    }

    @Override
    protected String getConstraintName() {

        return "uq_pw_name";
    }

    @Override
    protected Integer getDtoId(Object dto) {

        return ((PathwayAnalysisDTO) dto).getId();
    }

    @Override
    protected Object getEntity(int id) {

        return pwService.getById(id);
    }

    @Override
    protected Object convertToDTO(Object entity) {

        return conversionService.convert(entity, PathwayAnalysisExportDTO.class);
    }

    @Override
    protected void setDtoName(Object dto, String name) {

        ((PathwayAnalysisExportDTO) dto).setName(name);
    }

    @Override
    protected Object doImport(Object dto) {

        return pwController.importAnalysis((PathwayAnalysisExportDTO) dto);
    }
}
