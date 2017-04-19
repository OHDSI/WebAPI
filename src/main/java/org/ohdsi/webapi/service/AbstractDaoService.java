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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

/**
 *
 */
public abstract class AbstractDaoService {

  protected final Log log = LogFactory.getLog(getClass());
	
  @Value("${datasource.ohdsi.schema}")
  private String ohdsiSchema;
  public String getOhdsiSchema() {
      return ohdsiSchema;
  }

  @Value("${datasource.dialect}")
  private String dialect;
  public String getDialect() {
    return dialect;
  }

  @Value("${datasource.dialect.source}")
  private String sourceDialect;
  public String getSourceDialect() {
    return sourceDialect;
  }

  @Value("${studyresults.datasource.dialect}")
  private String studyResultsDialect;
	public String getStudyResultsDialect() {
		return studyResultsDialect;
	}
	
  @Value("${studyresults.datasource.schema}")
  private String studyResultsSchema;
	public String getStudyResultsSchema() {
		return studyResultsSchema;
	}

	
  @Autowired
  private JdbcTemplate jdbcTemplate;
  public JdbcTemplate getJdbcTemplate() {
    return jdbcTemplate;
  }

	@Autowired
	private JdbcTemplate studyResultsJdbcTemplate;
	public JdbcTemplate getStudyResultsJdbcTemplate() {
		return studyResultsJdbcTemplate;
	}
	
  @Autowired
  private SourceRepository sourceRepository;
  public SourceRepository getSourceRepository() {
    return sourceRepository;
  }

  @Autowired 
  private ConceptSetItemRepository conceptSetItemRepository;
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
	public TransactionTemplate getTransactionTemplate() {
    return transactionTemplate;
  }

  @Autowired
  private TransactionTemplate transactionTemplateRequiresNew;
  public TransactionTemplate getTransactionTemplateRequiresNew() {
    return transactionTemplateRequiresNew;
  }

	public JdbcTemplate getSourceJdbcTemplate(Source source) {
    DriverManagerDataSource dataSource = new DriverManagerDataSource(source.getSourceConnection());
    JdbcTemplate template = new JdbcTemplate(dataSource);
    return template;
  }
	
  protected List<Map<String, String>> genericResultSetLoader(String sql, Source source) {
    List<Map<String, String>> results = null;
    try {
      results = getSourceJdbcTemplate(source).query(sql, new RowMapper<Map<String, String>>() {

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
  
}
