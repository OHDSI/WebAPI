package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.Constants;
import org.slf4j.Logger;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.FutureTask;

public abstract class StoppableTransactionalTasklet<T> extends TransactionalTasklet<T> implements StoppableTasklet {

  protected final long checkInterval = 1000L;
  private volatile boolean stopped = false;

  public StoppableTransactionalTasklet(Logger log, TransactionTemplate transactionTemplate) {
    super(log, transactionTemplate);
  }

  protected int[] waitForFuture(FutureTask<int[]> futureTask) {
      try {
          while (true) {
              Thread.sleep(checkInterval);
              if (futureTask.isDone()) {
                  return futureTask.get();
              } else if (isStopped()) {
                  futureTask.cancel(true);
                  return null;
              }
          }
      } catch (Exception e) {
          if (isStopped() && e.getCause() instanceof DataAccessResourceFailureException) {
            // ignore exception
            return null;
          }
          throw new RuntimeException(e);
      }
  }

  protected boolean isStopped() {
    return stopped;
  }

  @Override
  protected void doAfter(StepContribution stepContribution, ChunkContext chunkContext) {

    if (isStopped()) {
      stepContribution.setExitStatus(new ExitStatus(Constants.CANCELED, "Canceled by user request"));
    }
  }

  @Override
  public void stop() {
      this.stopped = true;
  }
}
