package org.ohdsi.webapi.reusable.converter;

import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.reusable.dto.ReusableDTO;
import org.ohdsi.webapi.reusable.dto.ReusableVersionFullDTO;
import org.ohdsi.webapi.reusable.repository.ReusableRepository;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.ReusableVersion;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReusableVersionToReusableVersionFullDTOConverter
        extends BaseConversionServiceAwareConverter<ReusableVersion, ReusableVersionFullDTO> {
    @Autowired
    private ReusableRepository repository;

    @Override
    public ReusableVersionFullDTO convert(ReusableVersion source) {
        Reusable def = this.repository.findOne(source.getAssetId().intValue());
        ExceptionUtils.throwNotFoundExceptionIfNull(def,
                String.format("There is no reusable with id = %d.", source.getAssetId()));

        Reusable entity = new Reusable();
        entity.setId(def.getId());
        entity.setTags(def.getTags());
        entity.setName(def.getName());
        entity.setCreatedBy(def.getCreatedBy());
        entity.setCreatedDate(def.getCreatedDate());
        entity.setModifiedBy(def.getModifiedBy());
        entity.setModifiedDate(def.getModifiedDate());

        entity.setDescription(source.getDescription());
        entity.setData(source.getAssetJson());

        ReusableVersionFullDTO target = new ReusableVersionFullDTO();
        target.setVersionDTO(conversionService.convert(source, VersionDTO.class));
        target.setEntityDTO(conversionService.convert(entity, ReusableDTO.class));

        return target;
    }
}
