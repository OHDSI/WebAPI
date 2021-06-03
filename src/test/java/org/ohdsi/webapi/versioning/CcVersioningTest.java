package org.ohdsi.webapi.versioning;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcVersionFullDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.ohdsi.webapi.cohortcharacterization.specification.CohortCharacterizationImpl;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class CcVersioningTest extends BaseVersioningTest<CohortCharacterizationDTO, CcVersionFullDTO, Long> {
    private static final String JSON_PATH = "/versioning/characterization.json";

    @Autowired
    private CcController service;

    @Autowired
    private CcRepository repository;

    @Autowired
    private CohortDefinitionRepository cohortRepository;

    @Override
    public void doCreateInitialData() throws IOException {
        String expression = getExpression(getExpressionPath());

        CohortCharacterizationImpl characterizationImpl =
                Utils.deserialize(expression, CohortCharacterizationImpl.class);
        CohortCharacterizationEntity entity = conversionService.convert(characterizationImpl, CohortCharacterizationEntity.class);
        CcExportDTO exportDTO = conversionService.convert(entity, CcExportDTO.class);
        exportDTO.setName("test dto name");

        initialDTO = service.doImport(exportDTO);
    }

    protected void checkClientDTOEquality(CcVersionFullDTO fullDTO) {
        CohortCharacterizationDTO dto = fullDTO.getEntityDTO();
        checkEquality(dto);
        assertEquals(dto.getCohorts().size(), initialDTO.getCohorts().size());
        assertEquals(dto.getParameters().size(), initialDTO.getParameters().size());
        assertEquals(dto.getFeatureAnalyses().size(), initialDTO.getFeatureAnalyses().size());
    }

    protected void checkServerDTOEquality(CohortCharacterizationDTO dto) {
        checkEquality(dto);
    }

    private void checkEquality(CohortCharacterizationDTO dto) {
        assertNotEquals(dto.getName(), initialDTO.getName());
        assertEquals(dto.getCohorts().size(), initialDTO.getCohorts().size());
        assertEquals(dto.getParameters().size(), initialDTO.getParameters().size());
        assertEquals(dto.getFeatureAnalyses().size(), initialDTO.getFeatureAnalyses().size());
        assertEquals(dto.getTags().size(), 0);
    }

    @Override
    protected CohortCharacterizationDTO getEntity(Long id) {
        return service.getDesign(id);
    }

    @Override
    protected CohortCharacterizationDTO updateEntity(CohortCharacterizationDTO dto) {
        return service.update(dto.getId(), dto);
    }

    @Override
    protected CohortCharacterizationDTO copyAssetFromVersion(Long id, int version) {
        return service.copyAssetFromVersion(id, version);
    }

    @Override
    protected Long getId(CohortCharacterizationDTO dto) {
        return dto.getId();
    }

    @Override
    protected String getExpressionPath() {
        return JSON_PATH;
    }

    @Override
    protected List<VersionDTO> getVersions(Long id) {
        return service.getVersions(id);
    }

    @Override
    protected CcVersionFullDTO getVersion(Long id, int version) {
        return service.getVersion(id, version);
    }

    @Override
    protected void updateVersion(Long id, int version, VersionUpdateDTO updateDTO) {
        service.updateVersion(id, version, updateDTO);
    }

    @Override
    protected void deleteVersion(Long id, int version) {
        service.deleteVersion(id, version);
    }

    @Override
    public void doClear() {
        repository.deleteAll();
        cohortRepository.deleteAll();
    }
}
