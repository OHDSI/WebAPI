package org.ohdsi.webapi.executionengine.service;

import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.ohdsi.webapi.source.Source;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

public interface ScriptExecutionService {

    void runScript(Long executionId, Source source, List<AnalysisFile> files, String updatePassword,
                   String executableFilename, String targetTable) throws Exception;

    Source findSourceByKey(String key);

    AnalysisExecution createAnalysisExecution(Long jobId, Source source, String password, List<AnalysisFile> analysisFiles);

    String getExecutionStatus(Long executionId);

    List<AnalysisResultFile> getExecutionResultFiles(Long executionId);

    void updateAnalysisStatus(AnalysisExecution analysisExecution, AnalysisExecution.Status running);

    List<AnalysisExecution> findOutdatedAnalyses(Date invalidate);

    File getExecutionResult(Long executionId) throws IOException;
}
