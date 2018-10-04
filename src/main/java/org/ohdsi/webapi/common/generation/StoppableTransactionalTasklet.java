package org.ohdsi.webapi.common.generation;

import org.apache.commons.logging.Log;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.FutureTask;

public abstract class StoppableTransactionalTasklet<T> extends TransactionalTasklet<T> implements StoppableTasklet {

  protected final long checkInterval = 1000L;
  private volatile boolean stopped = false;

  public StoppableTransactionalTasklet(Log log, TransactionTemplate transactionTemplate) {
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
          throw new RuntimeException(e);
      }
  }

  protected boolean isStopped() {
    return stopped;
  }

  @Override
  public void stop() {
      this.stopped = true;
  }
}
