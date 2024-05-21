package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.analysis.Cohort;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortDTOToCohortDefinitionConverter extends BaseCohortDTOToCohortDefinitionConverter<CohortDTO> {
    private String convertExpression(final Cohort source) {
        return Utils.serialize(source.getExpression());
    }

    @Override
    protected void doConvert(CohortDTO source, CohortDefinition target) {
        super.doConvert(source, target);
        if (source.getExpression() != null) {
            final CohortDefinitionDetails details = new CohortDefinitionDetails();
            final String expression = convertExpression(source);
            details.setExpression(expression);
            target.setDetails(details);
        }
    }
}
