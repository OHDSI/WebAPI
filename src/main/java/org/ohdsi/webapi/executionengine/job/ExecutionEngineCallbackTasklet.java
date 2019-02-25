package org.ohdsi.webapi.executionengine.job;

import static org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus.Status.COMPLETED;
import static org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus.Status.FAILED;

import javax.persistence.EntityManager;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.executionengine.exception.ScriptCallbackException;
import org.ohdsi.webapi.executionengine.repository.AnalysisExecutionRepository;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

public class ExecutionEngineCallbackTasklet extends BaseExecutionTasklet {

    private final AnalysisExecutionRepository analysisExecutionRepository;
    private final EntityManager entityManager;

    public ExecutionEngineCallbackTasklet(AnalysisExecutionRepository analysisExecutionRepository, final EntityManager entityManager) {

        this.analysisExecutionRepository = analysisExecutionRepository;
        this.entityManager = entityManager;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        ExecutionEngineAnalysisStatus.Status status;

        final Long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobId();
        while (true) {
            entityManager.clear();
            status = analysisExecutionRepository.findByJobExecutionId(jobId).map(ExecutionEngineAnalysisStatus::getExecutionStatus)
                    .orElse(ExecutionEngineAnalysisStatus.Status.RUNNING);
            if (status == COMPLETED || status == FAILED) {
                break;
            }
            Thread.sleep(3000);
        }
        if (status == FAILED) {
            throw new ScriptCallbackException("Job execution failed");
        }
        return RepeatStatus.FINISHED;
    }
}
