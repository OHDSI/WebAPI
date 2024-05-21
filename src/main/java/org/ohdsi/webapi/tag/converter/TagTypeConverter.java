package org.ohdsi.webapi.tag.converter;

import org.ohdsi.webapi.tag.domain.TagType;

import javax.persistence.AttributeConverter;

public class TagTypeConverter implements AttributeConverter<TagType, Integer> {
    @Override
    public Integer convertToDatabaseColumn(TagType tagType) {
        return tagType.getValue();
    }

    @Override
    public TagType convertToEntityAttribute(Integer tagTypeValue) {
        return TagType.fromValue(tagTypeValue);
    }
}
