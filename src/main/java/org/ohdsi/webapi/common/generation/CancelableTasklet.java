package org.ohdsi.webapi.common.generation;

import org.apache.commons.logging.Log;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.StatementCancel;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.concurrent.FutureTask;

public abstract class CancelableTasklet extends StoppableTransactionalTasklet<int[]> implements StoppableTasklet {

  protected final StatementCancel stmtCancel;
  private final CancelableJdbcTemplate jdbcTemplate;

  public CancelableTasklet(Log log,
                           CancelableJdbcTemplate jdbcTemplate,
                           TransactionTemplate transactionTemplate) {
    super(log, transactionTemplate);
    this.jdbcTemplate = jdbcTemplate;
    this.stmtCancel = new StatementCancel();
  }

  protected int[] doTask(ChunkContext chunkContext) {

    String[] queries = prepareQueries(chunkContext, jdbcTemplate);

    FutureTask<int[]> batchUpdateTask = new FutureTask<>(
            () -> jdbcTemplate.batchUpdate(stmtCancel, queries)
    );
    taskExecutor.execute(batchUpdateTask);
    return waitForFuture(batchUpdateTask);
  }

  protected abstract String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate);

  @Override
  public void stop() {
    try {
      this.stmtCancel.cancel();
    } catch (SQLException ignored) {
    }
    super.stop();
  }
}
