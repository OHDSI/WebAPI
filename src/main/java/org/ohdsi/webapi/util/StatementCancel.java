package org.ohdsi.webapi.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.Statement;
import java.util.Objects;

public class StatementCancel {

  public enum CancellationResult {
    NO_ACTIVE_STATEMENT,
    CANCELED,
    UNSUPPORTED,
    FAILED
  }

  private Statement statement;
  private Connection connection;
  private boolean canceled = false;

  public synchronized boolean setStatement(Statement statement) {
    if (this.canceled) {
      return false;
    }
    this.statement = statement;
    try {
      this.connection = statement.getConnection();
    } catch (SQLException ignored) {
      this.connection = null;
    }
    return true;
  }

  public synchronized CancellationResult cancel() throws SQLException {
    this.canceled = true;

    if (Objects.isNull(statement)) {
      return CancellationResult.NO_ACTIVE_STATEMENT;
    }

    try {
      statement.cancel();
      return CancellationResult.CANCELED;
    } catch (SQLFeatureNotSupportedException | AbstractMethodError ignored) {
      return CancellationResult.UNSUPPORTED;
    } catch (SQLException ignored) {
      return CancellationResult.FAILED;
    }
  }

  public synchronized Connection getConnection() {
    return connection;
  }

  public synchronized boolean isCanceled() {
    return canceled;
  }
}
