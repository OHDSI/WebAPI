package org.ohdsi.webapi.estimation.converter;

import org.ohdsi.webapi.common.generation.ExecutionEngineGenerationEntityToDtoConverter;
import org.ohdsi.webapi.estimation.domain.EstimationGenerationEntity;
import org.springframework.stereotype.Component;

@Component
public class EstimationGenerationToCommonGenerationDtoConverter extends ExecutionEngineGenerationEntityToDtoConverter<EstimationGenerationEntity> {
}
