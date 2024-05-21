package org.ohdsi.webapi.conceptset.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.conceptset.ConceptSetItemRepository;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.versioning.domain.ConceptSetVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConceptSetToConceptSetVersionConverter
        extends BaseConversionServiceAwareConverter<ConceptSet, ConceptSetVersion> {
    @Autowired
    private ConceptSetItemRepository conceptSetItemRepository;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public ConceptSetVersion convert(ConceptSet source) {
        List<ConceptSetItem> items = conceptSetItemRepository.findAllByConceptSetId(source.getId());
        String itemString;
        try {
            itemString = mapper.writeValueAsString(items);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ConceptSetVersion target = new ConceptSetVersion();
        target.setAssetId(source.getId());
        target.setAssetJson(itemString);

        return target;
    }
}
