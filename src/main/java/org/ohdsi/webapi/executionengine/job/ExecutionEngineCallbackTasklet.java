package org.ohdsi.webapi.executionengine.job;

import static org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus.Status.COMPLETED;
import static org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus.Status.FAILED;

import javax.persistence.EntityManager;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.executionengine.repository.ExecutionEngineGenerationRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

public class ExecutionEngineCallbackTasklet extends BaseExecutionTasklet {

    private final ExecutionEngineGenerationRepository executionEngineGenerationRepository;
    private final EntityManager entityManager;
    private ExecutionEngineAnalysisStatus.Status status;

    public ExecutionEngineCallbackTasklet(ExecutionEngineGenerationRepository executionEngineGenerationRepository, final EntityManager entityManager) {

        this.executionEngineGenerationRepository = executionEngineGenerationRepository;
        this.entityManager = entityManager;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        final Long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getJobId();
        while (true) {
            entityManager.clear();
            status = executionEngineGenerationRepository.findById(jobId).map(g -> g.getAnalysisExecution().getExecutionStatus())
                    .orElse(ExecutionEngineAnalysisStatus.Status.RUNNING);
            if (status == COMPLETED || status == FAILED) {
                break;
            }
            Thread.sleep(3000);
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {

        return status == FAILED ? ExitStatus.FAILED : ExitStatus.COMPLETED;
    }
}
