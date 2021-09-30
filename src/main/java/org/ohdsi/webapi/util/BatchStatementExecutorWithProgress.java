package org.ohdsi.webapi.util;

import org.apache.tika.concurrent.SimpleThreadPoolExecutor;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class BatchStatementExecutorWithProgress {

  private String[] statements;

  private final TransactionTemplate transactionTemplate;

  private final JdbcTemplate jdbcTemplate;

  private ExecutorService executorService;

  private static int PROGRESS_UPDATE_SIZE = 10;

  public BatchStatementExecutorWithProgress(String[] statements,
                                            TransactionTemplate transactionTemplate,
                                            JdbcTemplate jdbcTemplate) {
    this.statements = statements;
    this.transactionTemplate = transactionTemplate;
    this.jdbcTemplate = jdbcTemplate;
    executorService = Executors.newSingleThreadExecutor();
  }

  public int[] execute(Consumer<Integer> consumer){
    int[] updateCount = new int[statements.length];
    return transactionTemplate.execute(status -> {
      int totals = statements.length;
      try {
        for (int i = 0; i < totals; i++) {
          String stmt = statements[i];
          updateCount[i] = jdbcTemplate.execute((StatementCallback<Integer>) st -> !st.execute(stmt) ? st.getUpdateCount() : 0);
          if (i % PROGRESS_UPDATE_SIZE == 0 || i == (totals - 1)) {
            int progress = (int) Math.round(100.0 * i / totals);
            if (Objects.nonNull(consumer)) {
              executorService.execute(() -> consumer.accept(progress));
            }
          }
        }
      } finally {
        executorService.shutdown();
      }
      return updateCount;
    });
  }
}
