package org.ohdsi.webapi.tag.converter;

import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TagToTagDTOConverter extends BaseCommonEntityToDTOConverter<Tag, TagDTO> {
    @Override
    protected void doConvert(Tag source, TagDTO target) {
        target.setId(source.getId());
        List<TagDTO> groups = source.getGroups().stream()
                .map(t -> conversionService.convert(t, TagDTO.class))
                .collect(Collectors.toList());
        target.setGroups(groups);
        target.setName(source.getName());
        target.setType(source.getType());
        target.setCount(source.getCount());
    }

    protected TagDTO createResultObject() {
        return new TagDTO();
    }
}
