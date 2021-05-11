package org.ohdsi.webapi.tag.converter;

import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.ohdsi.webapi.tag.domain.Tag;
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
        target.setCount(source.getCount());
        target.setColor(source.getColor());
        target.setIcon(source.getIcon());
        target.setShowGroup(source.isShowGroup());
        target.setPermissionProtected(source.isPermissionProtected());
        target.setMultiSelection(source.isMultiSelection());
        target.setMandatory(source.isMandatory());
        target.setAllowCustom(source.isAllowCustom());
        target.setDescription(source.getDescription());
    }

    protected TagDTO createResultObject() {
        return new TagDTO();
    }
}
