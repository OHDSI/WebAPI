package org.ohdsi.webapi.prediction.converter;

import org.ohdsi.webapi.common.generation.ExecutionEngineGenerationEntityToDtoConverter;
import org.ohdsi.webapi.prediction.domain.PredictionGenerationEntity;
import org.springframework.stereotype.Component;

@Component
public class PredictionGenerationToCommonGenerationDtoConverter extends ExecutionEngineGenerationEntityToDtoConverter<PredictionGenerationEntity> {
}
