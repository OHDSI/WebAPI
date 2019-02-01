package org.ohdsi.webapi.service;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.AuthMethod;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import com.odysseusinc.arachne.execution_engine_common.util.ConnectionParams;
import com.odysseusinc.datasourcemanager.krblogin.KerberosService;
import com.odysseusinc.datasourcemanager.krblogin.KrbConfig;
import com.odysseusinc.datasourcemanager.krblogin.RuntimeServiceMode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.IExecutionInfo;
import org.ohdsi.webapi.KerberosUtils;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecutionRepository;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisRepository;
import org.ohdsi.webapi.conceptset.ConceptSetItemRepository;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.DataSourceDTOParser;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Connection;

/**
 *
 */
public abstract class AbstractDaoService {

  protected final Log log = LogFactory.getLog(getClass());
  private static final String IMPALA_DATASOURCE = "impala";

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

  @Autowired
  protected Security security;

  @Autowired
  protected UserRepository userRepository;

  public static final List<GenerationStatus> INVALIDATE_STATUSES = new ArrayList<GenerationStatus>() {{
    add(GenerationStatus.PENDING);
    add(GenerationStatus.RUNNING);
  }};

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

  @Autowired
  private KerberosService kerberosService;

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

    ConnectionParams connectionParams = DataSourceDTOParser.parse(source);
    if (IMPALA_DATASOURCE.equalsIgnoreCase(source.getSourceDialect()) && AuthMethod.KERBEROS == connectionParams.getAuthMethod()) {
      loginToKerberos(source, connectionParams);
    }
    DriverManagerDataSource dataSource;
    if (source.getUsername() != null && source.getPassword() != null) {
      // NOTE: jdbc link should NOT include username and password, because they have higher priority than separate ones
      dataSource = new DriverManagerDataSource(
              source.getSourceConnection(),
              source.getUsername(),
              source.getPassword()
      );
    } else {
      dataSource = new DriverManagerDataSource(source.getSourceConnection());
    }
    return new JdbcTemplate(dataSource);
  }

  private void loginToKerberos(Source source, ConnectionParams connectionParams) {

    DataSourceUnsecuredDTO dto = DataSourceDTOParser.parseDTO(source, connectionParams);
    dto.setCdmSchema(source.getTableQualifier(SourceDaimon.DaimonType.CDM));
    KerberosUtils.setKerberosParams(source, connectionParams, dto);
    File temporaryDir = com.google.common.io.Files.createTempDir();
    KrbConfig krbConfig = new KrbConfig();
    try {
      krbConfig = kerberosService.runKinit(dto, RuntimeServiceMode.SINGLE, temporaryDir);
    } catch (RuntimeException | IOException e) {
      log.error("Login to kerberos failed", e);
    }
    try {
      FileUtils.forceDelete(temporaryDir);
      FileUtils.forceDelete(krbConfig.getKeytabPath().toFile());
    } catch (IOException e) {
      log.warn(e);
    }
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

  protected IExecutionInfo invalidateExecution(IExecutionInfo executionInfo) {

    return executionInfo.setIsValid(false)
            .setStatus(GenerationStatus.COMPLETE)
            .setMessage("Invalidated by system");
  }

  protected void invalidateExecutions(List<? extends IExecutionInfo> executionInfoList) {

    executionInfoList.forEach(this::invalidateExecution);
  }

  protected UserEntity getCurrentUser() {
    return userRepository.findByLogin(getCurrentUserLogin());
  }

  protected String getCurrentUserLogin() {
    return security.getSubject();
  }

}
