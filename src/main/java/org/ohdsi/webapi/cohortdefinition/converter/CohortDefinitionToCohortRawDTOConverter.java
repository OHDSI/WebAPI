package org.ohdsi.webapi.cohortdefinition.converter;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.springframework.stereotype.Component;

@Component
public class CohortDefinitionToCohortRawDTOConverter extends BaseCohortDefinitionToCohortMetadataDTOConverter<CohortRawDTO> {

	@Override
	public void doConvert(CohortDefinition source, CohortRawDTO target) {
		super.doConvert(source, target);
		target.setExpressionType(source.getExpressionType());
		if (source.getDetails() != null) {
			target.setExpression(source.getDetails().getExpression());
		}
	}

	@Override
	protected CohortRawDTO createResultObject() {
		return new CohortRawDTO();
	}
}
