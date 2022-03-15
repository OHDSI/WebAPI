package org.ohdsi.webapi.util;

import com.google.common.collect.ImmutableSet;
import com.odysseusinc.arachne.commons.types.DBMSType;
import org.ohdsi.sql.SqlSplit;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapperResultSetExtractor;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class TempTableCleanupManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(TempTableCleanupManager.class);

  private static final String[] TABLE_TYPES = { "TABLE", "GLOBAL TEMPORARY", "LOCAL TEMPORARY" };
  private static final String DROP_TABLE_STATEMENT = "IF OBJECT_ID('%1$s', 'U') IS NOT NULL DROP TABLE %s;\n";
  private static final Set<String> APPLICABLE_DIALECTS = ImmutableSet.of(
    DBMSType.ORACLE.getOhdsiDB(),
    DBMSType.IMPALA.getOhdsiDB(),
    DBMSType.BIGQUERY.getOhdsiDB(),
    DBMSType.SPARK.getOhdsiDB(),
    // For Cohort Characterization which uses 'permanent temp tables'
    DBMSType.MS_SQL_SERVER.getOhdsiDB(),
    DBMSType.PDW.getOhdsiDB()
  );

  private JdbcTemplate jdbcTemplate;
  private TransactionTemplate transactionTemplate;
  private String dialect;
  private String sessionId;
  private String tempSchema;

  public TempTableCleanupManager(JdbcTemplate jdbcTemplate, TransactionTemplate transactionTemplate, String dialect, String sessionId, String tempSchema) {
    this.jdbcTemplate = jdbcTemplate;
    this.transactionTemplate = transactionTemplate;
    this.dialect = dialect;
    this.sessionId = sessionId;
    this.tempSchema = tempSchema;
  }

  protected boolean isApplicable(String dialect) {
    return APPLICABLE_DIALECTS.contains(dialect);
  }

  public void cleanupTempTables() {
    LOGGER.info("Removing temp tables at {}", tempSchema);
    transactionTemplate.execute(status -> {
      try {
        Connection c = jdbcTemplate.getDataSource().getConnection();
        if (isApplicable(this.dialect)) {
          removeTempTables(c, sessionId + "%");
        }
      } catch (SQLException e) {
        LOGGER.error("Failed to cleanup temp tables", e);
        throw new RuntimeException(e);
      }
      return null;
    });
  }

  private void removeTempTables(Connection c, String tablePrefix) throws SQLException {

      DatabaseMetaData metaData = c.getMetaData();
      try (ResultSet resultSet = metaData.getTables(null, tempSchema, tablePrefix, TABLE_TYPES)) {
        RowMapperResultSetExtractor<String> extractor = new RowMapperResultSetExtractor<>((rs, rowNum) -> rs.getString("TABLE_NAME"));
        List<String> tableNames = extractor.extractData(resultSet);
        String sql = tableNames.stream().map(table -> String.format(DROP_TABLE_STATEMENT, tempSchema + "." + table)).collect(Collectors.joining());
        String translatedSql = SqlTranslate.translateSql(sql, dialect);
        Arrays.asList(SqlSplit.splitSql(translatedSql)).forEach(jdbcTemplate::execute);
      }
  }

}
