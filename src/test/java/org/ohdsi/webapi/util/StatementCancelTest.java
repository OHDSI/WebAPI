package org.ohdsi.webapi.util;

import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class StatementCancelTest {

  @Test
  public void cancel_capturesConnectionAndCancelsActiveStatement() throws Exception {
    Statement statement = mock(Statement.class);
    Connection connection = mock(Connection.class);
    when(statement.getConnection()).thenReturn(connection);
    StatementCancel cancel = new StatementCancel();

    assertTrue(cancel.setStatement(statement));
    assertSame(connection, cancel.getConnection());

    assertEquals(StatementCancel.CancellationResult.CANCELED, cancel.cancel());
    assertTrue(cancel.isCanceled());
    verify(statement).cancel();
  }

  @Test
  public void cancel_preservesCancellationWhenDriverDoesNotSupportIt() throws Exception {
    Statement statement = mock(Statement.class);
    doThrow(new SQLFeatureNotSupportedException()).when(statement).cancel();
    StatementCancel cancel = new StatementCancel();

    assertTrue(cancel.setStatement(statement));

    assertEquals(StatementCancel.CancellationResult.UNSUPPORTED, cancel.cancel());
    assertTrue(cancel.isCanceled());
  }

  @Test
  public void setStatement_rejectsWorkAfterCancellation() throws Exception {
    StatementCancel cancel = new StatementCancel();

    assertEquals(StatementCancel.CancellationResult.NO_ACTIVE_STATEMENT, cancel.cancel());
    assertFalse(cancel.setStatement(mock(Statement.class)));
  }
}