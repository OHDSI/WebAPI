package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortDefinitionToCohortRawDTOConverter extends BaseCohortDefinitionToCohortMetadataDTOConverter<CohortRawDTO> {

	@Override
	public CohortRawDTO convert(final CohortDefinition source) {

		final CohortRawDTO dto = super.convert(source);
		dto.setExpressionType(source.getExpressionType());
		if (source.getDetails() != null) {
			dto.setExpression(source.getDetails().getExpression());
		}
		return dto;
	}

	@Override
	protected CohortRawDTO getResultObject() {
		return new CohortRawDTO();
	}
}
