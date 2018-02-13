package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecutionRepository;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisRepository;
import org.ohdsi.webapi.conceptset.ConceptSetItemRepository;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;
import java.util.Properties;

/**
 *
 */
public abstract class AbstractDaoService {

  protected final Log log = LogFactory.getLog(getClass());

  @Value("${datasource.ohdsi.schema}")
  private String ohdsiSchema;

  @Value("${datasource.dialect}")
  private String dialect;

  @Value("${datasource.dialect.source}")
  private String sourceDialect;

  @Value("${source.name}")
  private String sourceName;

  @Value("${cdm.version}")
  private String cdmVersion;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private SourceRepository sourceRepository;

  @Autowired 
  ConceptSetItemRepository conceptSetItemRepository;

  public ConceptSetItemRepository getConceptSetItemRepository() {
    return conceptSetItemRepository;
  }
  
  @Autowired 
  private ConceptSetRepository conceptSetRepository;
  
  public ConceptSetRepository getConceptSetRepository() {
    return conceptSetRepository;
  }
  
  @Autowired
  private ComparativeCohortAnalysisRepository comparativeCohortAnalysisRepository;
  public ComparativeCohortAnalysisRepository getComparativeCohortAnalysisRepository() {
    return comparativeCohortAnalysisRepository;
  }
  
  @Autowired
  private ComparativeCohortAnalysisExecutionRepository comparativeCohortAnalysisExecutionRepository;
  public ComparativeCohortAnalysisExecutionRepository getComparativeCohortAnalysisExecutionRepository() {
    return comparativeCohortAnalysisExecutionRepository;
  }  
  
  @Autowired
  private TransactionTemplate transactionTemplate;

  @Autowired
  private TransactionTemplate transactionTemplateRequiresNew;

	@Autowired
  private TransactionTemplate transactionTemplateNoTransaction;

  public SourceRepository getSourceRepository() {
    return sourceRepository;
  }
  
  /**
   * @return the dialect
   */
  public String getDialect() {
    return dialect;
  }

  /**
   * @param dialect the dialect to set
   */
  public void setDialect(String dialect) {
    this.dialect = dialect;
  }

  /**
   * @return the jdbcTemplate
   */
  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

  public JdbcTemplate getSourceJdbcTemplate(Source source) {

    DriverManagerDataSource dataSource = new DriverManagerDataSource(source.getSourceConnection());
    JdbcTemplate template = new JdbcTemplate(dataSource);
    return template;
  }

  /**
   * @return the sourceDialect
   */
  public String getSourceDialect() {
    return sourceDialect;
  }

  /**
   * @param sourceDialect the sourceDialect to set
   */
  public void setSourceDialect(String sourceDialect) {
    this.sourceDialect = sourceDialect;
  }

  /**
   * @return the sourceName
   */
  public String getSourceName() {
    return sourceName;
  }

  /**
   * @param sourceName the sourceName to set
   */
  public void setSourceName(String sourceName) {
    this.sourceName = sourceName;
  }

  /**
   * @return the cdmVersion
   */
  public String getCdmVersion() {
    return cdmVersion;
  }

  /**
   * @param cdmVersion the cdmVersion to set
   */
  public void setCdmVersion(String cdmVersion) {
    this.cdmVersion = cdmVersion;
  }

  protected List<Map<String, String>> genericResultSetLoader(PreparedStatementRenderer psr, Source source) {
    List<Map<String, String>> results = null;
    try {
      results = getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new RowMapper<Map<String, String>>() {

        @Override
        public Map<String, String> mapRow(ResultSet rs, int rowNum)
                throws SQLException {
          Map<String, String> result = new HashMap<String, String>();
          ResultSetMetaData metaData = rs.getMetaData();
          int colCount = metaData.getColumnCount();
          for (int i = 1; i <= colCount; i++) {
            String columnLabel = metaData.getColumnLabel(i);
            String columnValue = String.valueOf(rs.getObject(i));
            result.put(columnLabel, columnValue);
          }
          return result;
        }

      });

    } catch (Exception e) {
      log.error("error loading in result set", e);
    }
    return results;
  }

  /**
   * @return the transactionTemplate
   */
  public TransactionTemplate getTransactionTemplate() {
    return transactionTemplate;
  }

  /**
   * @return the transactionTemplateRequiresNew
   */
  public TransactionTemplate getTransactionTemplateRequiresNew() {
    return transactionTemplateRequiresNew;
  }

	/**
   * @return the transactionTemplateNoTransaction
   */
  public TransactionTemplate getTransactionTemplateNoTransaction() {
    return transactionTemplateNoTransaction;
  }
	
  
  /**
   * @return the ohdsiSchema
   */
  public String getOhdsiSchema() {
      return ohdsiSchema;
  }
}
