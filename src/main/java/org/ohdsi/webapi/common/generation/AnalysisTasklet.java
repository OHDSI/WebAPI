package org.ohdsi.webapi.common.generation;

import org.apache.commons.logging.Log;
import org.ohdsi.webapi.cohortcharacterization.repository.AnalysisGenerationInfoEntityRepository;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public abstract class AnalysisTasklet implements StoppableTasklet {

    protected final Log log;
    protected final ExecutorService taskExecutor;
    protected final TransactionTemplate transactionTemplate;
    protected final AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository;

    protected volatile boolean stopped = false;
    protected final long checkInterval = 1000L;

    public AnalysisTasklet(Log log, TransactionTemplate transactionTemplate, AnalysisGenerationInfoEntityRepository analysisGenerationInfoEntityRepository) {

        this.log = log;
        this.transactionTemplate = transactionTemplate;
        this.analysisGenerationInfoEntityRepository = analysisGenerationInfoEntityRepository;
        this.taskExecutor = Executors.newSingleThreadExecutor();
    }

    protected abstract int[] doTask(ChunkContext chunkContext);

    @Override
    public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
        try {
            this.transactionTemplate.execute(status -> doTask(chunkContext));
        } catch (final TransactionException e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            taskExecutor.shutdown();
        }
        return RepeatStatus.FINISHED;
    }

    protected int[] waitForFuture(FutureTask<int[]> futureTask) {
        try {
            while (true) {
                Thread.sleep(checkInterval);
                if (futureTask.isDone()) {
                    return futureTask.get();
                } else if (stopped) {
                    futureTask.cancel(true);
                    return null;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void stop() {
        this.stopped = true;
    }

    protected void saveInfo(Long jobId, String serializedDesign, UserEntity userEntity) {

        AnalysisGenerationInfoEntity generationInfoEntity = new AnalysisGenerationInfoEntity();
        generationInfoEntity.setId(jobId);
        generationInfoEntity.setDesign(serializedDesign);
        generationInfoEntity.setCreatedBy(userEntity);
        analysisGenerationInfoEntityRepository.save(generationInfoEntity);
    }
}
