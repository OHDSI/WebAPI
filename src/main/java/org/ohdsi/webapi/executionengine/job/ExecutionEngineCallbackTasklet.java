package org.ohdsi.webapi.executionengine.job;

import static org.ohdsi.webapi.executionengine.entity.AnalysisExecution.Status.COMPLETED;
import static org.ohdsi.webapi.executionengine.entity.AnalysisExecution.Status.FAILED;

import java.util.Map;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.exception.ScriptCallbackException;
import org.ohdsi.webapi.executionengine.repository.AnalysisExecutionRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

public class ExecutionEngineCallbackTasklet implements Tasklet {

    private final AnalysisExecutionRepository analysisExecutionRepository;

    public ExecutionEngineCallbackTasklet(AnalysisExecutionRepository analysisExecutionRepository) {

        this.analysisExecutionRepository = analysisExecutionRepository;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        AnalysisExecution.Status status;

        while (true) {
            Map<String, Object> executionContext = chunkContext.getStepContext().getJobExecutionContext();
            if (executionContext.containsKey("engineExecutionId")) {
                Long engineExecutionId = (Long) executionContext.get("engineExecutionId");
                AnalysisExecution analysisExecution = analysisExecutionRepository.findOne(engineExecutionId.intValue());
                status = analysisExecution.getExecutionStatus();
                if (status == COMPLETED || status == FAILED) {
                    break;
                }
            }
            Thread.sleep(3000);
        }
        if (status == FAILED) {
            throw new ScriptCallbackException("Job execution failed");
        }
        return RepeatStatus.FINISHED;
    }
}
