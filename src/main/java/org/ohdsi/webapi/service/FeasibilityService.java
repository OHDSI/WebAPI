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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Hibernate;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.feasibility.InclusionRule;
import org.ohdsi.webapi.feasibility.FeasibilityStudy;
import org.ohdsi.webapi.feasibility.FeasibilityStudyQueryBuilder;
import org.ohdsi.webapi.feasibility.PerformFeasibilityTask;
import org.ohdsi.webapi.feasibility.PerformFeasibilityTasklet;
import org.ohdsi.webapi.feasibility.StudyInfo;
import org.ohdsi.webapi.feasibility.FeasibilityReport;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.webapi.TerminateJobStepExceptionHandler;
import org.ohdsi.webapi.feasibility.FeasibilityStudyRepository;
import org.ohdsi.webapi.cohortdefinition.GenerateCohortTask;
import org.ohdsi.webapi.cohortdefinition.GenerateCohortTasklet;
import org.ohdsi.webapi.cohortdefinition.GenerationStatus;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Path("/feasibility/")
@Component
public class FeasibilityService extends AbstractDaoService {
  @Autowired
  private CohortDefinitionRepository cohortDefinitionRepository;  
  
  @Autowired
  private FeasibilityStudyRepository feasibilityStudyRepository;      
  
  @Autowired
  private CohortDefinitionService definitionService;
  
  @Autowired
  private JobBuilderFactory jobBuilders;

  @Autowired
  private StepBuilderFactory stepBuilders;
    
  @Autowired
  private JobTemplate jobTemplate;
  
  @Value("${cohort.targetTable}")
  private String cohortTable;  
  
  @Context
  ServletContext context;
  
  public static class FeasibilityStudyListItem {
    public Integer id;
    public String name;
    public String description;
    public Integer phaseId;
    public Integer sampleSize;
    public String documentUrl;
    public String clinicalTrialsIdentifier;
    public String createdBy;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd, HH:mm")    
    public Date createdDate;
    public String modifiedBy;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd, HH:mm")    
    public Date modifiedDate;
  }  
  
  public static class FeasibilityStudyDTO extends FeasibilityStudyListItem {
    public CohortDefinitionService.CohortDefinitionDTO indexRule;
    public List<InclusionRule> inclusionRules;
  }
  
  private final RowMapper<FeasibilityReport.Summary> summaryMapper = new RowMapper<FeasibilityReport.Summary>() {
    @Override
    public FeasibilityReport.Summary mapRow(ResultSet rs, int rowNum) throws SQLException {
       FeasibilityReport.Summary summary = new FeasibilityReport.Summary();
       summary.totalPersons = rs.getLong("person_count");
       summary.matchingPersons = rs.getLong("match_count");

       double matchRatio = (summary.totalPersons > 0) ? ((double)summary.matchingPersons / (double)summary.totalPersons) : 0.0;
       summary.percentMatched = new BigDecimal(matchRatio*100.0).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
       return summary;
    }
  };
  
  private FeasibilityReport.Summary getSimulationSummary(int id)
  {
    String summaryQuery = String.format("select person_count, match_count from %s.feas_study_index_stats where study_id = %d",
            this.getOhdsiSchema(), id);
    String translatedSql = SqlTranslate.translateSql(summaryQuery, getSourceDialect(), getDialect(), SessionUtils.sessionId(), this.getOhdsiSchema());
    return this.getJdbcTemplate().queryForObject(translatedSql, summaryMapper);
  }
  
  private final RowMapper<FeasibilityReport.InclusionRuleStatistic> inclusionRuleStatisticMapper = new RowMapper<FeasibilityReport.InclusionRuleStatistic>() {
    
    @Override
    public FeasibilityReport.InclusionRuleStatistic mapRow(ResultSet rs, int rowNum) throws SQLException {
       FeasibilityReport.InclusionRuleStatistic statistic = new FeasibilityReport.InclusionRuleStatistic();
       statistic.id = rs.getInt("rule_sequence");
       statistic.name = rs.getString("name");
       long personTotal = rs.getLong("person_total");

       long gainCount = rs.getLong("gain_count");
       double excludeRatio = personTotal > 0 ? (double)gainCount / (double)personTotal : 0.0;
       String percentExcluded = new BigDecimal(excludeRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString();
       statistic.percentExcluded =  percentExcluded + "%";

       long satisfyCount = rs.getLong("person_count");
       double satisfyRatio = personTotal > 0 ? (double)satisfyCount / (double)personTotal : 0.0;
       String percentSatisfying = new BigDecimal(satisfyRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString();
       statistic.percentSatisfying = percentSatisfying + "%";
       return statistic;
    }   
  };
  
  
  private List<FeasibilityReport.InclusionRuleStatistic> getSimulationInclusionRuleStatistics(int id)
  {
    String statisticsQuery = String.format("select rule_sequence, name, person_count, gain_count, person_total from %s.feas_study_inclusion_stats where study_id = %d",
            this.getOhdsiSchema(), id);
    String translatedSql = SqlTranslate.translateSql(statisticsQuery, getSourceDialect(), getDialect(), SessionUtils.sessionId(), this.getOhdsiSchema());
    return this.getJdbcTemplate().query(translatedSql, inclusionRuleStatisticMapper);
  }
  
  private int countSetBits(long n)
  {
    int count = 0;
    while (n > 0)
    {
      n &= (n-1);
      count++;
    }
    return count;
  }
  
  private String formatBitMask(Long n, int size)
  {
    return StringUtils.reverse(StringUtils.leftPad(Long.toBinaryString(n), size, "0"));
  }
  
  private final RowMapper<Long[]> simulationResultItemMapper = new RowMapper<Long[]>() {

    @Override
    public Long[] mapRow(ResultSet rs, int rowNum) throws SQLException {
       Long[] resultItem = new Long[2];
       resultItem[0] = rs.getLong("inclusion_rule_mask");
       resultItem[1] = rs.getLong("person_count");
       return resultItem;
    }       
  };
  
  private String getInclusionRuleTreemapData(int id, int inclusionRuleCount)
  {
    String smulationResultsQuery = String.format("select inclusion_rule_mask, person_count from %s.feas_study_result where study_id = %d",
            this.getOhdsiSchema(), id);    
    String translatedSql = SqlTranslate.translateSql(smulationResultsQuery, getSourceDialect(), getDialect(), SessionUtils.sessionId(), this.getOhdsiSchema());
    
    // [0] is the inclusion rule bitmask, [1] is the count of the match
    List<Long[]> items = this.getJdbcTemplate().query(translatedSql, simulationResultItemMapper);
    Map<Integer, List<Long[]>> groups = new HashMap<>();
    for (Long[] item : items) {
     int bitsSet = countSetBits(item[0]);
     if (!groups.containsKey(bitsSet)) {
       groups.put(bitsSet, new ArrayList<Long[]>());
     }
     groups.get(bitsSet).add(item);
    }
    
    StringBuilder treemapData = new StringBuilder("{\"name\" : \"Everyone\", \"children\" : [");
    
    List<Integer> groupKeys = new ArrayList<Integer>(groups.keySet());
    Collections.sort(groupKeys);
    Collections.reverse(groupKeys);
    
    int groupCount = 0;
    // create a nested treemap data where more matches (more bits set in string) appear higher in the hierarchy)
    for(Integer groupKey : groupKeys) {
      if (groupCount > 0)
        treemapData.append(",");
      
      treemapData.append(String.format("{\"name\" : \"Group %d\", \"children\" : [",groupKey));
      
      int groupItemCount = 0;
      for (Long[] groupItem : groups.get(groupKey) ) {
        if (groupItemCount > 0)
          treemapData.append(",");
        
        //sb_treemap.Append("{\"name\": \"" + cohort_identifer + "\", \"size\": " + cohorts[cohort_identifer].ToString() + "}");
        treemapData.append(String.format("{\"name\": \"%s\", \"size\": %d}", formatBitMask(groupItem[0], inclusionRuleCount), groupItem[1]));
        groupItemCount++;
      }
      groupCount++;
    }
    
    treemapData.append(StringUtils.repeat("]}", groupCount+1));
    
    return treemapData.toString(); 
  }
  
  
  public FeasibilityStudyDTO feasibilityStudyToDTO(FeasibilityStudy study)
  {
    FeasibilityStudyDTO pDTO = new FeasibilityStudyDTO();
    pDTO.id = study.getId();
    pDTO.id = study.getId();
    pDTO.name = study.getName();
    pDTO.description = study.getDescription();
    pDTO.createdBy = study.getCreatedBy();
    pDTO.createdDate = study.getCreatedDate();
    pDTO.modifiedBy = study.getModifiedBy();
    pDTO.modifiedDate = study.getModifiedDate();
    pDTO.indexRule = this.definitionService.cohortDefinitionToDTO(study.getIndexRule());
    pDTO.inclusionRules = study.getInclusionRules();
    
    return pDTO;
  }
  
  /**
   * Returns all cohort definitions in the cohort schema
   * 
   * @return List of cohort_definition
   */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<FeasibilityService.FeasibilityStudyListItem> getFeasibilityStudyList() {
    ArrayList<FeasibilityService.FeasibilityStudyListItem> result = new ArrayList<>();
    Iterable<FeasibilityStudy> studies = this.feasibilityStudyRepository.findAll();
    for (FeasibilityStudy p : studies)
    {
      FeasibilityService.FeasibilityStudyListItem item = new FeasibilityService.FeasibilityStudyListItem();
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
   * Creates the feasibility study
   * 
   * @param study The study to create.
   * @return The new FeasibilityStudy
   */
  @PUT
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public FeasibilityService.FeasibilityStudyDTO createStudy(FeasibilityService.FeasibilityStudyDTO study) {
    Date currentTime = Calendar.getInstance().getTime();

    //create definition in 2 saves, first to get the generated ID for the new cohort definition (the index rule)
    // then to associate the new definition with the index rule of the study
    FeasibilityStudy newStudy = new FeasibilityStudy();
    newStudy.setName(study.name)
      .setDescription(study.description)
      .setCreatedBy("system")
      .setCreatedDate(currentTime)
      .setInclusionRules(study.inclusionRules);
    
    study.indexRule = this.definitionService.createCohortDefinition(study.indexRule);
    CohortDefinition indexRule = this.cohortDefinitionRepository.findOne(study.indexRule.id);
    newStudy.setIndexRule(indexRule);
    FeasibilityStudy createdStudy = this.feasibilityStudyRepository.save(newStudy);
    return feasibilityStudyToDTO(createdStudy);    
  }
  
  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional
  public FeasibilityService.FeasibilityStudyDTO getStudy(@PathParam("id") final int id) {
    FeasibilityStudy s = this.feasibilityStudyRepository.findOneWithDetail(id);
    return feasibilityStudyToDTO(s);
  }

  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public FeasibilityService.FeasibilityStudyDTO saveStudy(@PathParam("id") final int id, FeasibilityStudyDTO study) {
    Date currentTime = Calendar.getInstance().getTime();
    
    FeasibilityStudy updatedStudy = this.feasibilityStudyRepository.findOne(id);
    updatedStudy.setName(study.name)
      .setDescription(study.description)
      .setModifiedBy("system")
      .setModifiedDate(currentTime)
      .setInclusionRules(study.inclusionRules);
    
    updatedStudy.getIndexRule()
      .setModifiedBy("system")
      .setModifiedDate(currentTime)
      .setName(study.indexRule.name)
      .setDescription(study.indexRule.description)
      .setExpressionType(study.indexRule.expressionType)
      .getDetails().setExpression(study.indexRule.expression);

    this.feasibilityStudyRepository.save(updatedStudy);
    
    return getStudy(id);
  }
  
  @GET
  @Path("/{id}/generate")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)  
  public JobExecutionResource performStudy(@PathParam("id") final int id) {
    
    DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
    requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    
    TransactionStatus initStatus = this.getTransactionTemplate().getTransactionManager().getTransaction(requresNewTx);
    
    FeasibilityStudy study = this.feasibilityStudyRepository.findOne(id);
    CohortDefinition indexRule = study.getIndexRule();
    Hibernate.initialize(study.getIndexRule());
    Hibernate.initialize(indexRule.getDetails());
    Hibernate.initialize(study.getInclusionRules());
    Hibernate.initialize(study.getInfo());
    
    StudyInfo info = study.getInfo();
    if (info == null)
    {
      info = new StudyInfo(study);
      study.setInfo(info);
    }    
    info.setStatus(GenerationStatus.PENDING)
      .setStartTime(null)
      .setExecutionDuration(null);
    this.feasibilityStudyRepository.save(study);
    int indexRuleId = study.getIndexRule().getId();
    this.getTransactionTemplate().getTransactionManager().commit(initStatus);    

    CohortExpressionQueryBuilder.BuildExpressionQueryOptions indexRuleOptions = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
    indexRuleOptions.cohortId = indexRuleId;
    indexRuleOptions.cdmSchema = this.getCdmSchema();
    indexRuleOptions.targetTable = this.cohortTable;
      
    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString("study_id", ("" + id));
    final JobParameters jobParameters = builder.toJobParameters();

    GenerateCohortTask generateTask = new GenerateCohortTask()
            .setCohortDefinition(indexRule)
            .setOptions(indexRuleOptions)
            .setSourceDialect(this.getSourceDialect())
            .setTargetDialect(this.getDialect());
      
    GenerateCohortTasklet indexRuleTasklet = new GenerateCohortTasklet(generateTask, getJdbcTemplate(), getTransactionTemplate(), cohortDefinitionRepository);
    
    Step generateCohortStep = stepBuilders.get("performStudy.generateIndexCohort")
      .tasklet(indexRuleTasklet)
      .exceptionHandler(new TerminateJobStepExceptionHandler())
      .build();
    
    FeasibilityStudyQueryBuilder.BuildExpressionQueryOptions simulationQueryOptions = new FeasibilityStudyQueryBuilder.BuildExpressionQueryOptions();
    simulationQueryOptions.cdmSchema = this.getCdmSchema();
    simulationQueryOptions.cohortTable = this.cohortTable;
    
    PerformFeasibilityTask simulateTask = new PerformFeasibilityTask()
            .setOptions(simulationQueryOptions) // use same options as index rule to map to the correct CDM and Cohort table
            .setSourceDialect(this.getSourceDialect())
            .setTargetDialect(this.getDialect());
            
    PerformFeasibilityTasklet simulateTasket = new PerformFeasibilityTasklet(simulateTask, getJdbcTemplate(), getTransactionTemplate(), feasibilityStudyRepository, cohortDefinitionRepository);
            
    Step generateCohortStep2 = stepBuilders.get("performStudy.performStudy")
      .tasklet(simulateTasket)
      .build();    
    
    Job performStudyJob = jobBuilders.get("performStudy")
      .start(generateCohortStep)
      .next(generateCohortStep2)
      .build();
    
    JobExecutionResource jobExec = this.jobTemplate.launch(performStudyJob, jobParameters);
    return jobExec;
  }
  
  @GET
  @Path("/{id}/info")
  @Produces(MediaType.APPLICATION_JSON)
  public StudyInfo getSimulationInfo(@PathParam("id") final int id) {
    FeasibilityStudy p = this.feasibilityStudyRepository.findOneWithInfo(id);
    return p.getInfo();
  }
  
  @GET
  @Path("/{id}/report")
  @Produces(MediaType.APPLICATION_JSON)
  public FeasibilityReport getSimulationReport(@PathParam("id") final int id) {
    FeasibilityStudy p = this.feasibilityStudyRepository.findOne(id);
    
    FeasibilityReport.Summary summary = getSimulationSummary(id);
    List<FeasibilityReport.InclusionRuleStatistic> inclusionRuleStats = getSimulationInclusionRuleStatistics(id);
    String treemapData = getInclusionRuleTreemapData(id, inclusionRuleStats.size());

    FeasibilityReport report = new FeasibilityReport();
    report.summary = summary;
    report.inclusionRuleStats = inclusionRuleStats;
    report.treemapData = treemapData;
    
    return report;
  }  
}
