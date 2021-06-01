package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortVersionFullDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CohortVersionToCohortVersionFullDTOConverter
        extends BaseConversionServiceAwareConverter<CohortVersion, CohortVersionFullDTO> {
    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;

    @Override
    public CohortVersionFullDTO convert(CohortVersion source) {
        CohortDefinition def = this.cohortDefinitionRepository.findOneWithDetail(source.getAssetId().intValue());
        ExceptionUtils.throwNotFoundExceptionIfNull(def,
                String.format("There is no cohort definition with id = %d.", source.getAssetId()));

        CohortDefinitionDetails details = new CohortDefinitionDetails();
        details.setExpression(source.getAssetJson());

        CohortDefinition entity = new CohortDefinition();
        entity.setId(def.getId());
        entity.setTags(def.getTags());
        entity.setName(def.getName());
        entity.setExpressionType(def.getExpressionType());
        entity.setDetails(details);
        entity.setCohortAnalysisGenerationInfoList(def.getCohortAnalysisGenerationInfoList());
        entity.setGenerationInfoList(def.getGenerationInfoList());
        entity.setCreatedBy(def.getCreatedBy());
        entity.setCreatedDate(def.getCreatedDate());
        entity.setModifiedBy(def.getModifiedBy());
        entity.setModifiedDate(def.getModifiedDate());

        entity.setDescription(source.getDescription());

        CohortVersionFullDTO target = new CohortVersionFullDTO();
        target.setVersionDTO(conversionService.convert(source, VersionDTO.class));
        target.setEntityDTO(conversionService.convert(entity, CohortRawDTO.class));

        return target;
    }
}
