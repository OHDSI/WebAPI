package org.ohdsi.webapi.executionengine.service;

import org.ohdsi.webapi.executionengine.dto.ExecutionRequestDTO;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ScriptExecutionService {

    Long runScript(ExecutionRequestDTO dto) throws Exception;

    String getExecutionStatus(Long executionId);

    List<AnalysisResultFile> getExecutionResultFiles(Long executionId);
}
