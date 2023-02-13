package org.ohdsi.webapi.service;

import com.odysseusinc.arachne.commons.types.DBMSType;
import com.odysseusinc.arachne.execution_engine_common.api.v1.dto.DataSourceUnsecuredDTO;
import com.odysseusinc.datasourcemanager.krblogin.KerberosService;
import com.odysseusinc.datasourcemanager.krblogin.KrbConfig;
import com.odysseusinc.datasourcemanager.krblogin.RuntimeServiceMode;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.analysis.pathway.design.PathwayAnalysis;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.IExecutionInfo;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.common.sensitiveinfo.AbstractAdminService;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;
import org.ohdsi.webapi.conceptset.ConceptSetItemRepository;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.exception.BadRequestAtlasException;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.model.CommonEntityExt;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.reusable.domain.Reusable;
import org.ohdsi.webapi.security.PermissionService;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.DisabledSecurity;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceHelper;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.tag.TagSecurityUtils;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.util.CancelableJdbcTemplate;
import org.ohdsi.webapi.util.DataSourceDTOParser;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.ForbiddenException;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractDaoService extends AbstractAdminService {

  protected final Logger log = LoggerFactory.getLogger(getClass());

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

  @Value("${jdbc.suppressInvalidApiException}")
  protected boolean suppressApiException;

  @Autowired
  private JdbcTemplate jdbcTemplate;

  @Autowired
  private SourceRepository sourceRepository;

  @Autowired
  private ConceptSetItemRepository conceptSetItemRepository;

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
  private TransactionTemplate transactionTemplate;

  @Autowired
  private TransactionTemplate transactionTemplateRequiresNew;

  @Autowired
  private TransactionTemplate transactionTemplateNoTransaction;

  @Autowired
  private KerberosService kerberosService;

  @Autowired
  private SourceHelper sourceHelper;

  @Autowired
  private TagService tagService;

  @Autowired
  private PermissionService permissionService;
  
  @Autowired
  private ConversionService conversionService;

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

  public CancelableJdbcTemplate getSourceJdbcTemplate(Source source) {

    DriverManagerDataSource dataSource = getDriverManagerDataSource(source);
    CancelableJdbcTemplate jdbcTemplate = new CancelableJdbcTemplate(dataSource);
    jdbcTemplate.setSuppressApiException(suppressApiException);
    return jdbcTemplate;
  }

  public <T> T executeInTransaction(Source source, Function<JdbcTemplate, TransactionCallback<T>> callbackFunction) {
    DriverManagerDataSource dataSource = getDriverManagerDataSource(source);
    CancelableJdbcTemplate jdbcTemplate = new CancelableJdbcTemplate(dataSource);
    jdbcTemplate.setSuppressApiException(suppressApiException);
    DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
    TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
    transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    return transactionTemplate.execute(callbackFunction.apply(jdbcTemplate));
  }

  private DriverManagerDataSource getDriverManagerDataSource(Source source) {
    DataSourceUnsecuredDTO dataSourceData = DataSourceDTOParser.parseDTO(source);
    if (dataSourceData.getUseKerberos()) {
      loginToKerberos(dataSourceData);
    }
    DriverManagerDataSource dataSource;
    String connectionString = sourceHelper.getSourceConnectionString(source);
    if (dataSourceData.getUsername() != null && dataSourceData.getPassword() != null) {
      // NOTE: jdbc link should NOT include username and password, because they have higher priority than separate ones
      dataSource = new DriverManagerDataSource(
              connectionString,
              dataSourceData.getUsername(),
              dataSourceData.getPassword()
      );
    } else {
      dataSource = new DriverManagerDataSource(connectionString);
    }
    if (DBMSType.SNOWFLAKE.getValue().equalsIgnoreCase(source.getSourceDialect())) {
      if (dataSource.getConnectionProperties() == null) {
        dataSource.setConnectionProperties(new Properties());
      }
      dataSource.getConnectionProperties().setProperty("CLIENT_RESULT_COLUMN_CASE_INSENSITIVE", "true");
    }
    return dataSource;
  }

  private void loginToKerberos(DataSourceUnsecuredDTO dataSourceData) {

    File temporaryDir = com.google.common.io.Files.createTempDir();
    KrbConfig krbConfig = new KrbConfig();
    try {
      krbConfig = kerberosService.runKinit(dataSourceData, RuntimeServiceMode.SINGLE, temporaryDir);
    } catch (RuntimeException | IOException e) {
      log.error("Login to kerberos failed", e);
    }
    try {
      FileUtils.forceDelete(temporaryDir);
      if (krbConfig.getComponents() != null && StringUtils.isNotBlank(krbConfig.getComponents().getKeytabPath().toString())){
        FileUtils.forceDelete(krbConfig.getComponents().getKeytabPath().toFile());
      }
    } catch (IOException e) {
      log.warn(e.getMessage(), e);
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
      log.error("Result set loading error", e);
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
  
  protected PermissionService getPermissionService() {
    return this.permissionService;
  }

  protected void assignTag(CommonEntityExt<?> entity, int tagId) {
    if (Objects.nonNull(entity)) {
      Tag tag = tagService.getById(tagId);
      if (Objects.nonNull(tag)) {
        if (tag.isPermissionProtected() && !hasPermissionToAssignProtectedTags(entity, "post")) {
          throw new UnauthorizedException(String.format("No permission to assign protected tag '%s' to %s (id=%s).",
                  tag.getName(), entity.getClass().getSimpleName(), entity.getId()));
        }

        // unassign tags from the same group if group marked as multi_selection=false
        tag.getGroups().stream().findFirst().ifPresent(group -> {
          if (!group.isMultiSelection()) {
            entity.getTags().forEach(t -> {
              if (t.getGroups().stream().anyMatch(g -> g.getId().equals(group.getId()))) {
                unassignTag(entity, t.getId());
              }
            });
          }
        });


        entity.getTags().add(tag);
      }
    }
  }

  protected void unassignTag(CommonEntityExt<?> entity, int tagId) {
    if (Objects.nonNull(entity)) {
      Tag tag = tagService.getById(tagId);
      if (Objects.nonNull(tag)) {
        if (tag.isPermissionProtected() && !hasPermissionToAssignProtectedTags(entity, "delete")) {
          throw new UnauthorizedException(String.format("No permission to unassign protected tag '%s' from %s (id=%s).",
                  tag.getName(), entity.getClass().getSimpleName(), entity.getId()));
        }
        Set<Tag> tags = entity.getTags().stream()
                .filter(t -> t.getId() != tagId)
                .collect(Collectors.toSet());
        entity.setTags(tags);
      }
    }
  }

  private boolean hasPermissionToAssignProtectedTags(final CommonEntityExt<?> entity, final String method) {
    if (!isSecured()) {
      return true;
    }

    return TagSecurityUtils.checkPermission(TagSecurityUtils.getAssetName(entity), method);
  }

  protected void checkOwnerOrAdmin(UserEntity owner) {
    if (security instanceof DisabledSecurity) {
      return;
    }

    UserEntity user = getCurrentUser();
    Long ownerId = Objects.nonNull(owner) ? owner.getId() : null;

    if (!(user.getId().equals(ownerId) || isAdmin())) {
      throw new ForbiddenException();
    }
  }

  protected void checkOwnerOrAdminOrModerator(UserEntity owner) {
    if (security instanceof DisabledSecurity) {
      return;
    }

    UserEntity user = getCurrentUser();
    Long ownerId = Objects.nonNull(owner) ? owner.getId() : null;

    if (!(user.getId().equals(ownerId) || isAdmin() || isModerator())) {
      throw new ForbiddenException();
    }
  }

  protected void checkOwnerOrAdminOrGranted(CommonEntity<?> entity) {
    if (security instanceof DisabledSecurity) {
      return;
    }

    UserEntity user = getCurrentUser();
    Long ownerId = Objects.nonNull(entity.getCreatedBy()) ? entity.getCreatedBy().getId() : null;

    if (!(user.getId().equals(ownerId) || isAdmin() || permissionService.hasWriteAccess(entity))) {
      throw new ForbiddenException();
    }
  }

  protected <T extends CommonEntityDTO> List<T> listByTags(List<? extends CommonEntityExt<? extends Number>> entities,
                                                           List<String> names,
                                                           Class<T> clazz) {
    return entities.stream()
            .filter(e -> e.getTags().stream()
                    .map(tag -> tag.getName().toLowerCase(Locale.ROOT))
                    .collect(Collectors.toList())
                    .containsAll(names))
            .map(entity -> {
              T dto = conversionService.convert(entity, clazz);
              permissionService.fillWriteAccess(entity, dto);
              return dto;
            })
            .collect(Collectors.toList());
  }
}
