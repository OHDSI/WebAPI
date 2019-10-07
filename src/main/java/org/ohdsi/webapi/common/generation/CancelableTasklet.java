package org.ohdsi.webapi.common.generation;

import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.StatementCancel;
import org.slf4j.Logger;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public abstract class CancelableTasklet extends StoppableTransactionalTasklet<int[]> implements StoppableTasklet {

  protected final StatementCancel stmtCancel;
  protected final CancelableJdbcTemplate jdbcTemplate;

  public CancelableTasklet(Logger log,
                           CancelableJdbcTemplate jdbcTemplate,
                           TransactionTemplate transactionTemplate) {
    super(log, transactionTemplate);
    this.jdbcTemplate = jdbcTemplate;
    this.stmtCancel = new StatementCancel();
  }

  protected int[] doTask(ChunkContext chunkContext) {

    Callable<int[]> execution;
    String[] queries = prepareQueries(chunkContext, jdbcTemplate);
    if (Objects.nonNull(queries)) {
      execution = () -> jdbcTemplate.batchUpdate(stmtCancel, queries);
    } else {
      List<PreparedStatementCreator> creators = prepareStatementCreators(chunkContext, jdbcTemplate);
      if (Objects.nonNull(creators)) {
        execution = () -> jdbcTemplate.batchUpdate(stmtCancel, creators);
      } else {
        execution = () -> new int[0];
      }
    }

    FutureTask<int[]> batchUpdateTask = new FutureTask<>(execution);
    taskExecutor.execute(batchUpdateTask);
    return waitForFuture(batchUpdateTask);
  }

  protected String[] prepareQueries(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
    return null;
  }

  protected List<PreparedStatementCreator> prepareStatementCreators(ChunkContext chunkContext, CancelableJdbcTemplate jdbcTemplate) {
    return null;
  }

  @Override
  public void stop() {
    try {
      this.stmtCancel.cancel();
    } catch (SQLException ignored) {
    }
    super.stop();
  }
}
