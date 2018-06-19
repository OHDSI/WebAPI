package org.ohdsi.webapi.executionengine.job;

import org.ohdsi.webapi.executionengine.dto.ExecutionRequestDTO;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.ohdsi.webapi.executionengine.util.StringGenerationUtil;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

public class CreateAnalysisTasklet extends BaseExecutionTasklet {

    static final String ANALYSIS_EXECUTION_ID = "engineAnalysisExecutionId";
    
    private final ScriptExecutionService service;
    private final ExecutionRequestDTO request;

    private Integer analysisId;
    
    public CreateAnalysisTasklet(ScriptExecutionService executionService, ExecutionRequestDTO executionRequest) {

        this.service = executionService;
        this.request = executionRequest;
    }
    
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        final AnalysisExecution createAnalysis = service.createAnalysisExecution(
                request, 
                service.findSourceByKey(request.sourceKey), 
                StringGenerationUtil.generateRandomString());
        this.analysisId = createAnalysis.getId();
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {

        putInt(ANALYSIS_EXECUTION_ID, this.analysisId);
        return ExitStatus.COMPLETED;
    }
}
