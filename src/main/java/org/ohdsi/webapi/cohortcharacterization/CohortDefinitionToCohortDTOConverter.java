package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.converter.BaseConversionServiceAwareConverter;
import org.springframework.stereotype.Component;

@Component
public class CohortDefinitionToCohortDTOConverter extends BaseConversionServiceAwareConverter<CohortDefinition, CohortDTO> {
    @Override
    public CohortDTO convert(final CohortDefinition source) {
        final CohortDTO dto = new CohortDTO();
        dto.setDescription(source.getDescription());
        dto.setName(source.getName());
        dto.setId(source.getId());
        dto.setExpressionType(source.getExpressionType());
        dto.setExpression(source.getCohortExpression());
        return dto;
    }
}
