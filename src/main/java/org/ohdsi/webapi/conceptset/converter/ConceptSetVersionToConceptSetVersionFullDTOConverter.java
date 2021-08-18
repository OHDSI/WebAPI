package org.ohdsi.webapi.conceptset.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.conceptset.dto.ConceptSetVersionFullDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.ConceptSetVersion;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConceptSetVersionToConceptSetVersionFullDTOConverter
        extends BaseConversionServiceAwareConverter<ConceptSetVersion, ConceptSetVersionFullDTO> {
    @Autowired
    private ConceptSetRepository conceptSetRepository;

    @Autowired
    private ObjectMapper mapper;

    @Override
    public ConceptSetVersionFullDTO convert(ConceptSetVersion source) {
        List<ConceptSetItem> items;
        try {
            items = mapper.readValue(source.getAssetJson(),
                    new TypeReference<List<ConceptSetItem>>() {
                    });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        ConceptSet conceptSet = conceptSetRepository.findById(source.getAssetId().intValue());
        ExceptionUtils.throwNotFoundExceptionIfNull(conceptSet,
                String.format("There is no concept set with id = %d.", source.getAssetId()));

        ConceptSetVersionFullDTO target = new ConceptSetVersionFullDTO();
        target.setItems(items);
        target.setVersionDTO(conversionService.convert(source, VersionDTO.class));
        target.setEntityDTO(conversionService.convert(conceptSet, ConceptSetDTO.class));

        return target;
    }
}
