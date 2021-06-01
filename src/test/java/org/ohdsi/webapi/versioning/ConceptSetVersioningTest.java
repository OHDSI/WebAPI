package org.ohdsi.webapi.versioning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortVersionFullDTO;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.conceptset.dto.ConceptSetVersionFullDTO;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

public class ConceptSetVersioningTest extends BaseVersioningTest<ConceptSet,
        ConceptSetDTO, ConceptSetVersionFullDTO, Integer> {
    private static final String JSON_PATH = "/versioning/conceptset.json";

    @Autowired
    private ConceptSetService service;

    @Autowired
    private ConceptSetRepository repository;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public void createInitialDTO() throws IOException {
        ConceptSetDTO dto = new ConceptSetDTO();
        dto.setName("test dto name");

        UserEntity user = new UserEntity();
        user.setLogin("anonymous");
        userRepository.save(user);

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

    protected void checkClientDTOEquality(ConceptSetVersionFullDTO fullDTO) {
        ConceptSetDTO dto = fullDTO.getEntityDTO();
        checkEquality(dto);
        assertEquals(dto.getTags().size(), 0);
    }

    protected void checkServerDTOEquality(ConceptSetDTO dto) {
        checkEquality(dto);
        assertNull(dto.getTags());
    }

    private void checkEquality(ConceptSetDTO dto) {
        assertNotEquals(dto.getName(), initialDTO.getName());

        Iterable<ConceptSetItem> initialItems = service.getConceptSetItems(initialDTO.getId());
        long initialSize = StreamSupport.stream(initialItems.spliterator(), false).count();

        Iterable<ConceptSetItem> newItems = service.getConceptSetItems(dto.getId());
        long newSize = StreamSupport.stream(newItems.spliterator(), false).count();

        assertEquals(newSize, initialSize);
    }

    @Override
    protected ConceptSetDTO getEntity(Integer id) {
        return service.getConceptSet(id);
    }

    @Override
    protected ConceptSetDTO updateEntity(ConceptSetDTO dto) throws Exception {
        return service.updateConceptSet(dto.getId(), dto);
    }

    @Override
    protected ConceptSetDTO copyAssetFromVersion(Integer id, int version){
        return service.copyAssetFromVersion(id, version);
    }

    @Override
    protected Integer getId(ConceptSetDTO dto) {
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
    protected ConceptSetVersionFullDTO getVersion(Integer id, int version) {
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
