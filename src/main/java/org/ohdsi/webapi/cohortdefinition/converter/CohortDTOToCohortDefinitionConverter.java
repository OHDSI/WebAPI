package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.analysis.Cohort;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.springframework.stereotype.Component;

@Component
public class CohortDTOToCohortDefinitionConverter extends BaseCohortDTOToCohortDefinitionConverter<Cohort>{
    
    @Override
    public CohortDefinition convert(final Cohort source) {
        final CohortDefinition cohortDefinition = super.convert(source);

        if (source.getExpression() != null) {
            final CohortDefinitionDetails details = new CohortDefinitionDetails();
            final String expression = convertExpression(source);
            details.setExpression(expression);
            cohortDefinition.setDetails(details);
        }
        
        return cohortDefinition;
    }

    private String convertExpression(final Cohort source) {
        return Utils.serialize(source.getExpression());
    }
}
