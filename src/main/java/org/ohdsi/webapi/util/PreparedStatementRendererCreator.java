package org.ohdsi.webapi.util;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Objects;

public class PreparedStatementRendererCreator implements PreparedStatementCreator, SqlProvider {

  private final PreparedStatementRenderer psr;

  public PreparedStatementRendererCreator(PreparedStatementRenderer psr) {
    this.psr = psr;
  }

  @Override
  public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
    PreparedStatement statement = con.prepareStatement(psr.getSql());
    PreparedStatementSetter setter = psr.getSetter();
    if (Objects.nonNull(setter)) {
      setter.setValues(statement);
    }
    return statement;
  }

  @Override
  public String getSql() {
    return psr.getSql();
  }
}
