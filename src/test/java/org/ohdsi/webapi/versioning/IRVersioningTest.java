package org.ohdsi.webapi.versioning;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.ircalc.dto.IRVersionFullDTO;
import org.ohdsi.webapi.service.IRAnalysisService;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class IRVersioningTest extends BaseVersioningTest<IRAnalysisDTO, IRVersionFullDTO, Integer> {
    private static final String JSON_PATH = "/versioning/ir.json";

    @Autowired
    private IRAnalysisService service;

    @Autowired
    private IncidenceRateAnalysisRepository repository;

    @Autowired
    private CohortDefinitionRepository cohortRepository;

    private IRAnalysisDTO deserializeExpression(String expression) {
        IRAnalysisDTO dto =
                Utils.deserialize(expression, IRAnalysisDTO.class);
        dto.setName("test dto name");
        dto.setDescription("test dto description");

        return dto;
    }

    @Override
    public void doCreateInitialData() throws IOException {
        String expression = getExpression(getExpressionPath());
        IRAnalysisDTO dto = deserializeExpression(expression);

        initialDTO = service.doImport(dto);
    }

    protected void checkClientDTOEquality(IRVersionFullDTO fullDTO) {
        IRAnalysisDTO dto = fullDTO.getEntityDTO();
        checkEquality(dto);
        assertEquals(dto.getTags().size(), 0);
    }

    protected void checkServerDTOEquality(IRAnalysisDTO dto) {
        checkEquality(dto);
        assertNull(dto.getTags());
    }

    private void checkEquality(IRAnalysisDTO dto) {
        assertNotEquals(dto.getName(), initialDTO.getName());
        assertEquals(dto.getDescription(), initialDTO.getDescription());
        assertEquals(dto.getExpression(), initialDTO.getExpression());
    }

    @Override
    protected IRAnalysisDTO getEntity(Integer id) {
        return service.getAnalysis(id);
    }

    @Override
    protected IRAnalysisDTO updateEntity(IRAnalysisDTO dto) {
        return service.saveAnalysis(dto.getId(), dto);
    }

    @Override
    protected IRAnalysisDTO copyAssetFromVersion(Integer id, int version) {
        return service.copyAssetFromVersion(id, version);
    }

    @Override
    protected Integer getId(IRAnalysisDTO dto) {
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
    protected IRVersionFullDTO getVersion(Integer id, int version) {
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
        cohortRepository.deleteAll();
    }
}
