package org.ohdsi.webapi.util;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.BatchUpdateException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class AdvancedJdbcTemplate extends JdbcTemplate {

  public AdvancedJdbcTemplate() {
  }

  public AdvancedJdbcTemplate(DataSource dataSource) {
    super(dataSource);
  }

  public AdvancedJdbcTemplate(DataSource dataSource, boolean lazyInit) {
    super(dataSource, lazyInit);
  }

  @Override
  protected int update(PreparedStatementCreator psc, PreparedStatementSetter pss) throws DataAccessException {
    logger.debug("Executing prepared SQL update");
    return execute(psc, new PreparedStatementCallback<Integer>() {
      @Override
      public Integer doInPreparedStatement(PreparedStatement ps) throws SQLException {
        try {
          if (pss != null) {
            pss.setValues(ps);
          }
          int rows = ps.execute() ? 0 : ps.getUpdateCount();
          if (logger.isDebugEnabled()) {
            logger.debug("SQL update affected " + rows + " rows");
          }
          return rows;
        }
        finally {
          if (pss instanceof ParameterDisposer) {
            ((ParameterDisposer) pss).cleanupParameters();
          }
        }
      }
    });
  }

  @Override
  public int[] batchUpdate(String... sql) throws DataAccessException {
    Assert.notEmpty(sql, "SQL array must not be empty");
    if (logger.isDebugEnabled()) {
      logger.debug("Executing SQL batch update of " + sql.length + " statements");
    }

    class BatchUpdateStatementCallback implements StatementCallback<int[]>, SqlProvider {

      private String currSql;

      @Override
      public int[] doInStatement(Statement stmt) throws SQLException, DataAccessException {
        int[] rowsAffected = new int[sql.length];
        if (JdbcUtils.supportsBatchUpdates(stmt.getConnection())) {
          for (String sqlStmt : sql) {
            this.currSql = appendSql(this.currSql, sqlStmt);
            stmt.addBatch(sqlStmt);
          }
          try {
            rowsAffected = stmt.executeBatch();
          }
          catch (BatchUpdateException ex) {
            String batchExceptionSql = null;
            for (int i = 0; i < ex.getUpdateCounts().length; i++) {
              if (ex.getUpdateCounts()[i] == Statement.EXECUTE_FAILED) {
                batchExceptionSql = appendSql(batchExceptionSql, sql[i]);
              }
            }
            if (StringUtils.hasLength(batchExceptionSql)) {
              this.currSql = batchExceptionSql;
            }
            throw ex;
          }
        }
        else {
          for (int i = 0; i < sql.length; i++) {
            this.currSql = sql[i];
            if (!stmt.execute(sql[i])) {
              rowsAffected[i] = stmt.getUpdateCount();
            }
            else {
              rowsAffected[i] = 0;  // suppress exception since Impala COMPUTE STATS returns resultSet
            }
          }
        }
        return rowsAffected;
      }

      private String appendSql(String sql, String statement) {
        return (StringUtils.isEmpty(sql) ? statement : sql + "; " + statement);
      }

      @Override
      public String getSql() {
        return this.currSql;
      }
    }

    return execute(new BatchUpdateStatementCallback());
  }
}
