package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortDefinitionToCohortDTOConverter extends BaseCohortDefinitionToCohortMetadataDTOConverter<CohortDTO> {

    @Override
    public CohortDTO convert(final CohortDefinition source) {

        final CohortDTO dto = super.convert(source);
        dto.setExpressionType(source.getExpressionType());
        dto.setExpression(source.getExpression());
        return dto;
    }

    @Override
    protected CohortDTO getResultObject() {
        return new CohortDTO();
    }
}
