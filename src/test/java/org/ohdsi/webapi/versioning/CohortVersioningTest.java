package org.ohdsi.webapi.versioning;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortVersionFullDTO;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class CohortVersioningTest extends BaseVersioningTest<CohortDTO, CohortVersionFullDTO, Integer> {
    private static final String JSON_PATH = "/versioning/cohort.json";

    @Autowired
    private CohortDefinitionService service;

    @Autowired
    private CohortDefinitionRepository repository;

    private CohortDTO deserializeExpression(String expression) {
        CohortRawDTO rawDTO = new CohortRawDTO();
        rawDTO.setExpression(expression);

        CohortDTO dto = conversionService.convert(rawDTO, CohortDTO.class);
        dto.setName("test dto name");
        dto.setDescription("test dto description");

        return dto;
    }

    @Override
    public void doCreateInitialData() throws IOException {
        String expression = getExpression(getExpressionPath());
        CohortDTO dto = deserializeExpression(expression);

        initialDTO = service.createCohortDefinition(dto);
    }

    protected void checkClientDTOEquality(CohortVersionFullDTO fullDTO) {
        CohortRawDTO dto = fullDTO.getEntityDTO();
        checkEquality(dto);
        assertEquals(dto.getTags().size(), 0);
        CohortDefinition def = conversionService.convert(initialDTO, CohortDefinition.class);
        CohortRawDTO rawDTO = conversionService.convert(def, CohortRawDTO.class);
        assertEquals(dto.getExpression(), rawDTO.getExpression());
    }

    protected void checkServerDTOEquality(CohortDTO dto) {
        checkEquality(dto);
        assertNull(dto.getTags());
        CohortDefinition def = conversionService.convert(initialDTO, CohortDefinition.class);
        CohortRawDTO rawDTO = conversionService.convert(def, CohortRawDTO.class);

        CohortDefinition newDef = conversionService.convert(dto, CohortDefinition.class);
        CohortRawDTO newRawDTO = conversionService.convert(newDef, CohortRawDTO.class);
        assertEquals(newRawDTO.getExpression(), rawDTO.getExpression());
    }

    private void checkEquality(CohortMetadataDTO dto) {
        assertNotEquals(dto.getName(), initialDTO.getName());
        assertEquals(dto.getDescription(), initialDTO.getDescription());
    }

    @Override
    protected CohortDTO getEntity(Integer id) {
        return service.getCohortDefinition(id);
    }

    @Override
    protected CohortDTO updateEntity(CohortDTO dto) {
        return service.saveCohortDefinition(dto.getId(), dto);
    }

    @Override
    protected CohortDTO copyAssetFromVersion(Integer id, int version) {
        return service.copyAssetFromVersion(id, version);
    }

    @Override
    protected Integer getId(CohortDTO dto) {
        return dto.getId();
    }

    @Override
    protected String getExpressionPath() {
        return JSON_PATH;
    }

    @Override
    protected List<VersionDTO> getVersions(Integer id) {
        return service.getVersions(id);
    }

    @Override
    protected CohortVersionFullDTO getVersion(Integer id, int version) {
        return service.getVersion(id, version);
    }

    @Override
    protected void updateVersion(Integer id, int version, VersionUpdateDTO updateDTO) {
        service.updateVersion(id, version, updateDTO);
    }

    @Override
    protected void deleteVersion(Integer id, int version) {
        service.deleteVersion(id, version);
    }

    @Override
    public void doClear() {
        repository.deleteAll();
    }
}
