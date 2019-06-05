package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.exception.AtlasException;
import org.slf4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class TransactionalTasklet<T> implements Tasklet {
  protected final ExecutorService taskExecutor;
  protected final Logger log;
  protected final TransactionTemplate transactionTemplate;

  public TransactionalTasklet(Logger log, TransactionTemplate transactionTemplate) {
    this.taskExecutor = Executors.newSingleThreadExecutor();
    this.log = log;
    this.transactionTemplate = transactionTemplate;
  }

  protected void doBefore(ChunkContext chunkContext) {
  }

  protected void doAfter(StepContribution stepContribution, ChunkContext chunkContext) {
  }

  @Override
  public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
    try {
      doBefore(chunkContext);
      this.transactionTemplate.execute(status -> doTask(chunkContext));
      contribution.setExitStatus(ExitStatus.COMPLETED);
    } catch (final Throwable ex) {
      log.error(ex.getMessage(), ex);
      contribution.setExitStatus(new ExitStatus(Constants.FAILED, ex.getMessage()));
      throw new AtlasException(ex);
    } finally {
      taskExecutor.shutdown();
      doAfter(contribution, chunkContext);
    }
    return RepeatStatus.FINISHED;
  }

  protected abstract T doTask(ChunkContext chunkContext);
}
