package org.ohdsi.webapi.executionengine.job;

import org.ohdsi.webapi.executionengine.dto.ExecutionRequestDTO;
import org.ohdsi.webapi.executionengine.service.ScriptExecutionService;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class RunExecutionEngineTasklet implements Tasklet {

    private final ScriptExecutionService executionService;
    private final ExecutionRequestDTO executionRequest;
    private final Long userId;

    public RunExecutionEngineTasklet(ScriptExecutionService executionService, ExecutionRequestDTO executionRequest, Long userId) {

        this.executionService = executionService;
        this.executionRequest = executionRequest;
        this.userId = userId;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        Long executionId = executionService.runScript(executionRequest, userId);
        chunkContext.getStepContext().getStepExecution().getExecutionContext().putLong("engineExecutionId", executionId);
        return RepeatStatus.FINISHED;
    }
}
