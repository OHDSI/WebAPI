package org.ohdsi.webapi.executionengine.job;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.executionengine.dto.ExecutionRequestDTO;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Map;

public class CreateAnalysisTasklet extends BaseExecutionTasklet {

    static final String ANALYSIS_EXECUTION_ID = "engineAnalysisExecutionId";
    
    private final ScriptExecutionService service;

    private final String sourceKey;

    private Integer analysisId;


    public CreateAnalysisTasklet(ScriptExecutionService executionService, String sourceKey) {

        this.service = executionService;
        this.sourceKey = sourceKey;
    }
    
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext context) throws Exception {

        Long jobId = context.getStepContext().getStepExecution().getJobExecution().getJobId();
        Map<String, Object> jobParams = context.getStepContext().getJobParameters();
        final String updatePassword = jobParams.get(Constants.Params.UPDATE_PASSWORD).toString();
        final AnalysisExecution createAnalysis = service.createAnalysisExecution(
                jobId,
                service.findSourceByKey(sourceKey),
                updatePassword);
        this.analysisId = createAnalysis.getId();
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {

        putInt(ANALYSIS_EXECUTION_ID, this.analysisId);
        return ExitStatus.COMPLETED;
    }
}
