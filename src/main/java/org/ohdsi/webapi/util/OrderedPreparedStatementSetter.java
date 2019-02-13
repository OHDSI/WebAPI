package org.ohdsi.webapi.util;

import org.springframework.jdbc.core.PreparedStatementSetter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class OrderedPreparedStatementSetter implements PreparedStatementSetter {

  private final List<Object> orderedParamsList;

  public OrderedPreparedStatementSetter(List<Object> orderedParamsList) {

    this.orderedParamsList = orderedParamsList;
  }

  @Override
  public void setValues(PreparedStatement ps) throws SQLException {

    if (Objects.nonNull(orderedParamsList)) {
      for (int i = 0; i < orderedParamsList.size(); i++) {
        ps.setObject(i + 1, orderedParamsList.get(i));
      }
    }
  }
}
