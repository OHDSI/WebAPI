package org.ohdsi.webapi.analysis.converter;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsEntity;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class AnalysisCohortDefinitionToCohortDefinitionConverter<T extends AnalysisCohortDefinition> extends BaseConversionServiceAwareConverter<T, CohortDefinitionEntity> {
    
    @Override
    public CohortDefinitionEntity convert(T source) {
        CohortDefinitionEntity cohortDefinition = new CohortDefinitionEntity();
        
        cohortDefinition.setId(source.getId());
        cohortDefinition.setDescription(source.getDescription());
        cohortDefinition.setExpressionType(source.getExpressionType());
        cohortDefinition.setName(source.getName());
        
        CohortDefinitionDetailsEntity details = new CohortDefinitionDetailsEntity();
        details.setCohortDefinition(cohortDefinition);
        details.setExpression(Utils.serialize(source.getExpression()));
        cohortDefinition.setDetails(details);
        
        return cohortDefinition;
    }
}
