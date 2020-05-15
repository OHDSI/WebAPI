package org.ohdsi.webapi.util;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class StatementCancel {

  private Statement statement;
  private boolean canceled = false;

  public synchronized void setStatement(Statement statement) {
    if (this.canceled) {
        throw new StatementCancelException();
    }
    this.statement = statement;
  }

  public synchronized void cancel() throws SQLException {
    this.canceled = true;

    if (Objects.nonNull(statement)) {
      statement.cancel();
    }
  }

  public synchronized boolean isCanceled() {
    return canceled;
  }
}
