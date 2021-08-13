package org.ohdsi.webapi.tagging;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcExportDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.ohdsi.webapi.cohortcharacterization.specification.CohortCharacterizationImpl;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class CohortTaggingTest extends BaseTaggingTest<CohortDTO, Integer> {
    private static final String JSON_PATH = "/tagging/cohort.json";

    @Autowired
    private CohortDefinitionService service;

    @Autowired
    private CohortDefinitionRepository repository;

    @Override
    public void doCreateInitialData() throws IOException {
        String expression = getExpression(getExpressionPath());
        CohortDTO dto = deserializeExpression(expression);

        initialDTO = service.createCohortDefinition(dto);
    }

    private CohortDTO deserializeExpression(String expression) {
        CohortRawDTO rawDTO = new CohortRawDTO();
        rawDTO.setExpression(expression);

        CohortDTO dto = conversionService.convert(rawDTO, CohortDTO.class);
        dto.setName("test dto name");
        dto.setDescription("test dto description");

        return dto;
    }

    @Override
    protected void doClear() {
        repository.deleteAll();
    }

    @Override
    protected String getExpressionPath() {
        return JSON_PATH;
    }

    @Override
    protected void assignTag(Integer id, boolean isPermissionProtected) {
        service.assignTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void unassignTag(Integer id, boolean isPermissionProtected) {
        service.unassignTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void assignProtectedTag(Integer id, boolean isPermissionProtected) {
        service.assignPermissionProtectedTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected void unassignProtectedTag(Integer id, boolean isPermissionProtected) {
        service.unassignPermissionProtectedTag(id, getTag(isPermissionProtected).getId());
    }

    @Override
    protected CohortDTO getDTO(Integer id) {
        return service.getCohortDefinition(id);
    }

    @Override
    protected Integer getId(CohortDTO dto) {
        return dto.getId();
    }
}
