package org.ohdsi.webapi.prediction.converter;

import org.ohdsi.webapi.common.generation.BaseCommonGenerationToDtoConverter;
import org.ohdsi.webapi.common.generation.ExecutionBasedGenerationDTO;
import org.ohdsi.webapi.estimation.domain.EstimationGenerationEntity;
import org.ohdsi.webapi.prediction.domain.PredictionGenerationEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

@Component
public class PredictionGenerationToCommonGenerationDtoConverter extends BaseCommonGenerationToDtoConverter<PredictionGenerationEntity, ExecutionBasedGenerationDTO> {

    @Override
    protected ExecutionBasedGenerationDTO createResultObject() {
        return new ExecutionBasedGenerationDTO();
    }

    @Override
    public ExecutionBasedGenerationDTO convert(PredictionGenerationEntity source) {
        ExecutionBasedGenerationDTO dto = super.convert(source);
        if (Objects.nonNull(source.getAnalysisExecution()) && Objects.nonNull(source.getAnalysisExecution().getResultFiles())) {
            dto.setNumResultFiles(source.getAnalysisExecution().getResultFiles().size());
        } else {
            dto.setNumResultFiles(0);
        }
        return dto;
    }
}
