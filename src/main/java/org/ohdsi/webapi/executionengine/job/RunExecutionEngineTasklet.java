package org.ohdsi.webapi.executionengine.job;

import static org.ohdsi.webapi.executionengine.job.CreateAnalysisTasklet.ANALYSIS_EXECUTION_ID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.executionengine.dto.ExecutionRequestDTO;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class RunExecutionEngineTasklet implements Tasklet, StepExecutionListener {

    private static final Log LOGGER = LogFactory.getLog(RunExecutionEngineTasklet.class);
    
    private final ScriptExecutionService executionService;
    private final ExecutionRequestDTO executionRequest;
    private Integer analysisExecutionId;

    static final String ENGINE_EXECUTION_ID = "engineExecutionId";
    
    private Long executionId;
    
    public RunExecutionEngineTasklet(ScriptExecutionService executionService, ExecutionRequestDTO executionRequest) {

        this.executionService = executionService;
        this.executionRequest = executionRequest;
    }

    @Override
    public void beforeStep(final StepExecution stepExecution) {

        this.analysisExecutionId = (Integer) stepExecution
                .getJobExecution()
                .getExecutionContext()
                .get(ANALYSIS_EXECUTION_ID);
        LOGGER.debug(String.format("Ran %s analysis for cohort %s", executionRequest.analysisType.toString(), String.valueOf(executionRequest.cohortId)));
    }
    
    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        this.executionId = executionService.runScript(executionRequest, analysisExecutionId);
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(final StepExecution stepExecution) {

        stepExecution.getJobExecution().getExecutionContext().putLong(ENGINE_EXECUTION_ID, this.executionId);
        return ExitStatus.COMPLETED;
    }
}
