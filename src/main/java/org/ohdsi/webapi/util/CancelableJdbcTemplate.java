package org.ohdsi.webapi.util;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlProvider;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.BatchUpdateException;
import java.sql.SQLException;
import java.sql.Statement;

public class CancelableJdbcTemplate extends JdbcTemplate {

  public CancelableJdbcTemplate() {
  }

  public CancelableJdbcTemplate(DataSource dataSource) {
    super(dataSource);
  }

  public CancelableJdbcTemplate(DataSource dataSource, boolean lazyInit) {
    super(dataSource, lazyInit);
  }

  public int[] batchUpdate(StatementCancel cancelOp, String... sql) throws DataAccessException {
    Assert.notEmpty(sql, "SQL array must not be empty");
    if (logger.isDebugEnabled()) {
      logger.debug("Executing SQL batch update of " + sql.length + " statements");
    }

    class BatchUpdateStatementCallback implements StatementCallback<int[]>, SqlProvider {

      private String currSql;

      @Override
      public int[] doInStatement(Statement stmt) throws SQLException, DataAccessException {
        int[] rowsAffected = new int[sql.length];
        cancelOp.setStatement(stmt);
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
              throw new InvalidDataAccessApiUsageException("Invalid batch SQL statement: " + sql[i]);
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
