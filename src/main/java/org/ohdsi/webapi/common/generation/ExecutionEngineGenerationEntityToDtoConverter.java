package org.ohdsi.webapi.common.generation;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

import org.ohdsi.webapi.executionengine.entity.ExecutionEngineGenerationEntity;

import java.util.Objects;

public class ExecutionEngineGenerationEntityToDtoConverter<T extends ExecutionEngineGenerationEntity> extends BaseCommonGenerationToDtoConverter<T, ExecutionBasedGenerationDTO>  {

  @Override
  protected ExecutionBasedGenerationDTO createResultObject() {
    return new ExecutionBasedGenerationDTO();
  }

  @Override
  public ExecutionBasedGenerationDTO convert(T source) {

    ExecutionBasedGenerationDTO dto = super.convert(source);
    if (source.getAnalysisExecution() != null && source.getAnalysisExecution().getExecutionStatus() != null) {
      dto.setStatus(source.getAnalysisExecution().getExecutionStatus().name());
    }
    if (nonNull(source.getAnalysisExecution()) && nonNull(source.getAnalysisExecution().getResultFiles())) {
      dto.setNumResultFiles(source.getAnalysisExecution().getResultFiles().size());
    } else {
      dto.setNumResultFiles(0);
    }
    return dto;
  }
}
