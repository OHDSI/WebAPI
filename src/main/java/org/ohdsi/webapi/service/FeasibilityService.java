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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.feasibility.InclusionRule;
import org.ohdsi.webapi.feasibility.FeasibilityStudy;
import org.ohdsi.webapi.feasibility.PerformFeasibilityTasklet;
import org.ohdsi.webapi.feasibility.StudyGenerationInfo;
import org.ohdsi.webapi.feasibility.FeasibilityReport;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.TerminateJobStepExceptionHandler;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;
import org.ohdsi.webapi.feasibility.FeasibilityStudyRepository;
import org.ohdsi.webapi.cohortdefinition.GenerateCohortTasklet;
import org.ohdsi.webapi.GenerationStatus;
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
import org.springframework.jdbc.core.JdbcTemplate;
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

  @Autowired
  private Security security;

  @Context
  ServletContext context;

  private StudyGenerationInfo findStudyGenerationInfoBySourceId(Collection<StudyGenerationInfo> infoList, Integer sourceId) {
    for (StudyGenerationInfo info : infoList) {
      if (info.getId().getSourceId() == sourceId) {
        return info;
      }
    }
    return null;
  }

  private CohortGenerationInfo findCohortGenerationInfoBySourceId(Collection<CohortGenerationInfo> infoList, Integer sourceId) {
    for (CohortGenerationInfo info : infoList) {
      if (info.getId().getSourceId() == sourceId) {
        return info;
      }
    }
    return null;
  }

  public static class FeasibilityStudyListItem {

    public Integer id;
    public String name;
    public String description;
    public String createdBy;
    public Integer indexCohortId;
    public Integer matchingCohortId;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm")
    public Date createdDate;
    public String modifiedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm")
    public Date modifiedDate;
  }

  public static class FeasibilityStudyDTO extends FeasibilityStudyListItem {

    public String indexRule;
    public String indexDescription;
    public List<InclusionRule> inclusionRules;
  }

  public static class StudyInfoDTO {
    public StudyGenerationInfo generationInfo;
    public FeasibilityReport.Summary summary;
  }
  
  private final RowMapper<FeasibilityReport.Summary> summaryMapper = new RowMapper<FeasibilityReport.Summary>() {
    @Override
    public FeasibilityReport.Summary mapRow(ResultSet rs, int rowNum) throws SQLException {
      FeasibilityReport.Summary summary = new FeasibilityReport.Summary();
      summary.totalPersons = rs.getLong("person_count");
      summary.matchingPersons = rs.getLong("match_count");

      double matchRatio = (summary.totalPersons > 0) ? ((double) summary.matchingPersons / (double) summary.totalPersons) : 0.0;
      summary.percentMatched = new BigDecimal(matchRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
      return summary;
    }
  };

  private FeasibilityReport.Summary getSimulationSummary(int id, Source source) {

    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

    String summaryQuery = String.format("select person_count, match_count from %s.feas_study_index_stats where study_id = %d", resultsTableQualifier, id);
    String translatedSql = SqlTranslate.translateSql(summaryQuery, "sql server", source.getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);
    List<FeasibilityReport.Summary> summaryList = this.getSourceJdbcTemplate(source).query(translatedSql, summaryMapper);
    if (summaryList.size() > 0)
      return summaryList.get(0);
    
    return null;
  }

  private final RowMapper<FeasibilityReport.InclusionRuleStatistic> inclusionRuleStatisticMapper = new RowMapper<FeasibilityReport.InclusionRuleStatistic>() {

    @Override
    public FeasibilityReport.InclusionRuleStatistic mapRow(ResultSet rs, int rowNum) throws SQLException {
      FeasibilityReport.InclusionRuleStatistic statistic = new FeasibilityReport.InclusionRuleStatistic();
      statistic.id = rs.getInt("rule_sequence");
      statistic.name = rs.getString("name");
      statistic.countSatisfying = rs.getLong("person_count");
      long personTotal = rs.getLong("person_total");

      long gainCount = rs.getLong("gain_count");
      double excludeRatio = personTotal > 0 ? (double) gainCount / (double) personTotal : 0.0;
      String percentExcluded = new BigDecimal(excludeRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString();
      statistic.percentExcluded = percentExcluded + "%";

      long satisfyCount = rs.getLong("person_count");
      double satisfyRatio = personTotal > 0 ? (double) satisfyCount / (double) personTotal : 0.0;
      String percentSatisfying = new BigDecimal(satisfyRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString();
      statistic.percentSatisfying = percentSatisfying + "%";
      return statistic;
    }
  };

  private String getMatchingCriteriaExpression(FeasibilityStudy p) {

    if (p.getInclusionRules().size() == 0) {
      throw new RuntimeException("Study must have at least 1 inclusion rule");
    }

    try {
      // all resultRule repository objects are initalized; create 'all criteria' cohort definition from index rule + inclusion rules
      ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
      CohortExpression indexRuleExpression = mapper.readValue(p.getIndexRule().getDetails().getExpression(), CohortExpression.class);

      if (indexRuleExpression.additionalCriteria == null) {
        CriteriaGroup additionalCriteria = new CriteriaGroup();
        additionalCriteria.type = "ALL";
        indexRuleExpression.additionalCriteria = additionalCriteria;
      } else {
        if (!"ALL".equalsIgnoreCase(indexRuleExpression.additionalCriteria.type)) {
          // move this CriteriaGroup inside a new parent CriteriaGroup where the parent CriteriaGroup.type == "ALL"
          CriteriaGroup parentGroup = new CriteriaGroup();
          parentGroup.type = "ALL";
          parentGroup.groups = new CriteriaGroup[1];
          parentGroup.groups[0] = indexRuleExpression.additionalCriteria;
          indexRuleExpression.additionalCriteria = parentGroup;
        }
      }
      // place each inclusion rule (which is a CriteriaGroup) in the indexRuleExpression.additionalCriteria.group array to create the 'allCriteriaExpression'
      ArrayList<CriteriaGroup> additionalCriteriaGroups = new ArrayList<>();
      if (indexRuleExpression.additionalCriteria.groups != null) {
        additionalCriteriaGroups.addAll(Arrays.asList(indexRuleExpression.additionalCriteria.groups));
      }

      for (InclusionRule inclusionRule : p.getInclusionRules()) {
        String inclusionRuleJSON = inclusionRule.getExpression();
        CriteriaGroup inclusionRuleGroup = mapper.readValue(inclusionRuleJSON, CriteriaGroup.class);
        additionalCriteriaGroups.add(inclusionRuleGroup);
      }
      // overwrite indexRule additional criteria groups with the new list of groups with inclusion rules
      indexRuleExpression.additionalCriteria.groups = additionalCriteriaGroups.toArray(new CriteriaGroup[0]);

      String allCriteriaExpression = mapper.writeValueAsString(indexRuleExpression); // index rule expression now contains all inclusion criteria as additional criteria
      return allCriteriaExpression;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

  }

  private List<FeasibilityReport.InclusionRuleStatistic> getSimulationInclusionRuleStatistics(int id, Source source) {
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String statisticsQuery = String.format("select rule_sequence, name, person_count, gain_count, person_total from %s.feas_study_inclusion_stats where study_id = %d ORDER BY rule_sequence", resultsTableQualifier, id);
    String translatedSql = SqlTranslate.translateSql(statisticsQuery, "sql server", source.getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);
    return this.getSourceJdbcTemplate(source).query(translatedSql, inclusionRuleStatisticMapper);
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

  private final RowMapper<Long[]> simulationResultItemMapper = new RowMapper<Long[]>() {

    @Override
    public Long[] mapRow(ResultSet rs, int rowNum) throws SQLException {
      Long[] resultItem = new Long[2];
      resultItem[0] = rs.getLong("inclusion_rule_mask");
      resultItem[1] = rs.getLong("person_count");
      return resultItem;
    }
  };

  private String getInclusionRuleTreemapData(int id, int inclusionRuleCount, Source source) {
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String smulationResultsQuery = String.format("select inclusion_rule_mask, person_count from %s.feas_study_result where study_id = %d",
            resultsTableQualifier, id);
    String translatedSql = SqlTranslate.translateSql(smulationResultsQuery, "sql server", source.getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);

    // [0] is the inclusion rule bitmask, [1] is the count of the match
    List<Long[]> items = this.getSourceJdbcTemplate(source).query(translatedSql, simulationResultItemMapper);
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
    for (Integer groupKey : groupKeys) {
      if (groupCount > 0) {
        treemapData.append(",");
      }

      treemapData.append(String.format("{\"name\" : \"Group %d\", \"children\" : [", groupKey));

      int groupItemCount = 0;
      for (Long[] groupItem : groups.get(groupKey)) {
        if (groupItemCount > 0) {
          treemapData.append(",");
        }

        //sb_treemap.Append("{\"name\": \"" + cohort_identifer + "\", \"size\": " + cohorts[cohort_identifer].ToString() + "}");
        treemapData.append(String.format("{\"name\": \"%s\", \"size\": %d}", formatBitMask(groupItem[0], inclusionRuleCount), groupItem[1]));
        groupItemCount++;
      }
      groupCount++;
    }

    treemapData.append(StringUtils.repeat("]}", groupCount + 1));

    return treemapData.toString();
  }

  public FeasibilityStudyDTO feasibilityStudyToDTO(FeasibilityStudy study) {
    FeasibilityStudyDTO pDTO = new FeasibilityStudyDTO();
    pDTO.id = study.getId();
    pDTO.name = study.getName();
    pDTO.description = study.getDescription();
    pDTO.indexCohortId = study.getIndexRule().getId();
    pDTO.matchingCohortId = study.getResultRule() != null ? study.getResultRule().getId() : null;
    pDTO.createdBy = study.getCreatedBy();
    pDTO.createdDate = study.getCreatedDate();
    pDTO.modifiedBy = study.getModifiedBy();
    pDTO.modifiedDate = study.getModifiedDate();
    pDTO.indexRule = study.getIndexRule().getDetails().getExpression();
    pDTO.indexDescription = study.getIndexRule().getDescription();
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
    for (FeasibilityStudy p : studies) {
      FeasibilityService.FeasibilityStudyListItem item = new FeasibilityService.FeasibilityStudyListItem();
      item.id = p.getId();
      item.name = p.getName();
      item.description = p.getDescription();
      item.indexCohortId = p.getIndexRule().getId();
      item.matchingCohortId = p.getResultRule() != null ? p.getResultRule().getId() : null;
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
            .setCreatedBy(security.getSubject())
            .setCreatedDate(currentTime)
            .setInclusionRules(new ArrayList<InclusionRule>(study.inclusionRules));

    // create index cohort
    CohortDefinition indexRule = new CohortDefinition()
            .setName("Index Population for Study: " + newStudy.getName())
            .setDescription(study.indexDescription)
            .setCreatedBy(security.getSubject())
            .setCreatedDate(currentTime)
            .setExpressionType(ExpressionType.SIMPLE_EXPRESSION);

    CohortDefinitionDetails indexDetails = new CohortDefinitionDetails();
    indexDetails.setCohortDefinition(indexRule)
            .setExpression(study.indexRule);
    indexRule.setDetails(indexDetails);
    newStudy.setIndexRule(indexRule);

    // build matching cohort from inclusion rules if inclusion rules exist
    if (newStudy.getInclusionRules().size() > 0) {
      CohortDefinition resultDef = new CohortDefinition()
              .setName("Matching Population for Study: " + newStudy.getName())
              .setDescription(newStudy.getDescription())
              .setCreatedBy(security.getSubject())
              .setCreatedDate(currentTime)
              .setExpressionType(ExpressionType.SIMPLE_EXPRESSION);

      CohortDefinitionDetails resultDetails = new CohortDefinitionDetails();
      resultDetails.setCohortDefinition(resultDef)
              .setExpression(getMatchingCriteriaExpression(newStudy));
      resultDef.setDetails(resultDetails);
      newStudy.setResultRule(resultDef);
    }

    FeasibilityStudy createdStudy = this.feasibilityStudyRepository.save(newStudy);

    return feasibilityStudyToDTO(createdStudy);
  }

  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
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
            .setModifiedBy(security.getSubject())
            .setModifiedDate(currentTime)
            .setInclusionRules(study.inclusionRules);

    updatedStudy.getIndexRule()
            .setModifiedBy(security.getSubject())
            .setModifiedDate(currentTime)
            .setName("Index Population for Study: " + updatedStudy.getName())
            .setDescription(study.indexDescription)
            .getDetails().setExpression(study.indexRule);

    CohortDefinition resultRule = updatedStudy.getResultRule();
    if (updatedStudy.getInclusionRules().size() > 0) {
      if (resultRule == null) {
        resultRule = new CohortDefinition();
        resultRule.setName("Matching Population for Study: " + updatedStudy.getName())
                .setCreatedBy(security.getSubject())
                .setCreatedDate(currentTime)
                .setExpressionType(ExpressionType.SIMPLE_EXPRESSION);

        CohortDefinitionDetails resultDetails = new CohortDefinitionDetails();
        resultDetails.setCohortDefinition(resultRule);
        resultRule.setDetails(resultDetails);
        updatedStudy.setResultRule(resultRule);
      }

      resultRule.setModifiedBy(security.getSubject())
              .setModifiedDate(currentTime)
              .setName("Matching Population for Study: " + updatedStudy.getName())
              .setDescription(updatedStudy.getDescription())
              .setModifiedBy(security.getSubject())
              .setModifiedDate(currentTime)
              .getDetails().setExpression(getMatchingCriteriaExpression(updatedStudy));
    } else {
      updatedStudy.setResultRule(null);
      if (resultRule != null) {
        cohortDefinitionRepository.delete(resultRule);
      }
    }
    this.feasibilityStudyRepository.save(updatedStudy);

    return getStudy(id);
  }

  @GET
  @Path("/{study_id}/generate/{sourceKey}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public JobExecutionResource performStudy(@PathParam("study_id") final int study_id, @PathParam("sourceKey") final String sourceKey) {
    Date startTime = Calendar.getInstance().getTime();

    Source source = this.getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);

    DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
    requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);

    TransactionStatus initStatus = this.getTransactionTemplate().getTransactionManager().getTransaction(requresNewTx);

    FeasibilityStudy study = this.feasibilityStudyRepository.findOne(study_id);

    CohortDefinition indexRule = this.cohortDefinitionRepository.findOne(study.getIndexRule().getId());
    CohortGenerationInfo indexInfo = findCohortGenerationInfoBySourceId(indexRule.getGenerationInfoList(), source.getSourceId());
    if (indexInfo == null) {
      indexInfo = new CohortGenerationInfo(indexRule, source.getSourceId());
      indexRule.getGenerationInfoList().add(indexInfo);
    }
    indexInfo.setStatus(GenerationStatus.PENDING)
            .setStartTime(startTime)
            .setExecutionDuration(null);
    this.cohortDefinitionRepository.save(indexRule);

    if (study.getResultRule() != null)
    {
      CohortDefinition resultRule = this.cohortDefinitionRepository.findOne(study.getResultRule().getId());
      CohortGenerationInfo resultInfo = findCohortGenerationInfoBySourceId(resultRule.getGenerationInfoList(), source.getSourceId());
      if (resultInfo == null) {
        resultInfo = new CohortGenerationInfo(resultRule, source.getSourceId());
        resultRule.getGenerationInfoList().add(resultInfo);
      }
      resultInfo.setStatus(GenerationStatus.PENDING)
              .setStartTime(startTime)
              .setExecutionDuration(null);
      this.cohortDefinitionRepository.save(resultRule);
    }
    
    StudyGenerationInfo studyInfo = findStudyGenerationInfoBySourceId(study.getStudyGenerationInfoList(), source.getSourceId());
    if (studyInfo == null) {
      studyInfo = new StudyGenerationInfo(study, source);
      study.getStudyGenerationInfoList().add(studyInfo);
    }
    studyInfo.setStatus(GenerationStatus.PENDING)
            .setStartTime(startTime)
            .setExecutionDuration(null);

    this.feasibilityStudyRepository.save(study);

    this.getTransactionTemplate().getTransactionManager().commit(initStatus);

    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString("jobName", "performing feasibility study on " + indexRule.getName() + " : " + source.getSourceName() + " (" + source.getSourceKey() + ")");
    builder.addString("cdm_database_schema", cdmTableQualifier);
    builder.addString("results_database_schema", resultsTableQualifier);
    builder.addString("target_database_schema", resultsTableQualifier);
    builder.addString("target_dialect", source.getSourceDialect());
    builder.addString("target_table", "cohort");
    builder.addString("cohort_definition_id", ("" + indexRule.getId()));
    builder.addString("study_id", ("" + study_id));
    builder.addString("source_id", ("" + source.getSourceId()));
    builder.addString("generate_stats", Boolean.TRUE.toString());

    final JobParameters jobParameters = builder.toJobParameters();
    final JdbcTemplate sourceJdbcTemplate = getSourceJdbcTemplate(source);

    GenerateCohortTasklet indexRuleTasklet = new GenerateCohortTasklet(sourceJdbcTemplate, getTransactionTemplate(), cohortDefinitionRepository);

    Step generateCohortStep = stepBuilders.get("performStudy.generateIndexCohort")
            .tasklet(indexRuleTasklet)
            .exceptionHandler(new TerminateJobStepExceptionHandler())
            .build();

    PerformFeasibilityTasklet simulateTasket = new PerformFeasibilityTasklet(sourceJdbcTemplate, getTransactionTemplate(), feasibilityStudyRepository, cohortDefinitionRepository);

    Step performStudyStep = stepBuilders.get("performStudy.performStudy")
            .tasklet(simulateTasket)
            .build();

    Job performStudyJob = jobBuilders.get("performStudy")
            .start(generateCohortStep)
            .next(performStudyStep)
            .build();

    JobExecutionResource jobExec = this.jobTemplate.launch(performStudyJob, jobParameters);
    return jobExec;
  }

  @GET
  @Path("/{id}/info")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional(readOnly = true)
  public List<StudyInfoDTO> getSimulationInfo(@PathParam("id") final int id) {
    FeasibilityStudy study = this.feasibilityStudyRepository.findOne(id);

    List<StudyInfoDTO> result = new ArrayList<>();
    for (StudyGenerationInfo generationInfo : study.getStudyGenerationInfoList()) {
      StudyInfoDTO info = new StudyInfoDTO();
      info.generationInfo = generationInfo;
      info.summary = getSimulationSummary(id, generationInfo.getSource());
      result.add(info);
    }
    return result;
  }

  @GET
  @Path("/{id}/report/{sourceKey}")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public FeasibilityReport getSimulationReport(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey) {

    Source source = this.getSourceRepository().findBySourceKey(sourceKey);

    FeasibilityReport.Summary summary = getSimulationSummary(id, source);
    List<FeasibilityReport.InclusionRuleStatistic> inclusionRuleStats = getSimulationInclusionRuleStatistics(id, source);
    String treemapData = getInclusionRuleTreemapData(id, inclusionRuleStats.size(), source);

    FeasibilityReport report = new FeasibilityReport();
    report.summary = summary;
    report.inclusionRuleStats = inclusionRuleStats;
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
  public FeasibilityStudyDTO copy(@PathParam("id") final int id) {
    FeasibilityStudyDTO sourceStudy = getStudy(id);
    sourceStudy.id = null; // clear the ID
    sourceStudy.name = "COPY OF: " + sourceStudy.name;

    FeasibilityStudyDTO copyStudy = createStudy(sourceStudy);
    return copyStudy;
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
    feasibilityStudyRepository.delete(id);
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
    FeasibilityStudy study = feasibilityStudyRepository.findOne(id);
    StudyGenerationInfo itemToRemove = null;
    for (StudyGenerationInfo info : study.getStudyGenerationInfoList())
    {
      if (info.getSource().getSourceKey().equals(sourceKey))
        itemToRemove = info;
    }
    
    if (itemToRemove != null)
      study.getStudyGenerationInfoList().remove(itemToRemove);
    
    feasibilityStudyRepository.save(study);
  }
  
}
