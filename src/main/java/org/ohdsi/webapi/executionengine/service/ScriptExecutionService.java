package org.ohdsi.webapi.executionengine.service;

import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.source.Source;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface ScriptExecutionService {

    void runScript(Long executionId, Source source, List<AnalysisFile> files, String updatePassword,
                   String executableFilename, String targetTable) throws Exception;

    Source findSourceByKey(String key);

    ExecutionEngineAnalysisStatus createAnalysisExecution(Long jobId, Source source, String password, List<AnalysisFile> analysisFiles);

    String getExecutionStatus(Long executionId);

    void updateAnalysisStatus(ExecutionEngineAnalysisStatus analysisExecution, ExecutionEngineAnalysisStatus.Status running);

    File getExecutionResult(Long executionId) throws IOException;
}
