package org.ohdsi.webapi.util;

import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.SqlProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class PreparedStatementRendererCreator implements PreparedStatementWithParamsCreator, PreparedStatementCreator, SqlProvider {

  private final PreparedStatementRenderer psr;

  public PreparedStatementRendererCreator(PreparedStatementRenderer psr) {
    this.psr = psr;
  }

  @Override
  public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
    PreparedStatement statement = con.prepareStatement(psr.getSql(),
            ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
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

  @Override
  public List<Object> getOrderedParamsList() {

    return psr.getOrderedParamsList();
  }
}
