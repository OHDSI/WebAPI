package org.ohdsi.webapi.util;

import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.model.CommonEntityExt;
import org.ohdsi.webapi.security.authz.User;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.springframework.core.convert.support.GenericConversionService;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ConversionUtils {
    public static void convertMetadataExt(GenericConversionService conversionService, CommonEntityExt<? extends Number> source, CommonEntityExtDTO target) {
        ConversionUtils.convertMetadata(conversionService, source, target);

        if (Objects.nonNull(source.getTags())) {
            Set<TagDTO> tags = new HashSet<>();
            source.getTags().forEach(tag -> {
                TagDTO tagDTO = conversionService.convert(tag, TagDTO.class);
                if (Objects.nonNull(tag.getGroups())) {
                    tag.getGroups().forEach(group -> {
                        TagDTO groupDTO = conversionService.convert(group, TagDTO.class);
                        tags.add(groupDTO);
                    });
                }
                tags.add(tagDTO);
            });
            target.setTags(tags);
        }
    }

    public static void convertMetadata(GenericConversionService conversionService, CommonEntity<? extends Number> source, CommonEntityDTO target) {
        target.setCreatedBy(User.fromEntity(source.getCreatedBy()));
        target.setCreatedDate(source.getCreatedDate());
        target.setModifiedBy(User.fromEntity(source.getModifiedBy()));
        target.setModifiedDate(source.getModifiedDate());
    }
}
