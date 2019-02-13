package org.ohdsi.webapi.executionengine.job;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.ohdsi.webapi.source.Source;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;
import java.util.Map;

public class RunExecutionEngineTasklet extends BaseExecutionTasklet {

    public static final String SCRIPT_ID = "scriptId";
    private final ScriptExecutionService executionService;
    private final Source source;
    private final List<AnalysisFile> analysisFiles;

    public RunExecutionEngineTasklet(ScriptExecutionService executionService, Source source, List<AnalysisFile> analysisFiles) {

        this.executionService = executionService;
        this.source = source;
        this.analysisFiles = analysisFiles;
    }
    
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        final Long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobId();
        Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
        final String updatePassword = jobParams.get(Constants.Params.UPDATE_PASSWORD).toString();
        final String executableFilename = jobParams.get(Constants.Params.EXECUTABLE_FILE_NAME).toString();
        executionService.runScript(jobId, source, analysisFiles, updatePassword, executableFilename);
        return RepeatStatus.FINISHED;
    }
}
