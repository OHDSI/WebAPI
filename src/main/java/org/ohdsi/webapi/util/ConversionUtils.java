package org.ohdsi.webapi.util;

import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.model.CommonEntityExt;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;
import org.ohdsi.webapi.tag.Tag;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.user.dto.UserDTO;
import org.springframework.core.convert.support.GenericConversionService;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ConversionUtils {
    public static void convertMetadataExt(GenericConversionService conversionService, CommonEntityExt<? extends Number> source, CommonEntityExtDTO target) {
        ConversionUtils.convertMetadata(conversionService, source, target);

        if (Objects.nonNull(source.getTags())) {
            Set<TagDTO> tags = source.getTags().stream()
                    .map(tag -> conversionService.convert(tag, TagDTO.class)).collect(Collectors.toSet());
            target.setTags(tags);
        }
    }

    public static void convertMetadataDTOExt(GenericConversionService conversionService, CommonEntityExtDTO source, CommonEntityExt<? extends Number> target) {
        if (Objects.nonNull(source.getTags())) {
            Set<Tag> tags = source.getTags().stream()
                    .map(tag -> conversionService.convert(tag, Tag.class)).collect(Collectors.toSet());
            target.setTags(tags);
        }
    }

    public static void convertMetadata(GenericConversionService conversionService, CommonEntity<? extends Number> source, CommonEntityDTO target) {
        target.setCreatedBy(conversionService.convert(source.getCreatedBy(), UserDTO.class));
        target.setCreatedDate(source.getCreatedDate());
        target.setModifiedBy(conversionService.convert(source.getModifiedBy(), UserDTO.class));
        target.setModifiedDate(source.getModifiedDate());
    }
}
