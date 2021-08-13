package org.ohdsi.webapi.analysis.converter;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class AnalysisCohortDefinitionToCohortDefinitionConverter<T extends AnalysisCohortDefinition> extends BaseConversionServiceAwareConverter<T, CohortDefinition> {
    
    @Override
    public CohortDefinition convert(T source) {
        CohortDefinition cohortDefinition = new CohortDefinition();
        
        cohortDefinition.setId(source.getId());
        cohortDefinition.setDescription(source.getDescription());
        cohortDefinition.setExpressionType(source.getExpressionType());
        cohortDefinition.setName(source.getName());
        
        CohortDefinitionDetails details = new CohortDefinitionDetails();
        details.setCohortDefinition(cohortDefinition);
        details.setExpression(Utils.serialize(source.getExpression()));
        cohortDefinition.setDetails(details);
        
        return cohortDefinition;
    }
}
