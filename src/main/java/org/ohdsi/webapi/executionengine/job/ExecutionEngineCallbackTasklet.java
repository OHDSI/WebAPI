package org.ohdsi.webapi.executionengine.job;

import static org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus.Status.COMPLETED;
import static org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus.Status.FAILED;

import java.util.Optional;
import javax.persistence.EntityManager;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineGenerationEntity;
import org.ohdsi.webapi.executionengine.repository.ExecutionEngineGenerationRepository;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

public class ExecutionEngineCallbackTasklet extends BaseExecutionTasklet {

    private final ExecutionEngineGenerationRepository executionEngineGenerationRepository;
    private final EntityManager entityManager;
    private ExitStatus exitStatus;

    public ExecutionEngineCallbackTasklet(ExecutionEngineGenerationRepository executionEngineGenerationRepository, final EntityManager entityManager) {

        this.executionEngineGenerationRepository = executionEngineGenerationRepository;
        this.entityManager = entityManager;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        final Long jobId = chunkContext.getStepContext().getStepExecution().getJobExecution().getId();
        while (true) {
            entityManager.clear();

            Optional<ExitStatus> exitStatusOptional = executionEngineGenerationRepository.findById(jobId)
                    .filter(g -> {
                        ExecutionEngineAnalysisStatus.Status status = g.getAnalysisExecution().getExecutionStatus();
                        return status == COMPLETED || status == FAILED;
                    })
                    .map(this::create);

            if (exitStatusOptional.isPresent()) {
                this.exitStatus = exitStatusOptional.get();
                break;
            }

            Thread.sleep(3000);
        }
        return RepeatStatus.FINISHED;
    }


    private ExitStatus create(ExecutionEngineGenerationEntity executionEngineGenerationEntity) {
        ExitStatus status = executionEngineGenerationEntity.getAnalysisExecution().getExecutionStatus() == FAILED ?
                ExitStatus.FAILED :
                ExitStatus.COMPLETED;
        return status.addExitDescription(executionEngineGenerationEntity.getExitMessage());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        return this.exitStatus;
    }
}
