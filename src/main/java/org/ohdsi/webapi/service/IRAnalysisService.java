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
package org.ohdsi.webapi.service;

import static org.ohdsi.webapi.Constants.GENERATE_IR_ANALYSIS;
import static org.ohdsi.webapi.Constants.Params.*;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.opencsv.CSVWriter;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.ircalc.AnalysisReport;
import org.ohdsi.webapi.ircalc.ExecutionInfo;
import org.ohdsi.webapi.ircalc.IRAnalysisInfoListener;
import org.ohdsi.webapi.ircalc.IRExecutionInfoRepository;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisDetails;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExpression;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.ircalc.IRAnalysisTasklet;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Component
public class IRAnalysisService extends AbstractDaoService implements GeneratesNotification, IRAnalysisResource {

  private static final Logger log = LoggerFactory.getLogger(IRAnalysisService.class);
  private final static String STRATA_STATS_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/incidencerate/sql/strata_stats.sql");
  private static final String NAME = "irAnalysis";
  private static final String NO_INCIDENCE_RATE_ANALYSIS_MESSAGE = "There is no incidence rate analysis with id = %d.";
  private static final EntityGraph ANALYSIS_WITH_EXECUTION_INFO = EntityGraphUtils.fromName("IncidenceRateAnalysis.withExecutionInfoList");

  @Autowired
  private IncidenceRateAnalysisRepository irAnalysisRepository;

  @Autowired
  private IRExecutionInfoRepository irExecutionInfoRepository;

  @Autowired
  private JobBuilderFactory jobBuilders;

  @Autowired
  private StepBuilderFactory stepBuilders;

  @Autowired
  private JobTemplate jobTemplate;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private JobService jobService;

  @Autowired
  private Security security;

  @Autowired
  private SourceService sourceService;

  @Autowired
  private GenerationUtils generationUtils;

  @Autowired
  private SourceAccessor sourceAccessor;

  @Context
  ServletContext context;
  
  private ExecutionInfo findExecutionInfoBySourceId(Collection<ExecutionInfo> infoList, Integer sourceId) {
    for (ExecutionInfo info : infoList) {
      if (sourceId.equals(info.getId().getSourceId())) {
        return info;
      }
    }
    return null;
  }

  public static class IRAnalysisListItem {

    public Integer id;
    public String name;
    public String description;
    public String createdBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    public Date createdDate;
    public String modifiedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm")
    public Date modifiedDate;
  }

  public static class IRAnalysisDTO extends IRAnalysisListItem {
    public String expression;
  }

  public static class AnalysisInfoDTO {
    public ExecutionInfo executionInfo;
    public List<AnalysisReport.Summary> summaryList = new ArrayList<>();
  }
  
  public static class StratifyReportItem {
    public long bits;
    public long totalPersons;
    public long timeAtRisk;
    public long cases;
  }
  
  private final RowMapper<AnalysisReport.Summary> summaryMapper = new RowMapper<AnalysisReport.Summary>() {
    @Override
    public AnalysisReport.Summary mapRow(ResultSet rs, int rowNum) throws SQLException {
      AnalysisReport.Summary summary = new AnalysisReport.Summary();
      summary.targetId = rs.getInt("target_id");
      summary.outcomeId = rs.getInt("outcome_id");
      summary.totalPersons = rs.getLong("person_count");
      summary.timeAtRisk = rs.getLong("time_at_risk");
      summary.cases = rs.getLong("cases");
      return summary;
    }
  };

  private List<AnalysisReport.Summary> getAnalysisSummaryList(int id, Source source) {
    String tqName = "tableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String sql = "select target_id, outcome_id, sum(person_count) as person_count, sum(time_at_risk) as time_at_risk," +
      " sum(cases) as cases from @tableQualifier.ir_analysis_result where analysis_id = @id GROUP BY target_id, outcome_id";
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, "id", whitelist(id));
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), summaryMapper);
  }

  private final RowMapper<AnalysisReport.StrataStatistic> strataRuleStatisticMapper = new RowMapper<AnalysisReport.StrataStatistic>() {

    @Override
    public AnalysisReport.StrataStatistic mapRow(ResultSet rs, int rowNum) throws SQLException {
      AnalysisReport.StrataStatistic statistic = new AnalysisReport.StrataStatistic();

      statistic.id = rs.getInt("strata_sequence");
      statistic.name = rs.getString("name");
      statistic.targetId = rs.getInt("target_id");
      statistic.outcomeId = rs.getInt("outcome_id");
      
      statistic.totalPersons = rs.getLong("person_count");
      statistic.timeAtRisk = rs.getLong("time_at_risk");
      statistic.cases = rs.getLong("cases");
      return statistic;
    }
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

  private final RowMapper<StratifyReportItem> stratifyResultsMapper = new RowMapper<StratifyReportItem>() {

    @Override
    public StratifyReportItem mapRow(ResultSet rs, int rowNum) throws SQLException {
      StratifyReportItem resultItem = new StratifyReportItem();
      resultItem.bits = rs.getLong("strata_mask");
      resultItem.totalPersons = rs.getLong("person_count");
      resultItem.timeAtRisk = rs.getLong("time_at_risk");
      resultItem.cases = rs.getLong("cases");
      return resultItem;
    }
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
        groups.put(bitsSet, new ArrayList<StratifyReportItem>());
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

  public IRAnalysisDTO analysisToDTO(IncidenceRateAnalysis  analysis) {
    IRAnalysisDTO aDTO = new IRAnalysisDTO();
    aDTO.id = analysis.getId();
    aDTO.name = analysis.getName();
    aDTO.description = analysis.getDescription();
    aDTO.createdBy = UserUtils.nullSafeLogin(analysis.getCreatedBy());
    aDTO.createdDate = analysis.getCreatedDate();
    aDTO.modifiedBy = UserUtils.nullSafeLogin(analysis.getModifiedBy());
    aDTO.modifiedDate = analysis.getModifiedDate();
    aDTO.expression = analysis.getDetails() != null ? analysis.getDetails().getExpression() : null;

    return aDTO;
  }


  @Override
  public List<IRAnalysisService.IRAnalysisListItem> getIRAnalysisList() {

    return getTransactionTemplate().execute(transactionStatus -> {
      Iterable<IncidenceRateAnalysis> analysisList = this.irAnalysisRepository.findAll();
      return StreamSupport.stream(analysisList.spliterator(), false).map(p -> {
        IRAnalysisService.IRAnalysisListItem item = new IRAnalysisService.IRAnalysisListItem();
        item.id = p.getId();
        item.name = p.getName();
        item.description = p.getDescription();
        item.createdBy = UserUtils.nullSafeLogin(p.getCreatedBy());
        item.createdDate = p.getCreatedDate();
        item.modifiedBy = UserUtils.nullSafeLogin(p.getModifiedBy());
        item.modifiedDate = p.getModifiedDate();
        return item;
      }).collect(Collectors.toList());
    });
  }

  @Override
  public IRAnalysisDTO createAnalysis(IRAnalysisDTO analysis) {
    Date currentTime = Calendar.getInstance().getTime();

    UserEntity user = userRepository.findByLogin(security.getSubject());
    // it might be possible to leverage saveAnalysis() but not sure how to pull the auto ID from
    // the DB to pass it into saveAnalysis (since saveAnalysis does a findOne() at the start).
    // If there's a way to get the Entity into the persistence manager so findOne() returns this newly created entity
    // then we could create the entity here (without persist) and then call saveAnalysis within the same Tx.
    IncidenceRateAnalysis newAnalysis = new IncidenceRateAnalysis();
    newAnalysis.setName(analysis.name)
            .setDescription(analysis.description);
    newAnalysis.setCreatedBy(user);
    newAnalysis.setCreatedDate(currentTime);
    if (analysis.expression != null) {
      IncidenceRateAnalysisDetails details = new IncidenceRateAnalysisDetails(newAnalysis);
      newAnalysis.setDetails(details);
      details.setExpression(analysis.expression);
    }
    else
      newAnalysis.setDetails(null);
    
    IncidenceRateAnalysis createdAnalysis = this.irAnalysisRepository.save(newAnalysis);
    return analysisToDTO(createdAnalysis);
  }

  @Override
  public IRAnalysisDTO getAnalysis(final int id) {

    return getTransactionTemplate().execute(transactionStatus -> {
      IncidenceRateAnalysis a = this.irAnalysisRepository.findOne(id);
      ExceptionUtils.throwNotFoundExceptionIfNull(a, String.format(NO_INCIDENCE_RATE_ANALYSIS_MESSAGE, id));
      return analysisToDTO(a);
    });
  }

  @Override
  public IRAnalysisDTO saveAnalysis(final int id, IRAnalysisDTO analysis) {
    Date currentTime = Calendar.getInstance().getTime();

    UserEntity user = userRepository.findByLogin(security.getSubject());
    IncidenceRateAnalysis updatedAnalysis = this.irAnalysisRepository.findOne(id);
    updatedAnalysis.setName(analysis.name)
            .setDescription(analysis.description);
    updatedAnalysis.setModifiedBy(user);
    updatedAnalysis.setModifiedDate(currentTime);
    
    if (analysis.expression != null) {
      
      IncidenceRateAnalysisDetails details = updatedAnalysis.getDetails();
      if (details == null) {
        details = new IncidenceRateAnalysisDetails(updatedAnalysis);
        updatedAnalysis.setDetails(details);
      }
      details.setExpression(analysis.expression);
    }
    else
      updatedAnalysis.setDetails(null);
    
    this.irAnalysisRepository.save(updatedAnalysis);
    return getAnalysis(id);
  }

  @Override
  public JobExecutionResource performAnalysis(final int analysisId, final String sourceKey) {
    Date startTime = Calendar.getInstance().getTime();

    Source source = this.getSourceRepository().findBySourceKey(sourceKey);

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
          IncidenceRateAnalysis ir = this.irAnalysisRepository.findOne(irId);
          IncidenceRateAnalysisExpression expression = Utils.deserialize(ir.getDetails().getExpression(), IncidenceRateAnalysisExpression.class);
          return Stream.concat(
              expression.targetIds.stream(),
              expression.outcomeIds.stream()
            ).map(id -> {
              CohortDefinition cd = new CohortDefinition();
              cd.setId(id);
              return cd;
            })
            .collect(Collectors.toList());
      },
      new IRAnalysisTasklet(getSourceJdbcTemplate(source), getTransactionTemplate(), irAnalysisRepository, sourceService)
    );

    generateIrJob.listener(new IRAnalysisInfoListener(getTransactionTemplate(), irAnalysisRepository));

    final JobParameters jobParameters = builder.toJobParameters();

    return jobService.runJob(generateIrJob.build(), jobParameters);
  }

  @Override
  public void cancelAnalysis(int analysisId, String sourceKey) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    jobService.cancelJobExecution(NAME, j -> {
      JobParameters jobParameters = j.getJobParameters();
      return Objects.equals(jobParameters.getString(ANALYSIS_ID), String.valueOf(analysisId))
              && Objects.equals(jobParameters.getString(SOURCE_ID), String.valueOf(source.getSourceId()));
    });
  }

  @Override
  public List<AnalysisInfoDTO> getAnalysisInfo(final int id) {

    List<ExecutionInfo> executionInfoList = irExecutionInfoRepository.findByAnalysisId(id);
    return executionInfoList.stream().map(ei -> {
      AnalysisInfoDTO info = new AnalysisInfoDTO();
      info.executionInfo = ei;
      return info;
    }).collect(Collectors.toList());
  }

  @Override
  @DataSourceAccess
  public AnalysisInfoDTO getAnalysisInfo(int id, @SourceKey String sourceKey) {

    Source source = sourceService.findBySourceKey(sourceKey);
    ExceptionUtils.throwNotFoundExceptionIfNull(source, String.format("There is no source with sourceKey = %s", sourceKey));
    AnalysisInfoDTO info = new AnalysisInfoDTO();
    List<ExecutionInfo> executionInfoList = irExecutionInfoRepository.findByAnalysisId(id);
    info.executionInfo = executionInfoList.stream().filter(i -> Objects.equals(i.getSource(), source))
            .findFirst().orElse(null);
    try{
      if (Objects.nonNull(info.executionInfo) && Objects.equals(info.executionInfo.getStatus(), GenerationStatus.COMPLETE)
        && info.executionInfo.getIsValid()) {
        info.summaryList = getAnalysisSummaryList(id, source);
      }
    }catch (Exception e) {
      log.error("Error getting IR Analysis summary list", e);
      throw new InternalServerErrorException();
    }
    return info;
  }

  @Override
  public AnalysisReport getAnalysisReport(final int id, final String sourceKey, final int targetId, final int outcomeId ) {

    Source source = this.getSourceRepository().findBySourceKey(sourceKey);

    AnalysisReport.Summary summary = IterableUtils.find(getAnalysisSummaryList(id, source), new Predicate<AnalysisReport.Summary>() {
      @Override
      public boolean evaluate(AnalysisReport.Summary summary) {
        return ((summary.targetId == targetId) && (summary.outcomeId == outcomeId));
      }
    });

    Collection<AnalysisReport.StrataStatistic> strataStats = CollectionUtils.select(getStrataStatistics(id, source), new Predicate<AnalysisReport.StrataStatistic>() {
      @Override
      public boolean evaluate(AnalysisReport.StrataStatistic summary) {
        return ((summary.targetId == targetId) && (summary.outcomeId == outcomeId));
      }
    });
    String treemapData = getStrataTreemapData(id, targetId, outcomeId, strataStats.size(), source);

    AnalysisReport report = new AnalysisReport();
    report.summary = summary;
    report.stratifyStats = new ArrayList<>(strataStats);
    report.treemapData = treemapData;

    return report;
  }

  @Override
  public IRAnalysisDTO copy(final int id) {
    IRAnalysisDTO analysis = getAnalysis(id);
    analysis.id = null; // clear the ID
    analysis.name = String.format(Constants.Templates.ENTITY_COPY_PREFIX, analysis.name);

    IRAnalysisDTO copyStudy = createAnalysis(analysis);
    return copyStudy;
  }


  @Override
  public Response export(final int id) {

    Response response = null;
    HashMap<String, String> fileList = new HashMap<>();
    HashMap<Integer, String> distTypeLookup = new HashMap<>();

    distTypeLookup.put(1, "TAR");
    distTypeLookup.put(2, "TTO");

    try {
      IncidenceRateAnalysis analysis = this.irAnalysisRepository.findOne(id);
      Set<ExecutionInfo> executions = analysis.getExecutionInfoList();

      fileList.put("analysisDefinition.json", analysis.getDetails().getExpression());

      // squentially return reults of IR calculation.  In Spring 1.4.2, we can utlilize @Async operations to do this in parallel.
      // store results in single CSV file
      ArrayList<String[]> summaryLines = new ArrayList<>();
      ArrayList<String[]> strataLines = new ArrayList<>();
      ArrayList<String[]> distLines = new ArrayList<>();

      for (ExecutionInfo execution : executions)
      {
        Source source = execution.getSource();
        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        // perform this query to CDM in an isolated transaction to avoid expensive JDBC transaction synchronization
        DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
        requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);        
        TransactionStatus initStatus = this.getTransactionTemplateRequiresNew().getTransactionManager().getTransaction(requresNewTx);


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

        SqlRowSet rs = this.getSourceJdbcTemplate(source).queryForRowSet(translatedSql);

        this.getTransactionTemplateRequiresNew().getTransactionManager().commit(initStatus);

        if (distLines.isEmpty())
        {
          distLines.add(rs.getMetaData().getColumnNames());
        }
        while (rs.next())
        {
          ArrayList<String> columns = new ArrayList<>();
          for(int i = 1; i <= rs.getMetaData().getColumnNames().length; i++)
          {
            switch (rs.getMetaData().getColumnName(i)) {
              case "dist_type": 
                columns.add(distTypeLookup.get(rs.getInt(i)));
                break;
              default:
                columns.add(rs.getString(i));
                break;
            }
           }
          distLines.add(columns.toArray(new String[0]));
        }
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

      response = Response
        .ok(baos)
        .type(MediaType.APPLICATION_OCTET_STREAM)
        .header("Content-Disposition", String.format("attachment; filename=\"%s\"", "ir_analysis_" + id + ".zip"))
        .build();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return response;    
  }

  @Override
  public void delete(final int id) {
    irAnalysisRepository.delete(id);
  }

  @Override   
  public void deleteInfo(final int id, final String sourceKey) {
    IncidenceRateAnalysis analysis = irAnalysisRepository.findOne(id);
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
      irExecutionInfoRepository.save(executions);
      return null;
    });
  }
  
}
