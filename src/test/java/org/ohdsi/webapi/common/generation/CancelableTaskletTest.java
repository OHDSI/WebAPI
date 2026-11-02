package org.ohdsi.webapi.common.generation;

import org.junit.Test;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;

import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CancelableTaskletTest {

  @Test
  public void stop_marksTaskletCanceledWhenDriverDoesNotSupportStatementCancel() throws Exception {
    Statement statement = mock(Statement.class);
    doThrow(new SQLFeatureNotSupportedException()).when(statement).cancel();
    TestCancelableTasklet tasklet = new TestCancelableTasklet(statement);
    StepContribution contribution = mock(StepContribution.class);

    FutureTask<int[]> work = tasklet.startBlockingWork();
    assertTrue(tasklet.awaitWorkStart());

    tasklet.stop();

    assertNull(tasklet.awaitWorkCompletion(work));
    tasklet.applyAfter(contribution);

    verify(statement).cancel();
    verify(contribution).setExitStatus(new ExitStatus(Constants.CANCELED, "Canceled by user request"));
    tasklet.shutdown();
  }

  @Test
  public void stop_suppressesACompletedWorkerFailure() {
    TestCancelableTasklet tasklet = new TestCancelableTasklet(mock(Statement.class));
    FutureTask<int[]> work = new FutureTask<>(() -> {
      throw new RuntimeException("canceling statement due to user request");
    });
    work.run();

    tasklet.stop();

    assertNull(tasklet.awaitWorkCompletion(work));
    tasklet.shutdown();
  }

  private static class TestCancelableTasklet extends CancelableTasklet {

    private final CountDownLatch workStarted = new CountDownLatch(1);

    TestCancelableTasklet(Statement statement) {
      super(
          LoggerFactory.getLogger(TestCancelableTasklet.class),
          mock(CancelableJdbcTemplate.class),
          null
      );
      stmtCancel.setStatement(statement);
    }

    FutureTask<int[]> startBlockingWork() {
      FutureTask<int[]> work = new FutureTask<>(() -> {
        workStarted.countDown();
        new CountDownLatch(1).await();
        return new int[0];
      });
      taskExecutor.execute(work);
      return work;
    }

    boolean awaitWorkStart() throws InterruptedException {
      return workStarted.await(1, TimeUnit.SECONDS);
    }

    int[] awaitWorkCompletion(FutureTask<int[]> work) {
      return waitForFuture(work);
    }

    void applyAfter(StepContribution contribution) {
      doAfter(contribution, mock(ChunkContext.class));
    }

    void shutdown() {
      taskExecutor.shutdownNow();
    }
  }
}