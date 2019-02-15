package org.ohdsi.webapi.estimation.converter;

import org.ohdsi.webapi.common.generation.BaseCommonGenerationToDtoConverter;
import org.ohdsi.webapi.common.generation.ExecutionBasedGenerationDTO;
import org.ohdsi.webapi.estimation.domain.EstimationGenerationEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;

@Component
public class EstimationGenerationToCommonGenerationDtoConverter extends BaseCommonGenerationToDtoConverter<EstimationGenerationEntity, ExecutionBasedGenerationDTO> {

    @Override
    protected ExecutionBasedGenerationDTO createResultObject() {
        return new ExecutionBasedGenerationDTO();
    }

    @Override
    public ExecutionBasedGenerationDTO convert(EstimationGenerationEntity source) {
        ExecutionBasedGenerationDTO dto = super.convert(source);
        Object[] values = new Object[]{ source.getAnalysisExecution(), source.getAnalysisExecution().getResultFiles() };
        if (Arrays.stream(values).noneMatch(Objects::isNull)) {
            dto.setNumResultFiles(source.getAnalysisExecution().getResultFiles().size());
        } else {
            dto.setNumResultFiles(0);
        }
        return dto;
    }
}
