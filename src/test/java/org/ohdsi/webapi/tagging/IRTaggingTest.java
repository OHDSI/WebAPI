package org.ohdsi.webapi.tagging;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.IRAnalysisService;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

public class IRTaggingTest extends BaseTaggingTest<IRAnalysisDTO, Integer> {
    private static final String JSON_PATH = "/tagging/ir.json";

    @Autowired
    private IRAnalysisService service;

    @Autowired
    private IncidenceRateAnalysisRepository repository;

    @Autowired
    private CohortDefinitionRepository cohortRepository;

    @Override
    public void doCreateInitialData() throws IOException {
        String expression = getExpression(getExpressionPath());
        IRAnalysisDTO dto = deserializeExpression(expression);

        initialDTO = service.doImport(dto);
    }

    private IRAnalysisDTO deserializeExpression(String expression) {
        IRAnalysisDTO dto =
                Utils.deserialize(expression, IRAnalysisDTO.class);
        dto.setName("test dto name");
        dto.setDescription("test dto description");

        return dto;
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
    protected IRAnalysisDTO getDTO(Integer id) {
        return service.getAnalysis(id);
    }

    @Override
    protected Integer getId(IRAnalysisDTO dto) {
        return dto.getId();
    }
}
