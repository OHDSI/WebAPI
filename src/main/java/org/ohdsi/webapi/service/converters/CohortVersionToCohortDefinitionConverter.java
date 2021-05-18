package org.ohdsi.webapi.service.converters;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.ohdsi.webapi.model.CommonEntityExt;
import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CohortVersionToCohortDefinitionConverter
        extends BaseConversionServiceAwareConverter<CohortVersion, CohortDefinition> {
    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;

    @Override
    public CohortDefinition convert(CohortVersion source) {        
        CohortDefinition def = this.cohortDefinitionRepository.findOneWithDetail(source.getAssetId());
        ExceptionUtils.throwNotFoundExceptionIfNull(def, 
                String.format("There is no cohort definition with id = %d.", source.getAssetId()));
        
        CohortDefinitionDetails details = new CohortDefinitionDetails();
        details.setExpression(source.getAssetJson());

        CohortDefinition target = new CohortDefinition();
        target.setCohortAnalysisGenerationInfoList(def.getCohortAnalysisGenerationInfoList());
        target.setId(def.getId());
        target.setTags(def.getTags());
        target.setName(def.getName());
        target.setDescription(source.getDescription());
        target.setExpressionType(def.getExpressionType());
        target.setDetails(details);
        target.setCohortAnalysisGenerationInfoList(def.getCohortAnalysisGenerationInfoList());
        target.setGenerationInfoList(def.getGenerationInfoList());
        target.setCohortCharacterizations(def.getCohortCharacterizations());
        target.setTags(def.getTags());
        target.setCreatedBy(def.getCreatedBy());
        target.setCreatedDate(def.getCreatedDate());
        target.setModifiedBy(def.getModifiedBy());
        target.setModifiedDate(def.getModifiedDate());

        return target;
    }
}
