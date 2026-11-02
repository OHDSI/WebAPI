package org.ohdsi.webapi.test;

import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.StatementCancel;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StatementCancelPostgresIT {

  private static final String SLEEP_QUERY = "SELECT pg_sleep(10)";
  private static final long QUERY_START_TIMEOUT_MS = 3_000L;
  private static JdbcTemplate jdbcTemplate;

  @BeforeClass
  public static void beforeClass() throws IOException {
    ITStarter.before();
    jdbcTemplate = new JdbcTemplate(ITStarter.getDataSource());
  }

  @AfterClass
  public static void afterClass() {
    ITStarter.tearDownSubject();
  }

  @Test
  public void cancel_interruptsRunningPostgresStatement() throws Exception {
    CancelableJdbcTemplate jdbcTemplate = new CancelableJdbcTemplate(ITStarter.getDataSource());
    StatementCancel statementCancel = new StatementCancel();
    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<int[]> execution = executor.submit(() -> jdbcTemplate.batchUpdate(
        statementCancel,
        Collections.singletonList((PreparedStatementCreator) connection -> connection.prepareStatement(SLEEP_QUERY))
    ));

    try {
      waitForSleepQueryToStart();

      long cancelStartedAt = System.nanoTime();
      assertEquals(StatementCancel.CancellationResult.CANCELED, statementCancel.cancel());

      try {
        execution.get(3, TimeUnit.SECONDS);
        fail("Expected PostgreSQL to cancel the running statement");
      } catch (ExecutionException expected) {
        long cancelElapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - cancelStartedAt);
        assertTrue("Statement cancellation took " + cancelElapsedMs + "ms", cancelElapsedMs < QUERY_START_TIMEOUT_MS);
      }
    } finally {
      if (!execution.isDone()) {
        statementCancel.cancel();
      }
      executor.shutdownNow();
      executor.awaitTermination(3, TimeUnit.SECONDS);
    }
  }

  private void waitForSleepQueryToStart() throws InterruptedException {
    long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(QUERY_START_TIMEOUT_MS);
    while (System.nanoTime() < deadline) {
      Integer activeQueries = jdbcTemplate.queryForObject(
          "SELECT count(*) FROM pg_stat_activity WHERE state = 'active' AND query = ?",
          Integer.class,
          SLEEP_QUERY
      );
      if (activeQueries != null && activeQueries > 0) {
        return;
      }
      Thread.sleep(50L);
    }
    fail("PostgreSQL did not start the simulated query within " + QUERY_START_TIMEOUT_MS + "ms");
  }
}