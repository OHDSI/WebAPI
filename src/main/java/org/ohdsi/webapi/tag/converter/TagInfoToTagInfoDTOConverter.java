package org.ohdsi.webapi.tag.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.service.converters.BaseCommonEntityToDTOConverter;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.domain.TagInfo;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.dto.TagInfoDTO;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TagInfoToTagInfoDTOConverter extends BaseConversionServiceAwareConverter<TagInfo, TagInfoDTO> {
    @Override
    public TagInfoDTO convert(TagInfo tagInfo) {
        TagInfoDTO tagInfoDTO = new TagInfoDTO();
        tagInfoDTO.setCount(tagInfo.getTagCount());
        tagInfoDTO.setTag(conversionService.convert(tagInfo.getTag(), TagDTO.class));

        return tagInfoDTO;
    }
}
