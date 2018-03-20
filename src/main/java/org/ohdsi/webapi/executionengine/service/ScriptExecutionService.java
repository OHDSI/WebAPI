package org.ohdsi.webapi.executionengine.service;

import java.util.List;
import org.ohdsi.webapi.executionengine.dto.ExecutionRequestDTO;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.springframework.stereotype.Service;

@Service
public interface ScriptExecutionService {

    Long runScript(ExecutionRequestDTO dto) throws Exception;

    String getExecutionStatus(Long executionId);

    List<AnalysisResultFile> getExecutionResultFiles(Long executionId);
}
