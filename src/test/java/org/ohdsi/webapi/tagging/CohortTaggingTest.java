package org.ohdsi.webapi.tagging;

import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionService;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.domain.TagType;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;

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

    @Override
    protected CohortDTO doCopyData(CohortDTO def) {
        return service.copy(def.getId());
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

    @org.junit.Test
    public void unassignGroupedTagRemovesJoinTableRow() {
        Tag group = new Tag();
        group.setName("grouped tag parent");
        group.setType(TagType.SYSTEM);
        group.setCreatedDate(new Date());
        group.setGroups(new HashSet<>());
        group = tagRepository.save(group);

        Tag child = new Tag();
        child.setName("grouped tag child");
        child.setType(TagType.SYSTEM);
        child.setCreatedDate(new Date());
        child.setGroups(new HashSet<>(List.of(group)));
        child = tagRepository.save(child);

        try {
            service.assignTag(getId(initialDTO), child.getId());
            assertEquals(Integer.valueOf(1), jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM cohort_tag WHERE asset_id = ? AND tag_id = ?",
                    Integer.class,
                    getId(initialDTO),
                    child.getId()));

            service.unassignTag(getId(initialDTO), child.getId());
            assertEquals(Integer.valueOf(0), jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM cohort_tag WHERE asset_id = ? AND tag_id = ?",
                    Integer.class,
                    getId(initialDTO),
                    child.getId()));
        } finally {
            child.setGroups(new HashSet<>());
            tagRepository.save(child);
            tagRepository.delete(child);
            tagRepository.delete(group);
        }
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

    @Override
    protected void assignTags(Integer id, Tag...tags) {
        for (Tag tag : tags) {
            service.assignTag(id, tag.getId());
        }
    }

    @Override
    protected List<CohortDTO> getDTOsByTag(List<String> tagNames) {
        TagNameListRequestDTO requestDTO = new TagNameListRequestDTO();
        requestDTO.setNames(tagNames);
        return service.listByTags(requestDTO);
    }
}
