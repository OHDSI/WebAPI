package org.ohdsi.webapi.tagging;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CcShortDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.ohdsi.webapi.cohortcharacterization.specification.CohortCharacterizationImpl;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

public class CcTaggingTest extends BaseTaggingTest<CcShortDTO, Long> {
    private static final String JSON_PATH = "/tagging/characterization.json";

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

    @Override
    protected CcShortDTO doCopyData(CcShortDTO def) {
        return service.copy(def.getId());
    }

    @Override
    protected List<CcShortDTO> getDTOsByTag(List<String> tagNames) {
        TagNameListRequestDTO requestDTO = new TagNameListRequestDTO();
        requestDTO.setNames(tagNames);
        return service.listByTags(requestDTO);
    }

    @Override
    protected void doClear() {
        repository.deleteAll();
        cohortRepository.deleteAll();
    }

    @Override
    protected String getExpressionPath() {
        return JSON_PATH;
    }

    @Override
    protected void assignTag(Long id, boolean isPermissionProtected) {
        service.assignTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void unassignTag(Long id, boolean isPermissionProtected) {
        service.unassignTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void assignProtectedTag(Long id, boolean isPermissionProtected) {
        service.assignPermissionProtectedTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void unassignProtectedTag(Long id, boolean isPermissionProtected) {
        service.unassignPermissionProtectedTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected CohortCharacterizationDTO getDTO(Long id) {
        return service.getDesign(id);
    }

    @Override
    protected Long getId(CcShortDTO dto) {
        return dto.getId();
    }

    @Override
    protected void assignTags(Long id, Tag...tags) {
        for (Tag tag : tags) {
            service.assignTag(id, tag.getId());
        }
    }
}
