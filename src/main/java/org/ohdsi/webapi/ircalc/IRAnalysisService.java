/*
 * Copyright 2015 Observational Health Data Sciences and Informatics [OHDSI.org].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.ircalc;

import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain2.NamedEntityGraph;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.opencsv.CSVWriter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.check.checker.ir.IRChecker;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetailsEntity;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.common.DesignImportService;
import org.ohdsi.webapi.common.generation.GenerateSqlResult;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.ircalc.dto.IRAnalysisDTO;
import org.ohdsi.webapi.ircalc.dto.IRAnalysisShortDTO;
import org.ohdsi.webapi.ircalc.dto.IRVersionFullDTO;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.security.authz.AuthorizationService;
import org.ohdsi.webapi.security.authz.UserEntity;
import org.ohdsi.webapi.security.authz.UserRepository;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.versioning.domain.IRVersion;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.ohdsi.webapi.versioning.service.VersionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.ohdsi.webapi.Constants.GENERATE_IR_ANALYSIS;
import static org.ohdsi.webapi.Constants.Params.ANALYSIS_ID;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@RestController
public class IRAnalysisService extends AbstractDaoService implements
        GeneratesNotification, IRAnalysisResource {

  private static final Logger log = LoggerFactory.getLogger(IRAnalysisService.class);
  private final static String STRATA_STATS_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/incidencerate/sql/strata_stats.sql");
  private static final String NAME = "irAnalysis";
  private static final String NO_INCIDENCE_RATE_ANALYSIS_MESSAGE = "There is no incidence rate analysis with id = %d.";
  private static final EntityGraph ANALYSIS_WITH_EXECUTION_INFO = NamedEntityGraph.loading("IncidenceRateAnalysis.withExecutionInfoList");

  private final IRAnalysisQueryBuilder queryBuilder;

  @Value("${security.defaultGlobalReadPermissions}")
  private boolean defaultGlobalReadPermissions;
  
  @Autowired
  private IncidenceRateAnalysisRepository irAnalysisRepository;

  @Autowired
  private IRExecutionInfoRepository irExecutionInfoRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JobService jobService;

  @Autowired
  private SourceService sourceService;

  @Autowired
  private GenerationUtils generationUtils;

  @Autowired
  ConversionService conversionService;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private CohortDefinitionRepository cohortDefinitionRepository;

  @Autowired
  private DesignImportService designImportService;

    @Autowired
  private IRChecker checker;

  @Autowired
  private AuthorizationService authorizationService;

  @Autowired
  private VersionService<IRVersion> versionService;

  public IRAnalysisService(final ObjectMapper objectMapper) {

     this.queryBuilder = new IRAnalysisQueryBuilder(objectMapper);
  }

  private ExecutionInfo findExecutionInfoBySourceId(Collection<ExecutionInfo> infoList, Integer sourceId) {
    for (ExecutionInfo info : infoList) {
      if (sourceId.equals(info.getId().getSourceId())) {
        return info;
      }
    }
    return null;
  }

  public static class StratifyReportItem {
    public long bits;
    public long totalPersons;
    public long timeAtRisk;
    public long cases;
  }

  public static class GenerateSqlRequest {
    public GenerateSqlRequest() {
    }

    @JsonProperty("analysisId")
    public Integer analysisId;

    @JsonProperty("expression")
    public IncidenceRateAnalysisExpression expression;

    @JsonProperty("options")
    public IRAnalysisQueryBuilder.BuildExpressionQueryOptions options;

  }
  
  private final RowMapper<AnalysisReport.Summary> summaryMapper = (rs, rowNum) -> {
    AnalysisReport.Summary summary = new AnalysisReport.Summary();
    summary.targetId = rs.getInt("target_id");
    summary.outcomeId = rs.getInt("outcome_id");
    summary.totalPersons = rs.getLong("person_count");
    summary.timeAtRisk = rs.getLong("time_at_risk");
    summary.cases = rs.getLong("cases");
    return summary;
  };

  private List<AnalysisReport.Summary> getAnalysisSummaryList(int id, Source source) {
    String tqName = "tableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String sql = "select target_id, outcome_id, sum(person_count) as person_count, sum(time_at_risk) as time_at_risk," +
      " sum(cases) as cases from @tableQualifier.ir_analysis_result where analysis_id = @id GROUP BY target_id, outcome_id";
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, "id", whitelist(id));
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), summaryMapper);
  }

  private final RowMapper<AnalysisReport.StrataStatistic> strataRuleStatisticMapper = (rs, rowNum) -> {
    AnalysisReport.StrataStatistic statistic = new AnalysisReport.StrataStatistic();

    statistic.id = rs.getInt("strata_sequence");
    statistic.name = rs.getString("name");
    statistic.targetId = rs.getInt("target_id");
    statistic.outcomeId = rs.getInt("outcome_id");

    statistic.totalPersons = rs.getLong("person_count");
    statistic.timeAtRisk = rs.getLong("time_at_risk");
    statistic.cases = rs.getLong("cases");
    return statistic;
  };

  private List<AnalysisReport.StrataStatistic> getStrataStatistics(int id, Source source) {
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, STRATA_STATS_QUERY_TEMPLATE, "results_database_schema", resultsTableQualifier, "analysis_id", whitelist(id));
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), strataRuleStatisticMapper);
  }

  private int countSetBits(long n) {
    int count = 0;
    while (n > 0) {
      n &= (n - 1);
      count++;
    }
    return count;
  }

  private String formatBitMask(Long n, int size) {
    return StringUtils.reverse(StringUtils.leftPad(Long.toBinaryString(n), size, "0"));
  }

  private final RowMapper<StratifyReportItem> stratifyResultsMapper = (rs, rowNum) -> {
    StratifyReportItem resultItem = new StratifyReportItem();
    resultItem.bits = rs.getLong("strata_mask");
    resultItem.totalPersons = rs.getLong("person_count");
    resultItem.timeAtRisk = rs.getLong("time_at_risk");
    resultItem.cases = rs.getLong("cases");
    return resultItem;
  };

  private String getStrataTreemapData(int analysisId, int targetId, int outcomeId, int inclusionRuleCount, Source source) {
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

    String query = "select strata_mask, person_count, time_at_risk, cases from @resultsTableQualifier.ir_analysis_result where analysis_id = @analysis_id and target_id = @target_id and outcome_id = @outcome_id";
    Object[] paramValues = {analysisId, targetId, outcomeId};
    String[] params = {"analysis_id", "target_id", "outcome_id"};
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, query, "resultsTableQualifier", resultsTableQualifier, params, paramValues, SessionUtils.sessionId());
    // [0] is the inclusion rule bitmask, [1] is the count of the match
    List<StratifyReportItem> items = getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), stratifyResultsMapper);

    Map<Integer, List<StratifyReportItem>> groups = new HashMap<>();
    for (StratifyReportItem item : items) {
      int bitsSet = countSetBits(item.bits);
      if (!groups.containsKey(bitsSet)) {
        groups.put(bitsSet, new ArrayList<>());
      }
      groups.get(bitsSet).add(item);
    }

    StringBuilder treemapData = new StringBuilder("{\"name\" : \"Everyone\", \"children\" : [");

    List<Integer> groupKeys = new ArrayList<>(groups.keySet());
    Collections.sort(groupKeys);
    Collections.reverse(groupKeys);

    int groupCount = 0;
    // create a nested treemap data where more matches (more bits set in string) appear higher in the hierarchy)
    for (Integer groupKey : groupKeys) {
      if (groupCount > 0) {
        treemapData.append(",");
      }

      treemapData.append(String.format("{\"name\" : \"Group %d\", \"children\" : [", groupKey));

      int groupItemCount = 0;
      for (StratifyReportItem groupItem : groups.get(groupKey)) {
        if (groupItemCount > 0) {
          treemapData.append(",");
        }

        //sb_treemap.Append("{\"name\": \"" + cohort_identifer + "\", \"size\": " + cohorts[cohort_identifer].ToString() + "}");
        treemapData.append(String.format("{\"name\": \"%s\", \"size\": %d, \"cases\": %d, \"timeAtRisk\": %d }", formatBitMask(groupItem.bits, inclusionRuleCount), groupItem.totalPersons, groupItem.cases, groupItem.timeAtRisk));
        groupItemCount++;
      }
      groupCount++;
    }

    treemapData.append(StringUtils.repeat("]}", groupCount + 1));

    return treemapData.toString();
  }

  @Override
  public List<IRAnalysisShortDTO> getIRAnalysisList() {
    return getTransactionTemplate().execute(transactionStatus -> {
      Iterable<IncidenceRateAnalysis> analysisList = this.irAnalysisRepository.findAll();
      return StreamSupport.stream(analysisList.spliterator(), false)
              //.filter(!defaultGlobalReadPermissions ? entity -> authorizationService.hasReadAccess(entity) : entity -> true)
              .map(analysis -> {
                IRAnalysisShortDTO dto = conversionService.convert(analysis, IRAnalysisShortDTO.class);
                // authorizationService.fillWriteAccess(analysis, dto);
                // authorizationService.fillReadAccess(analysis, dto);
                return dto;
              })
              .collect(Collectors.toList());
    });
  }

  @Override
  @Transactional
  public int getCountIRWithSameName(final int id, String name) {
    return irAnalysisRepository.getCountIRWithSameName(id, name);
  }

  @Override
  @Transactional
  @CacheEvict(cacheNames = org.ohdsi.webapi.security.authz.AuthorizationCacheService.CachingSetup.AUTH_INFO_CACHE, key = "@authorizationService.getAuthenticatedPrincipal().getUserId()")
  @PreAuthorize("isPermitted('create:incidence')")
  public IRAnalysisDTO createAnalysis(IRAnalysisDTO analysis) {
    Date currentTime = Calendar.getInstance().getTime();

    UserEntity user = userRepository.findByLogin(authorizationService.getCurrentUser().login()).orElseThrow();
    // it might be possible to leverage saveAnalysis() but not sure how to pull the auto ID from
    // the DB to pass it into saveAnalysis (since saveAnalysis does a findOne() at the start).
    // If there's a way to get the Entity into the persistence manager so findOne() returns this newly created entity
    // then we could create the entity here (without persist) and then call saveAnalysis within the same Tx.
    IncidenceRateAnalysis newAnalysis = new IncidenceRateAnalysis();
    newAnalysis.setName(StringUtils.trim(analysis.getName()))
            .setDescription(analysis.getDescription());
    newAnalysis.setCreatedBy(user);
    newAnalysis.setCreatedDate(currentTime);
    if (analysis.getExpression() != null) {
      IncidenceRateAnalysisDetails details = new IncidenceRateAnalysisDetails(newAnalysis);
      newAnalysis.setDetails(details);
      details.setExpression(analysis.getExpression());
    }
    else {
      newAnalysis.setDetails(null);
    }
    IncidenceRateAnalysis createdAnalysis = this.irAnalysisRepository.save(newAnalysis);
    return conversionService.convert(createdAnalysis, IRAnalysisDTO.class);
  }

    @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('read:incidence') or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, READ)")
    @Override
    @Transactional
  public IRAnalysisDTO getAnalysis(final int id) {
    return getTransactionTemplate().execute(transactionStatus -> {
      IncidenceRateAnalysis a = this.irAnalysisRepository.findById(id).orElseThrow();
      ExceptionUtils.throwNotFoundExceptionIfNull(a, String.format(NO_INCIDENCE_RATE_ANALYSIS_MESSAGE, id));
      return conversionService.convert(a, IRAnalysisDTO.class);
    });
  }

    @PreAuthorize("isPermitted('create:incidence')")
    @Override
    public IRAnalysisDTO doImport(final IRAnalysisDTO dto) {
        dto.setTags(null);
        if (dto.getExpression() != null) {
            try {
                IncidenceRateAnalysisExportExpression expression = objectMapper.readValue(
                        dto.getExpression(), IncidenceRateAnalysisExportExpression.class);
                // Create lists of ids from list of cohort definitions because we do not store
                // cohort definitions in expression now
                fillCohortIds(expression.targetIds, expression.targetCohorts);
                fillCohortIds(expression.outcomeIds, expression.outcomeCohorts);
                String strExpression = objectMapper.writeValueAsString(new IncidenceRateAnalysisExpression(expression));
                dto.setExpression(strExpression);
              } catch (Exception e) {
              log.error("Error converting expression to object", e);
              throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
        dto.setName(NameUtils.getNameWithSuffix(dto.getName(), this::getNamesLike));
        return createAnalysis(dto);
    }


    @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('read:incidence') or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, READ)")
    @Override
    @Transactional
    public IRAnalysisDTO export(final Integer id) {
      IncidenceRateAnalysis analysis = this.irAnalysisRepository.findById(id).orElseThrow();
      ExceptionUtils.throwNotFoundExceptionIfNull(analysis, String.format(NO_INCIDENCE_RATE_ANALYSIS_MESSAGE, id));

      try {
          IncidenceRateAnalysisExportExpression expression = objectMapper.readValue(
                  analysis.getDetails().getExpression(), IncidenceRateAnalysisExportExpression.class);

          // Cohorts are not stored in expression now - create lists of cohorts from
          // lists of their ids
          fillCohorts(expression.outcomeIds, expression.outcomeCohorts);
          fillCohorts(expression.targetIds, expression.targetCohorts);
          expression.outcomeCohorts.forEach(ExportUtil::clearCreateAndUpdateInfo);
          expression.targetCohorts.forEach(ExportUtil::clearCreateAndUpdateInfo);

          String strExpression = objectMapper.writeValueAsString(expression);
          analysis.getDetails().setExpression(strExpression);
        } catch (Exception e) {
          log.error("Error converting expression to object", e);
          throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        }
      IRAnalysisDTO irAnalysisDTO = conversionService.convert(analysis, IRAnalysisDTO.class);
      ExportUtil.clearCreateAndUpdateInfo(irAnalysisDTO);

      return irAnalysisDTO;
    }

    @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, WRITE)")
    @Override
    @Transactional
  public IRAnalysisDTO saveAnalysis(final int id, IRAnalysisDTO analysis) {
    Date currentTime = Calendar.getInstance().getTime();

    saveVersion(id);

    UserEntity user = userRepository.findByLogin(authorizationService.getCurrentUser().login()).orElseThrow();
    IncidenceRateAnalysis updatedAnalysis = this.irAnalysisRepository.findById(id).orElseThrow();
    updatedAnalysis.setName(StringUtils.trim(analysis.getName()))
            .setDescription(analysis.getDescription());
    updatedAnalysis.setModifiedBy(user);
    updatedAnalysis.setModifiedDate(currentTime);
    
    if (analysis.getExpression() != null) {
      
      IncidenceRateAnalysisDetails details = updatedAnalysis.getDetails();
      if (details == null) {
        details = new IncidenceRateAnalysisDetails(updatedAnalysis);
        updatedAnalysis.setDetails(details);
      }
      details.setExpression(analysis.getExpression());
    }
    else
      updatedAnalysis.setDetails(null);
    
    this.irAnalysisRepository.save(updatedAnalysis);
    return getAnalysis(id);
  }

  @PreAuthorize("(isOwner(#analysisId, INCIDENCE_RATE) or isAnyPermitted(anyOf('read:incidence','write:incidence')) or hasEntityAccess(#analysisId, INCIDENCE_RATE, READ)) and (isPermitted('write:source') or hasSourceAccess(#sourceKey, WRITE))")
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  @Override
  public JobExecutionResource performAnalysis(final int analysisId, final String sourceKey) {
    IRAnalysisDTO irAnalysisDTO = getAnalysis(analysisId);
    CheckResult checkResult = runDiagnostics(irAnalysisDTO);
    if (checkResult.hasCriticalErrors()) {
      throw new RuntimeException("Cannot be generated due to critical errors in design. Call 'check' service for further details");
    }

    Date startTime = Calendar.getInstance().getTime();

    Source source = this.getSourceRepository().findBySourceKey(sourceKey);

    ExceptionUtils.throwNotFoundExceptionIfNull(source, String.format("There is no source with sourceKey = %s", sourceKey));

    DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
    requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    TransactionStatus initStatus = this.getTransactionTemplate().getTransactionManager().getTransaction(requresNewTx);

    IncidenceRateAnalysis analysis = this.irAnalysisRepository.findOneWithExecutionsOnExistingSources(analysisId, ANALYSIS_WITH_EXECUTION_INFO);

    ExecutionInfo analysisInfo = findExecutionInfoBySourceId(analysis.getExecutionInfoList(), source.getSourceId());
    if (analysisInfo != null) {
      if (analysisInfo.getStatus() != GenerationStatus.COMPLETE)
        return null; // Exit execution, another process has started it.
    }
    else {
      analysisInfo = new ExecutionInfo(analysis, source);
      analysis.getExecutionInfoList().add(analysisInfo);
    }
    
    analysisInfo.setStatus(GenerationStatus.PENDING)
            .setStartTime(startTime)
            .setExecutionDuration(null);

    this.irAnalysisRepository.save(analysis);

    this.getTransactionTemplate().getTransactionManager().commit(initStatus);

    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString(JOB_NAME, String.format("IR Analysis: %d: %s (%s)", analysis.getId(), source.getSourceName(), source.getSourceKey()));
    builder.addString(ANALYSIS_ID, String.valueOf(analysisId));
    builder.addString(SOURCE_ID, String.valueOf(source.getSourceId()));

    SimpleJobBuilder generateIrJob = generationUtils.buildJobForCohortBasedAnalysisTasklet(
      GENERATE_IR_ANALYSIS,
      source,
      builder,
      getSourceJdbcTemplate(source),
      chunkContext -> {
          Integer irId = Integer.valueOf(chunkContext.getStepContext().getJobParameters().get(ANALYSIS_ID).toString());
          IncidenceRateAnalysis ir = this.irAnalysisRepository.findById(irId).orElseThrow();
          IncidenceRateAnalysisExpression expression = Utils.deserialize(ir.getDetails().getExpression(), IncidenceRateAnalysisExpression.class);
          return Stream.concat(
              expression.targetIds.stream(),
              expression.outcomeIds.stream()
            ).map(id -> cohortDefinitionRepository.findOneWithDetail(id))
            .collect(Collectors.toList());
      },
      new IRAnalysisTasklet(getSourceJdbcTemplate(source), getTransactionTemplate(), irAnalysisRepository, sourceService, queryBuilder, objectMapper)
    );

    generateIrJob.listener(new IRAnalysisInfoListener(getTransactionTemplate(), irAnalysisRepository));

    final JobParameters jobParameters = builder.toJobParameters();

    return jobService.runJob(generateIrJob.build(), jobParameters);
  }

  @PreAuthorize("(isOwner(#analysisId, INCIDENCE_RATE) or isAnyPermitted(anyOf('read:incidence','write:incidence')) or hasEntityAccess(#analysisId, INCIDENCE_RATE, READ)) and (isPermitted('write:source') or hasSourceAccess(#sourceKey, WRITE))")
  @Override
  public void cancelAnalysis(int analysisId, String sourceKey) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
      jobService.cancelJobExecution(j -> {
          JobParameters jobParameters = j.getJobParameters();
          String jobName = j.getJobInstance().getJobName();
          return Objects.equals(jobParameters.getString(ANALYSIS_ID), String.valueOf(analysisId))
                  && Objects.equals(jobParameters.getString(SOURCE_ID), String.valueOf(source.getSourceId()))
                  && Objects.equals(NAME, jobName);
      });
  }

  @Override
  @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('read:incidence') or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, READ)")
  @Transactional(readOnly = true)
  public List<AnalysisInfoDTO> getAnalysisInfo(final int id) {

    List<ExecutionInfo> executionInfoList = irExecutionInfoRepository.findByAnalysisId(id);
    return executionInfoList.stream().map(ei -> {
      AnalysisInfoDTO info = new AnalysisInfoDTO();
      info.setExecutionInfo(ei);
      return info;
    }).collect(Collectors.toList());
  }

  @Override
  @PreAuthorize("(isOwner(#id, INCIDENCE_RATE) or isAnyPermitted(anyOf('read:incidence','write:incidence')) or hasEntityAccess(#id, INCIDENCE_RATE, READ)) and (isAnyPermitted(anyOf('read:source','write:source')) or hasSourceAccess(#sourceKey, READ))")
  @Transactional(readOnly = true)
  public AnalysisInfoDTO getAnalysisInfo(int id, String sourceKey) {

    Source source = sourceService.findBySourceKey(sourceKey);
    ExceptionUtils.throwNotFoundExceptionIfNull(source, String.format("There is no source with sourceKey = %s", sourceKey));
    AnalysisInfoDTO info = new AnalysisInfoDTO();
    List<ExecutionInfo> executionInfoList = irExecutionInfoRepository.findByAnalysisId(id);
    info.setExecutionInfo(executionInfoList.stream().filter(i -> Objects.equals(i.getSource(), source))
            .findFirst().orElse(null));
    try{
      if (Objects.nonNull(info.getExecutionInfo()) && Objects.equals(info.getExecutionInfo().getStatus(), GenerationStatus.COMPLETE)
        && info.getExecutionInfo().getIsValid()) {
        info.setSummaryList(getAnalysisSummaryList(id, source));
      }
    }catch (Exception e) {
      log.error("Error getting IR Analysis summary list", e);
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
    }
    return info;
  }

  @Override
  @PreAuthorize("(isOwner(#id, INCIDENCE_RATE) or isPermitted('read:incidence') or isAnyPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, READ)) and (isAnyPermitted(anyOf('read:source','write:source')) or hasSourceAccess(#sourceKey, READ))")
  @Transactional
  public AnalysisReport getAnalysisReport(final int id, final String sourceKey, final int targetId, final int outcomeId ) {

    Source source = this.getSourceRepository().findBySourceKey(sourceKey);

    AnalysisReport.Summary summary = IterableUtils.find(getAnalysisSummaryList(id, source), summary12 -> ((summary12.targetId == targetId) && (summary12.outcomeId == outcomeId)));

    Collection<AnalysisReport.StrataStatistic> strataStats = CollectionUtils.select(getStrataStatistics(id, source),
            summary1 -> ((summary1.targetId == targetId) && (summary1.outcomeId == outcomeId)));
    String treemapData = getStrataTreemapData(id, targetId, outcomeId, strataStats.size(), source);

    AnalysisReport report = new AnalysisReport();
    report.summary = summary;
    report.stratifyStats = new ArrayList<>(strataStats);
    report.treemapData = treemapData;

    return report;
  }

  @Override
  public GenerateSqlResult generateSql(GenerateSqlRequest request) {
    IRAnalysisQueryBuilder.BuildExpressionQueryOptions options = request.options;
    GenerateSqlResult result = new GenerateSqlResult();
    if (options == null) {
      options = new IRAnalysisQueryBuilder.BuildExpressionQueryOptions();
    }
    String expressionSql = queryBuilder.buildAnalysisQuery(request.expression, request.analysisId, options);
    result.templateSql = SqlRender.renderSql(expressionSql, null, null);

    return result;
  }

    @Override
    public CheckResult runDiagnostics(IRAnalysisDTO irAnalysisDTO){

        return new CheckResult(checker.check(irAnalysisDTO));
    }

  @Override
  @Transactional
  @CacheEvict(cacheNames = org.ohdsi.webapi.security.authz.AuthorizationCacheService.CachingSetup.AUTH_INFO_CACHE, key = "@authorizationService.getAuthenticatedPrincipal().getUserId()")
  @PreAuthorize("(isOwner(#id, INCIDENCE_RATE) or isAnyPermitted(anyOf('read:incidence','write:incidence')) or hasEntityAccess(#id, INCIDENCE_RATE, READ)) and isPermitted('create:incidence')")
  public IRAnalysisDTO copy(final int id) {
    IRAnalysisDTO analysis = getAnalysis(id);
    analysis.setTags(null);
    analysis.setId(null); // clear the ID
    analysis.setName(getNameForCopy(analysis.getName()));
    return createAnalysis(analysis);
  }


  @Override
  @Transactional
  @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isAnyPermitted(anyOf('read:incidence','write:incidence')) or hasEntityAccess(#id, INCIDENCE_RATE, READ)")
  public ResponseEntity<byte[]> export(final int id) {

    Map<String, String> fileList = new HashMap<>();
    Map<Integer, String> distTypeLookup = ImmutableMap.of(1, "TAR", 2, "TTO");

    try {
      IncidenceRateAnalysis analysis = this.irAnalysisRepository.findById(id).orElseThrow();
      Set<ExecutionInfo> executions = analysis.getExecutionInfoList();

      fileList.put("analysisDefinition.json", analysis.getDetails().getExpression());

      // squentially return reults of IR calculation.  In Spring 1.4.2, we can utlilize @Async operations to do this in parallel.
      // store results in single CSV file
      ArrayList<String[]> summaryLines = new ArrayList<>();
      ArrayList<String[]> strataLines = new ArrayList<>();
      ArrayList<String[]> distLines = new ArrayList<>();

      executions = executions.stream().filter(e -> this.isSourceAvailable(e.getSource())).collect(Collectors.toSet());
      for (ExecutionInfo execution : executions)
      {
        Source source = execution.getSource();
        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        // get the summary data
        List<AnalysisReport.Summary> summaryList = getAnalysisSummaryList(id, source);
        if (summaryLines.isEmpty())
        {
          summaryLines.add("db_id#targetId#outcomeId#total#timeAtRisk#cases".split("#"));
        }
        for (AnalysisReport.Summary summary : summaryList)
        {
          summaryLines.add(new String[] {source.getSourceKey(),String.valueOf(summary.targetId), String.valueOf(summary.outcomeId), String.valueOf(summary.totalPersons), String.valueOf(summary.timeAtRisk), String.valueOf(summary.cases)});
        }

        // get the strata results
        List<AnalysisReport.StrataStatistic> strataList = getStrataStatistics(id, source);
        if (strataLines.isEmpty())
        {
          strataLines.add("db_id#targetId#outcomeId#strata_id#strata_name#total#timeAtRisk#cases".split("#"));
        }
        for (AnalysisReport.StrataStatistic strata : strataList)
        {
          strataLines.add(new String[] {source.getSourceKey(),String.valueOf(strata.targetId), String.valueOf(strata.outcomeId),String.valueOf(strata.id), String.valueOf(strata.name), String.valueOf(strata.totalPersons), String.valueOf(strata.timeAtRisk), String.valueOf(strata.cases)});
        }        

        // get the distribution data
        String distQuery = String.format("select '%s' as db_id, target_id, outcome_id, strata_sequence, dist_type, total, avg_value, std_dev, min_value, p10_value, p25_value, median_value, p75_value, p90_value, max_value from %s.ir_analysis_dist where analysis_id = %d", source.getSourceKey(), resultsTableQualifier, id);
        String translatedSql = SqlTranslate.translateSql(distQuery, source.getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);

        this.getSourceJdbcTemplate(source).query(translatedSql, resultSet -> {
          if (distLines.isEmpty()) {
            ArrayList<String> columnNames = new ArrayList<>();
            for(int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
              columnNames.add(resultSet.getMetaData().getColumnName(i));
            }
            distLines.add(columnNames.toArray(new String[0]));
          }
          ArrayList<String> columnValues = new ArrayList<>();
          for(int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
            switch (resultSet.getMetaData().getColumnName(i)) {
              case "dist_type":
                columnValues.add(distTypeLookup.get(resultSet.getInt(i)));
                break;
              default:
                columnValues.add(resultSet.getString(i));
                break;
            }
          }
          distLines.add(columnValues.toArray(new String[0]));
        });
      }

      // Write report lines to CSV
      StringWriter sw = null;
      CSVWriter csvWriter = null;

      sw = new StringWriter();
      csvWriter = new CSVWriter(sw);
      csvWriter.writeAll(summaryLines);
      csvWriter.flush();
      fileList.put("ir_summary.csv", sw.getBuffer().toString());

      sw = new StringWriter();
      csvWriter = new CSVWriter(sw);
      csvWriter.writeAll(strataLines);
      csvWriter.flush();      
      fileList.put("ir_strata.csv", sw.getBuffer().toString());

      sw = new StringWriter();
      csvWriter = new CSVWriter(sw);
      csvWriter.writeAll(distLines);
      csvWriter.flush();      
      fileList.put("ir_dist.csv", sw.getBuffer().toString());

      // build zip output
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ZipOutputStream zos = new ZipOutputStream(baos);

      for(String fileName : fileList.keySet())
      {
        ZipEntry resultsEntry = new ZipEntry(fileName);
        zos.putNextEntry(resultsEntry);
        zos.write(fileList.get(fileName).getBytes());
      }

      zos.closeEntry();
      zos.close();
      baos.flush();
      baos.close();      

      byte[] payload = baos.toByteArray();
      HttpHeaders headers = new HttpHeaders();
      headers.add(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", "ir_analysis_" + id + ".zip"));
      return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM).body(payload);
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), ex);
    }
    
  }

  @Override
  @Transactional
  @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, WRITE)")
  public void delete(final int id) {
    irAnalysisRepository.deleteById(id);
  }

  @Override
  @Transactional
  @PreAuthorize("(isOwner(#id, INCIDENCE_RATE) or isAnyPermitted(anyOf('read:incidence','write:incidence')) or hasEntityAccess(#id, INCIDENCE_RATE, READ)) and (isPermitted('write:source') or hasSourceAccess(#sourceKey, WRITE))")
  public void deleteInfo(final int id, final String sourceKey) {
    IncidenceRateAnalysis analysis = irAnalysisRepository.findById(id).orElseThrow();
    ExecutionInfo itemToRemove = null;
    for (ExecutionInfo info : analysis.getExecutionInfoList())
    {
      if (info.getSource().getSourceKey().equals(sourceKey))
        itemToRemove = info;
    }

    if (itemToRemove != null)
      analysis.getExecutionInfoList().remove(itemToRemove);

    irAnalysisRepository.save(analysis);
  }

  @Override
  @Transactional
  @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('admin:tags') or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, WRITE)")
  public void assignTag(final Integer id, final int tagId) {
    IncidenceRateAnalysis entity = irAnalysisRepository.findById(id).orElseThrow();
    assignTag(entity, tagId);
  }

  @Override
  @Transactional
  @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('admin:tags') or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, WRITE)")
  public void unassignTag(final Integer id, final int tagId) {
    IncidenceRateAnalysis entity = irAnalysisRepository.findById(id).orElseThrow();
    unassignTag(entity, tagId);
  }

  @Override
  @Transactional
  @PreAuthorize("(isOwner(#id, INCIDENCE_RATE) or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, WRITE)) and isPermitted('admin:tags')")
  public void assignPermissionProtectedTag(final int id, final int tagId) {
    assignTag(id, tagId);
  }

  @Override
  @Transactional
  @PreAuthorize("(isOwner(#id, INCIDENCE_RATE) or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, WRITE)) and isPermitted('admin:tags')")
  public void unassignPermissionProtectedTag(final int id, final int tagId) {
    unassignTag(id, tagId);
  }

  @Override
  @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('read:incidence') or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, READ)")
  public List<VersionDTO> getVersions(long id) {
    List<VersionBase> versions = versionService.getVersions(VersionType.INCIDENCE_RATE, id);
    return versions.stream()
            .map(v -> conversionService.convert(v, VersionDTO.class))
            .collect(Collectors.toList());
  }

  @Override
  @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('read:incidence') or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, READ)")
  @Transactional
  public IRVersionFullDTO getVersion(int id, int version) {
    checkVersion(id, version, false);
    IRVersion irVersion = versionService.getById(VersionType.INCIDENCE_RATE, id, version);
    return conversionService.convert(irVersion, IRVersionFullDTO.class);
  }

  @Override
  @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, WRITE)")
  @Transactional
  public VersionDTO updateVersion(int id, int version, VersionUpdateDTO updateDTO) {
    checkVersion(id, version);
    updateDTO.setAssetId(id);
    updateDTO.setVersion(version);
    IRVersion updated = versionService.update(VersionType.INCIDENCE_RATE, updateDTO);

    return conversionService.convert(updated, VersionDTO.class);
  }

  @Override
  @PreAuthorize("isOwner(#id, INCIDENCE_RATE) or isPermitted('write:incidence') or hasEntityAccess(#id, INCIDENCE_RATE, WRITE)")
  @Transactional
  public void deleteVersion(int id, int version) {
    checkVersion(id, version);
    versionService.delete(VersionType.INCIDENCE_RATE, id, version);
  }

  @Override
  @CacheEvict(cacheNames = org.ohdsi.webapi.security.authz.AuthorizationCacheService.CachingSetup.AUTH_INFO_CACHE, key = "@authorizationService.getAuthenticatedPrincipal().getUserId()")
  @PreAuthorize("(isOwner(#id, INCIDENCE_RATE) or isAnyPermitted(anyOf('read:incidence','write:incidence')) or hasEntityAccess(#id, INCIDENCE_RATE, READ)) and isPermitted('create:incidence')")
  @Transactional
  public IRAnalysisDTO copyAssetFromVersion(int id, int version) {
    checkVersion(id, version, false);
    IRVersion irVersion = versionService.getById(VersionType.INCIDENCE_RATE, id, version);
    IRVersionFullDTO fullDTO = conversionService.convert(irVersion, IRVersionFullDTO.class);

    IRAnalysisDTO dto = fullDTO.getEntityDTO();
    dto.setId(null);
    dto.setTags(null);
    dto.setName(NameUtils.getNameForCopy(dto.getName(), this::getNamesLike,
            irAnalysisRepository.findByName(dto.getName())));
    return createAnalysis(dto);
  }

  @Override
  @Transactional
  public List<IRAnalysisDTO> listByTags(TagNameListRequestDTO requestDTO) {
    if (requestDTO == null || requestDTO.getNames() == null || requestDTO.getNames().isEmpty()) {
      return Collections.emptyList();
    }
    List<String> names = requestDTO.getNames().stream()
            .map(name -> name.toLowerCase(Locale.ROOT))
            .collect(Collectors.toList());
    List<IncidenceRateAnalysis> entities = irAnalysisRepository.findByTags(names);
    return listByTags(entities, names, IRAnalysisDTO.class);
  }

  @PostConstruct
  public void init() {

    invalidateIRExecutions();
  }

  @Override
  public String getJobName() {
    return NAME;
  }

  @Override
  public String getExecutionFoldingKey() {
    return ANALYSIS_ID;
  }

  private void invalidateIRExecutions() {

    getTransactionTemplateRequiresNew().execute(status -> {

      List<ExecutionInfo> executions = irExecutionInfoRepository.findByStatusIn(INVALIDATE_STATUSES);
      invalidateExecutions(executions);
      irExecutionInfoRepository.saveAll(executions);
      return null;
    });
  }

  private String getNameForCopy(String dtoName) {
    return NameUtils.getNameForCopy(dtoName, this::getNamesLike, irAnalysisRepository.findByName(dtoName));
  }

  private List<String> getNamesLike(String name) {
    return irAnalysisRepository.findAllByNameStartsWith(name).stream().map(IncidenceRateAnalysis::getName).collect(Collectors.toList());
  }

  private void fillCohorts(List<Integer> outcomeIds, List<CohortDTO> cohortDefinitions) {
    cohortDefinitions.clear();
    for (Integer cohortId : outcomeIds) {
      CohortDefinitionEntity cohortDefinition = cohortDefinitionRepository.findById(cohortId).orElseThrow();
      if (Objects.isNull(cohortDefinition)) {
        // Pass cohort without name to client if no cohort definition found
        cohortDefinition = new CohortDefinitionEntity();
        cohortDefinition.setId(cohortId);
        CohortDefinitionDetailsEntity details = new CohortDefinitionDetailsEntity();
        details.setCohortDefinition(cohortDefinition);
      }
      cohortDefinitions.add(conversionService.convert(cohortDefinition, CohortDTO.class));
    }
  }

  private void fillCohortIds(List<Integer> ids, List<CohortDTO> cohortDTOS) {
    ids.clear();
    for(CohortDTO cohortDTO: cohortDTOS) {
      CohortDefinitionEntity definition = conversionService.convert(cohortDTO, CohortDefinitionEntity.class);
      definition = designImportService.persistCohortOrGetExisting(definition);
      ids.add(definition.getId());
    }
    cohortDTOS.clear();
  }


  private boolean isSourceAvailable(Source source) {
    boolean sourceAvailable = true;
    try {
      sourceService.checkConnection(source);
    } catch (Exception e) {
      log.error("cannot get connection to source with key {}", source.getSourceKey(), e);
      sourceAvailable = false;
    }
    return sourceAvailable;
  }

  private void checkVersion(int id, int version) {
    checkVersion(id, version, true);
  }

  private void checkVersion(int id, int version, boolean checkOwnerShip) {
    Version irVersion = versionService.getById(VersionType.INCIDENCE_RATE, id, version);
    ExceptionUtils.throwNotFoundExceptionIfNull(irVersion,
            String.format("There is no incidence rates analysis version with id = %d.", version));

    IncidenceRateAnalysis entity = this.irAnalysisRepository.findById(id).orElseThrow();
    if (checkOwnerShip) {
      // TODO: Do we need check ownership under the new permission strategy
      // authorizationService.isOwner(id, EntityType.INCIDENCE_RATE);
    }
  }

  private IRVersion saveVersion(int id) {
    IncidenceRateAnalysis def = this.irAnalysisRepository.findById(id).orElseThrow();
    IRVersion version = conversionService.convert(def, IRVersion.class);

    UserEntity user = Objects.nonNull(def.getModifiedBy()) ? def.getModifiedBy() : def.getCreatedBy();
    Date versionDate = Objects.nonNull(def.getModifiedDate()) ? def.getModifiedDate() : def.getCreatedDate();
    version.setCreatedBy(user);
    version.setCreatedDate(versionDate);
    return versionService.create(VersionType.INCIDENCE_RATE, version);
  }
}
