package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortDefinitionToCohortDTOConverter extends BaseCohortDefinitionToCohortMetadataDTOConverter<CohortDTO> {

    @Override
    public void doConvert(CohortDefinition source, CohortDTO target) {
        super.doConvert(source, target);
        target.setExpressionType(source.getExpressionType());
        if (source.getDetails() != null) {
            CohortExpression expression = source.getDetails().getExpressionObject();
            target.setExpression(expression);
        }
    }

    @Override
    protected CohortDTO createResultObject() {
        return new CohortDTO();
    }
}
