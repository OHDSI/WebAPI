package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.analysis.Cohort;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortDTOToCohortDefinitionConverter extends BaseCohortDTOToCohortDefinitionConverter<CohortDefinition> {
    private String convertExpression(final Cohort source) {
        return Utils.serialize(source.getExpression());
    }

    @Override
    protected void doConvert(CohortMetadataDTO source, CohortDefinition target) {
        super.doConvert(source, target);
        if (target.getExpression() != null) {
            final CohortDefinitionDetails details = new CohortDefinitionDetails();
            final String expression = convertExpression(target);
            details.setExpression(expression);
            target.setDetails(details);
        }
    }

    @Override
    protected CohortDefinition createResultObject() {
        return new CohortDefinition();
    }
}
