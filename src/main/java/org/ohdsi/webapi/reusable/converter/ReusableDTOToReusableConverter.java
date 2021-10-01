package org.ohdsi.webapi.reusable.converter;

import org.ohdsi.webapi.service.converters.BaseCommonDTOToEntityConverter;
import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.tag.domain.Tag;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ReusableDTOToReusableConverter extends BaseCommonDTOToEntityConverter<ReusableDTO, Reusable> {
    protected Reusable createResultObject() {
        return new Reusable();
    }

    @Override
    protected void doConvert(ReusableDTO source, Reusable target) {
        target.setId(source.getId());
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setData(source.getData());
        if (Objects.nonNull(source.getTags())) {
            Set<Tag> tags = source.getTags().stream()
                    .map(tag -> conversionService.convert(tag, Tag.class)).collect(Collectors.toSet());
            target.setTags(tags);
        }
    }
}
