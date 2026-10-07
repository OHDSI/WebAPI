package org.ohdsi.webapi.util;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Statement;
import java.util.Arrays;
import javax.sql.DataSource;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

/**
 * Regression tests for https://github.com/OHDSI/WebAPI/issues/2528:
 * the SQL Server JDBC driver executes a statement batch as a single T-SQL batch,
 * so re-creating a temp table that was dropped earlier in the same batch fails with
 * "There is already an object named '#...' in the database". Statements must
 * therefore be executed one at a time on SQL Server.
 */
public class CancelableJdbcTemplateTest {

  private static final String[] SQL = new String[]{
    "CREATE TABLE #tmp (id int)",
    "DROP TABLE #tmp",
    "CREATE TABLE #tmp (id int)"
  };

  private Statement statement;
  private DatabaseMetaData metaData;
  private CancelableJdbcTemplate jdbcTemplate;

  @Before
  public void setUp() throws Exception {

    statement = mock(Statement.class);
    metaData = mock(DatabaseMetaData.class);
    Connection connection = mock(Connection.class);
    DataSource dataSource = mock(DataSource.class);

    when(dataSource.getConnection()).thenReturn(connection);
    when(connection.createStatement()).thenReturn(statement);
    when(connection.getMetaData()).thenReturn(metaData);
    when(statement.getConnection()).thenReturn(connection);
    when(metaData.supportsBatchUpdates()).thenReturn(true);
    when(statement.executeBatch()).thenReturn(new int[]{0, 0, 0});

    jdbcTemplate = new CancelableJdbcTemplate(dataSource);
  }

  @Test
  public void batchUpdate_executesStatementsIndividuallyOnSqlServer() throws Exception {

    when(metaData.getURL()).thenReturn("jdbc:sqlserver://localhost:1433;databaseName=cdm");

    int[] rowsAffected = jdbcTemplate.batchUpdate(new StatementCancel(), SQL);

    verify(statement, never()).addBatch(anyString());
    verify(statement, never()).executeBatch();
    ArgumentCaptor<String> executed = ArgumentCaptor.forClass(String.class);
    verify(statement, times(SQL.length)).execute(executed.capture());
    assertEquals(Arrays.asList(SQL), executed.getAllValues());
    assertArrayEquals(new int[]{0, 0, 0}, rowsAffected);
  }

  @Test
  public void batchUpdate_usesDriverBatchingWhenSupported() throws Exception {

    when(metaData.getURL()).thenReturn("jdbc:postgresql://localhost:5432/cdm");

    jdbcTemplate.batchUpdate(new StatementCancel(), SQL);

    ArgumentCaptor<String> batched = ArgumentCaptor.forClass(String.class);
    verify(statement, times(SQL.length)).addBatch(batched.capture());
    assertEquals(Arrays.asList(SQL), batched.getAllValues());
    verify(statement, times(1)).executeBatch();
    verify(statement, never()).execute(anyString());
  }
}
