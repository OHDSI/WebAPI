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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.exampleapplication.model.Widget;
import org.ohdsi.webapi.ircalc.AnalysisReport;
import org.ohdsi.webapi.ircalc.ExecutionInfo;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisDetails;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExpression;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisRepository;
import org.ohdsi.webapi.ircalc.PerformAnalysisTasklet;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Path("/ir/")
@Component
public class IRAnalysisService extends AbstractDaoService {

  private static final Log log = LogFactory.getLog(IRAnalysisService.class);
  private final static String STRATA_STATS_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/incidencerate/sql/strata_stats.sql"); 
  

  @Autowired
  private IncidenceRateAnalysisRepository irAnalysisRepository;

  @Autowired
  private JobBuilderFactory jobBuilders;

  @Autowired
  private StepBuilderFactory stepBuilders;

  @Autowired
  private JobTemplate jobTemplate;

  @Autowired
  private Security security;

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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm")
    public Date createdDate;
    public String modifiedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm")
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

    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

    String summaryQuery = String.format("select target_id, outcome_id, sum(person_count) as person_count, sum(time_at_risk) as time_at_risk, sum(cases) as cases from %s.ir_analysis_result where analysis_id = %d GROUP BY target_id, outcome_id", resultsTableQualifier, id);
    String translatedSql = SqlTranslate.translateSql(summaryQuery, "sql server", source.getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);
    List<AnalysisReport.Summary> summaryList = this.getSourceJdbcTemplate(source).query(translatedSql, summaryMapper);
    return summaryList;
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
    String statisticsQuery = STRATA_STATS_QUERY_TEMPLATE;
    statisticsQuery = statisticsQuery.replace("@results_database_schema", resultsTableQualifier);
    statisticsQuery = statisticsQuery.replace("@analysis_id", String.valueOf(id));
    String translatedSql = SqlTranslate.translateSql(statisticsQuery, "sql server", source.getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);
    return this.getSourceJdbcTemplate(source).query(translatedSql, strataRuleStatisticMapper);
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
    String analysisResultsQuery = String.format("select strata_mask, person_count, time_at_risk, cases from %s.ir_analysis_result where analysis_id = %d and target_id = %d and outcome_id = %d",
            resultsTableQualifier, analysisId, targetId, outcomeId);
    String translatedSql = SqlTranslate.translateSql(analysisResultsQuery, "sql server", source.getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);

    // [0] is the inclusion rule bitmask, [1] is the count of the match
    List<StratifyReportItem> items = this.getSourceJdbcTemplate(source).query(translatedSql, stratifyResultsMapper);
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
    aDTO.createdBy = analysis.getCreatedBy();
    aDTO.createdDate = analysis.getCreatedDate();
    aDTO.modifiedBy = analysis.getModifiedBy();
    aDTO.modifiedDate = analysis.getModifiedDate();
    aDTO.expression = analysis.getDetails() != null ? analysis.getDetails().getExpression() : null;

    return aDTO;
  }

  /**
   * Returns all IR Analysis in a list.
   *
   * @return List of IncidenceRateAnalysis
   */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<IRAnalysisService.IRAnalysisListItem> getIRAnalysisList() {
    ArrayList<IRAnalysisService.IRAnalysisListItem> result = new ArrayList<>();
    Iterable<IncidenceRateAnalysis> analysisList = this.irAnalysisRepository.findAll();
    for (IncidenceRateAnalysis p : analysisList) {
      IRAnalysisService.IRAnalysisListItem item = new IRAnalysisService.IRAnalysisListItem();
      item.id = p.getId();
      item.name = p.getName();
      item.description = p.getDescription();
      item.createdBy = p.getCreatedBy();
      item.createdDate = p.getCreatedDate();
      item.modifiedBy = p.getModifiedBy();
      item.modifiedDate = p.getModifiedDate();
      result.add(item);
    }
    return result;
  }

  /**
   * Creates the incidence rate analysis
   *
   * @param analysis The analysis to create.
   * @return The new FeasibilityStudy
   */
  @POST
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public IRAnalysisDTO createAnalysis(IRAnalysisDTO analysis) {
    Date currentTime = Calendar.getInstance().getTime();

    // it might be possible to leverage saveAnalysis() but not sure how to pull the auto ID from
    // the DB to pass it into saveAnalysis (since saveAnalysis does a findOne() at the start).
    // If there's a way to get the Entity into the persistence manager so findOne() returns this newly created entity
    // then we could create the entity here (wihtout persist) and then call saveAnalysis within the sasme Tx.
    IncidenceRateAnalysis newAnalysis = new IncidenceRateAnalysis();
    newAnalysis.setName(analysis.name)
            .setDescription(analysis.description)
            .setCreatedBy(security.getSubject())
            .setCreatedDate(currentTime);
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

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public IRAnalysisDTO getAnalysis(@PathParam("id") final int id) {
    IncidenceRateAnalysis a = this.irAnalysisRepository.findOne(id);
    return analysisToDTO(a);
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public IRAnalysisDTO saveAnalysis(@PathParam("id") final int id, IRAnalysisDTO analysis) {
    Date currentTime = Calendar.getInstance().getTime();

    IncidenceRateAnalysis updatedAnalysis = this.irAnalysisRepository.findOne(id);
    updatedAnalysis.setName(analysis.name)
            .setDescription(analysis.description)
            .setModifiedBy(security.getSubject())
            .setModifiedDate(currentTime);
    
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

  @GET
  @Path("/{analysis_id}/execute/{sourceKey}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public JobExecutionResource performAnalysis(@PathParam("analysis_id") final int analysisId, @PathParam("sourceKey") final String sourceKey) {
    Date startTime = Calendar.getInstance().getTime();

    Source source = this.getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);

    DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
    requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    TransactionStatus initStatus = this.getTransactionTemplate().getTransactionManager().getTransaction(requresNewTx);

    IncidenceRateAnalysis analysis = this.irAnalysisRepository.findOne(analysisId);

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
    builder.addString("jobName", "IR Analysis: " + analysis.getId() + " : " + source.getSourceName() + " (" + source.getSourceKey() + ")");
    builder.addString("cdm_database_schema", cdmTableQualifier);
    builder.addString("results_database_schema", resultsTableQualifier);
    builder.addString("target_dialect", source.getSourceDialect());
    builder.addString("analysis_id", ("" + analysisId));
    builder.addString("source_id", ("" + source.getSourceId()));

    final JobParameters jobParameters = builder.toJobParameters();

    PerformAnalysisTasklet analysisTasklet = new PerformAnalysisTasklet(getSourceJdbcTemplate(source), getTransactionTemplate(), irAnalysisRepository);

    Step irAnalysisStep = stepBuilders.get("irAnalysis.execute")
      .tasklet(analysisTasklet)
    .build();

    Job executeAnalysis = jobBuilders.get("irAnalysis")
      .start(irAnalysisStep)
      .build();

    JobExecutionResource jobExec = this.jobTemplate.launch(executeAnalysis, jobParameters);
    return jobExec;  }

  @GET
  @Path("/{id}/info")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public List<AnalysisInfoDTO> getAnalysisInfo(@PathParam("id") final int id) {
    IncidenceRateAnalysis analysis = this.irAnalysisRepository.findOne(id);

    List<AnalysisInfoDTO> result = new ArrayList<>();
    for (ExecutionInfo executionInfo : analysis.getExecutionInfoList()) {
      AnalysisInfoDTO info = new AnalysisInfoDTO();
      info.executionInfo = executionInfo;
      try {
        if (executionInfo.getStatus() == GenerationStatus.COMPLETE && executionInfo.getIsValid())
          info.summaryList = getAnalysisSummaryList(id, executionInfo.getSource());
      }
      catch (Exception e)
      {
        log.error("Error getting IR Analysis summary list.", e);
      }
      result.add(info);
    }
    return result;
  }

  @GET
  @Path("/{id}/report/{sourceKey}")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public AnalysisReport getAnalysisReport(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, @QueryParam("targetId") final int targetId, @QueryParam("outcomeId") final int outcomeId ) {

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

  /**
   * Copies the specified cohort definition
   *
   * @param id - the Cohort Definition ID to copy
   * @return the copied cohort definition as a CohortDefinitionDTO
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}/copy")
  @javax.transaction.Transactional
  public IRAnalysisDTO copy(@PathParam("id") final int id) {
    IRAnalysisDTO analysis = getAnalysis(id);
    analysis.id = null; // clear the ID
    analysis.name = "COPY OF: " + analysis.name;

    IRAnalysisDTO copyStudy = createAnalysis(analysis);
    return copyStudy;
  }

  
  /**
   * Exports the analysis definition and results
   *
   * @param id - the IR Analysis ID to export
   * @return Response containing binary stream of zipped data
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}/export")
  @Transactional
  public Response export(@PathParam("id") final int id) {

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
        String translatedSql = SqlTranslate.translateSql(distQuery, "sql server", source.getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);
        
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
  
  /**
   * Deletes the specified cohort definition
   *
   * @param id - the Cohort Definition ID to copy
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}")
  public void delete(@PathParam("id") final int id) {
    irAnalysisRepository.delete(id);
  }
  
  /**
   * Deletes the specified cohort definition
   *
   * @param id - the Cohort Definition ID to copy
   */
  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}/info/{sourceKey}")
  @Transactional    
  public void deleteInfo(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey) {
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
  
}
