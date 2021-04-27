package org.ohdsi.webapi.tag.converter;

import org.ohdsi.webapi.service.converters.BaseCommonDTOToEntityConverter;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.dto.*;
import org.springframework.stereotype.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TagDTOToTagConverter extends BaseCommonDTOToEntityConverter<TagDTO, Tag> {
    protected Tag createResultObject() {
        return new Tag();
    }

    @Override
    protected void doConvert(TagDTO source, Tag target) {
        target.setId(source.getId());
        List<Tag> groups = source.getGroups().stream()
                .map(t -> conversionService.convert(t, Tag.class))
                .collect(Collectors.toList());
        target.setGroups(groups);
        target.setName(source.getName());
        target.setType(source.getType());
    }
}
