package org.ohdsi.webapi.executionengine.service;

import java.util.List;
import org.ohdsi.webapi.executionengine.dto.ExecutionRequestDTO;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.source.Source;
import org.springframework.stereotype.Service;

@Service
public interface ScriptExecutionService {

    Long runScript(ExecutionRequestDTO dto, int analysisExecutionId) throws Exception;

    Source findSourceByKey(String key);

    AnalysisExecution createAnalysisExecution(ExecutionRequestDTO dto, Source source, String password);

    String getExecutionStatus(Long executionId);

    List<AnalysisResultFile> getExecutionResultFiles(Long executionId);
}
