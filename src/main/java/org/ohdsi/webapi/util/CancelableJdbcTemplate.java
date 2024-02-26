package org.ohdsi.webapi.util;

import org.ohdsi.sql.BigQuerySparkTranslate;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.jdbc.core.*;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Objects;

public class CancelableJdbcTemplate extends JdbcTemplate {

  private boolean suppressApiException = true;

  public CancelableJdbcTemplate() {
  }

  public CancelableJdbcTemplate(DataSource dataSource) {
    super(dataSource);
  }

  public CancelableJdbcTemplate(DataSource dataSource, boolean lazyInit) {
    super(dataSource, lazyInit);
  }

  public boolean isSuppressApiException() {
    return suppressApiException;
  }

  public void setSuppressApiException(boolean suppressApiException) {
    this.suppressApiException = suppressApiException;
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
        if (supportsBatchUpdates(stmt.getConnection())) {
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
            SQLException reason = ex.getNextException();
            if (Objects.nonNull(reason)) {
              throw reason;
            } else {
              throw new SQLException("Failed to execute batch update", ex);
            }
          }
        }
        else {
          for (int i = 0; i < sql.length; i++) {
            String connectionString = stmt.getConnection().getMetaData().getURL();
            if (connectionString.startsWith("jdbc:spark") || connectionString.startsWith("jdbc:databricks")) {
              this.currSql = BigQuerySparkTranslate.sparkHandleInsert(sql[i], stmt.getConnection());
              if (this.currSql == "" || this.currSql.isEmpty() || this.currSql == null) {
                rowsAffected[i] = -1;
                continue;
              }
            } else {
              this.currSql = sql[i];
            }
            if (!stmt.execute(this.currSql)) {
              rowsAffected[i] = stmt.getUpdateCount();
            }
            else if (!suppressApiException) {
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

  public int[] batchUpdate(StatementCancel cancelOp, List<PreparedStatementCreator> statements) {

    class BatchUpdateConnectionCallback implements ConnectionCallback<int[]> {

      private PreparedStatementCreator current;

      @Override
      public int[] doInConnection(Connection con) throws SQLException, DataAccessException {
        int[] rowsAffected = new int[statements.size()];

        for (int i = 0; i < statements.size(); i++) {
          current = statements.get(i);
          PreparedStatement query = current.createPreparedStatement(con);
          cancelOp.setStatement(query);
					if (!query.execute()) {
						rowsAffected[i] = query.getUpdateCount();
					}
					else if (!suppressApiException) {
						throw new InvalidDataAccessApiUsageException("Invalid batch SQL statement: " + getSql(current));
					}
          query.close();
          if (cancelOp.isCanceled()) {
            break;
          }
        }

        return rowsAffected;
      }

      private String getSql(PreparedStatementCreator statement) {
        if (statement instanceof SqlProvider) {
          return ((SqlProvider)statement).getSql();
        }
        return "";
      }
    }

    return execute(new BatchUpdateConnectionCallback());
  }

  private boolean supportsBatchUpdates(Connection connection) throws SQLException {

    // NOTE:
    // com.cloudera.impala.hivecommon.dataengine.HiveJDBCDataEngine.prepareBatch throws NOT_IMPLEMENTED exception
    return JdbcUtils.supportsBatchUpdates(connection) && !connection.getMetaData().getURL().startsWith("jdbc:impala");
  }
}
