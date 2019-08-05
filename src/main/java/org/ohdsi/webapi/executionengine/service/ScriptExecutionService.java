package org.ohdsi.webapi.executionengine.service;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.source.Source;

public interface ScriptExecutionService {

    void runScript(Long executionId, Source source, List<AnalysisFile> files, String updatePassword,
                   String executableFilename, String targetTable) throws Exception;

    Source findSourceByKey(String key);

    ExecutionEngineAnalysisStatus createAnalysisExecution(Long jobId, Source source, String password, List<AnalysisFile> analysisFiles);

    String getExecutionStatus(Long executionId);

    File getExecutionResult(Long executionId) throws IOException;

    void invalidateExecutions(Date invalidateDate);
}
