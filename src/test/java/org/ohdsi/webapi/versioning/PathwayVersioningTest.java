package org.ohdsi.webapi.versioning;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.pathway.PathwayController;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.dto.PathwayVersionFullDTO;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class PathwayVersioningTest extends BaseVersioningTest<PathwayAnalysisDTO, PathwayVersionFullDTO, Integer> {
    private static final String JSON_PATH = "/versioning/pathway.json";

    @Autowired
    private PathwayController service;

    @Autowired
    private PathwayAnalysisEntityRepository repository;

    @Autowired
    private CohortDefinitionRepository cohortRepository;

    @Override
    public void doCreateInitialData() throws IOException {
        String expression = getExpression(getExpressionPath());
        PathwayAnalysisExportDTO dto = Utils.deserialize(expression, PathwayAnalysisExportDTO.class);
        dto.setName("test dto name");

        initialDTO = service.importAnalysis(dto);
    }

    protected void checkClientDTOEquality(PathwayVersionFullDTO fullDTO) {
        PathwayAnalysisDTO dto = fullDTO.getEntityDTO();
        checkEquality(dto);
    }

    protected void checkServerDTOEquality(PathwayAnalysisDTO dto) {
        checkEquality(dto);
    }

    private void checkEquality(PathwayAnalysisDTO dto) {
        assertNotEquals(dto.getName(), initialDTO.getName());
        assertEquals(dto.getEventCohorts().size(), initialDTO.getEventCohorts().size());
        assertEquals(dto.getTargetCohorts().size(), initialDTO.getTargetCohorts().size());
        assertEquals(dto.getTags().size(), 0);
    }

    @Override
    protected PathwayAnalysisDTO getEntity(Integer id) {
        return service.get(id);
    }

    @Override
    protected PathwayAnalysisDTO updateEntity(PathwayAnalysisDTO dto) {
        return service.update(dto.getId(), dto);
    }

    @Override
    protected PathwayAnalysisDTO copyAssetFromVersion(Integer id, int version) {
        return service.copyAssetFromVersion(id, version);
    }

    @Override
    protected Integer getId(PathwayAnalysisDTO dto) {
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
    protected PathwayVersionFullDTO getVersion(Integer id, int version) {
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
