package org.ohdsi.webapi.executionengine.job;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.List;
import java.util.Map;

public class CreateAnalysisTasklet extends BaseExecutionTasklet {

    static final String ANALYSIS_EXECUTION_ID = "engineAnalysisExecutionId";
    
    private final ScriptExecutionService service;

    private final String sourceKey;
    private List<AnalysisFile> analysisFiles;

    private Integer analysisId;


    public CreateAnalysisTasklet(ScriptExecutionService executionService, String sourceKey, List<AnalysisFile> analysisFiles) {

        this.service = executionService;
        this.sourceKey = sourceKey;
        this.analysisFiles = analysisFiles;
    }
    
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext context) throws Exception {

        Long jobId = context.getStepContext().getStepExecution().getJobExecution().getId();
        Map<String, Object> jobParams = context.getStepContext().getJobParameters();
        final String updatePassword = jobParams.get(Constants.Params.UPDATE_PASSWORD).toString();
        final ExecutionEngineAnalysisStatus createAnalysis = service.createAnalysisExecution(
                jobId,
                service.findSourceByKey(sourceKey),
                updatePassword,
                analysisFiles);
        this.analysisId = createAnalysis.getId();
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {

        putInt(ANALYSIS_EXECUTION_ID, this.analysisId);
        return ExitStatus.COMPLETED;
    }
}
