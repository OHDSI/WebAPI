package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.analysis.Cohort;
import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsEntity;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortDTOToCohortDefinitionConverter extends BaseCohortDTOToCohortDefinitionConverter<CohortDTO> {
    private String convertExpression(final Cohort source) {
        return Utils.serialize(source.getExpression());
    }

    @Override
    protected void doConvert(CohortDTO source, CohortDefinitionEntity target) {
        super.doConvert(source, target);
        if (source.getExpression() != null) {
            final CohortDefinitionDetailsEntity details = new CohortDefinitionDetailsEntity();
            final String expression = convertExpression(source);
            details.setExpression(expression);
            target.setDetails(details);
        }
    }
}
