package org.ohdsi.webapi.tagging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ConceptSetTaggingTest extends BaseTaggingTest<ConceptSetDTO, Integer> {
    private static final String JSON_PATH = "/tagging/conceptset.json";

    @Autowired
    private ConceptSetService service;

    @Autowired
    private ConceptSetRepository repository;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void doCreateInitialData() throws IOException {
        ConceptSetDTO dto = new ConceptSetDTO();
        dto.setName("test dto name");

        initialDTO = service.createConceptSet(dto);

        String expression = getExpression(getExpressionPath());
        ConceptSetExpression.ConceptSetItem[] expressionItems;
        try {
            ConceptSetExpression conceptSetExpression = mapper.readValue(expression, ConceptSetExpression.class);
            expressionItems = conceptSetExpression.items;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ConceptSetItem[] items = Arrays.stream(expressionItems)
                .map(i -> conversionService.convert(i, ConceptSetItem.class))
                .toArray(ConceptSetItem[]::new);

        service.saveConceptSetItems(initialDTO.getId(), items);
    }

    @Override
    protected ConceptSetDTO doCopyData(ConceptSetDTO def) {
        ConceptSetDTO dto = new ConceptSetDTO();
        dto.setName("test dto name 2");
        return service.createConceptSet(dto);
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
    protected ConceptSetDTO getDTO(Integer id) {
        return service.getConceptSet(id);
    }

    @Override
    protected Integer getId(ConceptSetDTO dto) {
        return dto.getId();
    }

    @Override
    protected void assignTags(Integer id, Tag...tags) {
        for (Tag tag : tags) {
            service.assignTag(id, tag.getId());
        }
    }

    @Override
    protected List<ConceptSetDTO> getDTOsByTag(List<String> tagNames) {
        TagNameListRequestDTO requestDTO = new TagNameListRequestDTO();
        requestDTO.setNames(tagNames);
        return service.listByTags(requestDTO);
    }
}
