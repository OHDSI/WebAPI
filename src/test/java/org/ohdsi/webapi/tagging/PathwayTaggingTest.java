package org.ohdsi.webapi.tagging;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.pathway.PathwayController;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisExportDTO;
import org.ohdsi.webapi.pathway.repository.PathwayAnalysisEntityRepository;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

public class PathwayTaggingTest extends BaseTaggingTest<PathwayAnalysisDTO, Integer> {
    private static final String JSON_PATH = "/tagging/pathway.json";

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

    @Override
    protected PathwayAnalysisDTO doCopyData(PathwayAnalysisDTO def) {
        return service.copy(def.getId());
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
    protected PathwayAnalysisDTO getDTO(Integer id) {
        return service.get(id);
    }

    @Override
    protected Integer getId(PathwayAnalysisDTO dto) {
        return dto.getId();
    }

    @Override
    protected void assignTags(Integer id, Tag...tags) {
        for (Tag tag : tags) {
            service.assignTag(id, tag.getId());
        }
    }

    @Override
    protected List<PathwayAnalysisDTO> getDTOsByTag(List<String> tagNames) {
        TagNameListRequestDTO requestDTO = new TagNameListRequestDTO();
        requestDTO.setNames(tagNames);
        return service.listByTags(requestDTO);
    }
}
