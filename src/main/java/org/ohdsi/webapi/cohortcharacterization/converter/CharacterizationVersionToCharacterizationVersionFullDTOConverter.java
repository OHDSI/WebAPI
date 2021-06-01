package org.ohdsi.webapi.cohortcharacterization.converter;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcVersionFullDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.repository.CcRepository;
import org.ohdsi.webapi.cohortcharacterization.specification.CohortCharacterizationImpl;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.exception.ConversionAtlasException;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.versioning.domain.CharacterizationVersion;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CharacterizationVersionToCharacterizationVersionFullDTOConverter
        extends BaseConversionServiceAwareConverter<CharacterizationVersion, CcVersionFullDTO> {
    @Autowired
    private CohortDefinitionService cohortService;

    @Autowired
    private CcRepository ccRepository;

    @Override
    public CcVersionFullDTO convert(CharacterizationVersion source) {
        CohortCharacterizationEntity def = ccRepository.findOne(source.getAssetId());
        CohortCharacterizationImpl characterizationImpl =
                Utils.deserialize(source.getAssetJson(), CohortCharacterizationImpl.class);
        CohortCharacterizationEntity entity = conversionService.convert(characterizationImpl, CohortCharacterizationEntity.class);
        entity.setId(def.getId());
        entity.setTags(def.getTags());
        entity.setName(def.getName());
        entity.setCreatedBy(def.getCreatedBy());
        entity.setCreatedDate(def.getCreatedDate());
        entity.setModifiedBy(def.getModifiedBy());
        entity.setModifiedDate(def.getModifiedDate());

        List<Integer> ids = characterizationImpl.getCohorts().stream()
                .map(CohortMetadataDTO::getId)
                .collect(Collectors.toList());
        List<CohortDefinition> cohorts = cohortService.getCohorts(ids);
        if (cohorts.size() != ids.size()) {
            throw new ConversionAtlasException("Could not load version because it contains deleted cohorts");
        }
        entity.setCohortDefinitions(new HashSet<>(cohorts));

        CcVersionFullDTO target = new CcVersionFullDTO();
        target.setVersionDTO(conversionService.convert(source, VersionDTO.class));
        target.setEntityDTO(conversionService.convert(entity, CohortCharacterizationDTO.class));

        return target;
    }
}
