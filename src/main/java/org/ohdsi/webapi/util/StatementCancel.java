package org.ohdsi.webapi.util;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

public class StatementCancel {

  private Statement statement;

  public void setStatement(Statement statement) {

    this.statement = statement;
  }

  public void cancel() throws SQLException {

    if (Objects.nonNull(statement)) {
      statement.cancel();
    }
  }

}
