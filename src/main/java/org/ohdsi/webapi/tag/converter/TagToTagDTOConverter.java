package org.ohdsi.webapi.tag.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.ohdsi.webapi.tag.Tag;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TagToTagDTOConverter extends BaseCommonEntityToDTOConverter<Tag, TagDTO> {
    @Override
    protected void doConvert(Tag source, TagDTO target) {
        target.setId(source.getId());
        Set<TagDTO> groups = source.getGroups().stream()
                .map(t -> conversionService.convert(t, TagDTO.class))
                .collect(Collectors.toSet());
        target.setGroups(groups);
        target.setName(source.getName());
        target.setType(source.getType());
    }

    protected TagDTO createResultObject() {
        return new TagDTO();
    }
}
