/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.RoundingMode;
import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import javax.servlet.ServletContext;
import javax.transaction.Transactional;
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
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.sql.SqlTranslate;
import javax.ws.rs.core.Response;
import org.ohdsi.sql.SqlRender;

import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;
import org.ohdsi.webapi.cohortdefinition.GenerateCohortTasklet;
import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortdefinition.CleanupCohortTasklet;
import org.ohdsi.webapi.cohortdefinition.GenerationJobExecutionListener;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.cohortfeatures.GenerateCohortFeaturesTasklet;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.conceptset.ExportUtil;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.source.SourceInfo;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

/**
 *
 * @author cknoll1
 */
@Path("/cohortdefinition")
@Component
public class CohortDefinitionService extends AbstractDaoService {

  private static final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();

  @Autowired
  private Security security;

  @Autowired
  private CohortDefinitionRepository cohortDefinitionRepository;

  @Autowired
  private JobBuilderFactory jobBuilders;

  @Autowired
  private StepBuilderFactory stepBuilders;

  @Autowired
  private VocabularyService vocabService;
  
  @Autowired
  private SourceService sourceService;
    
  @Autowired
  private JobTemplate jobTemplate;

	@PersistenceContext
	protected EntityManager entityManager;
	
  private final RowMapper<InclusionRuleReport.Summary> summaryMapper = new RowMapper<InclusionRuleReport.Summary>() {
    @Override
    public InclusionRuleReport.Summary mapRow(ResultSet rs, int rowNum) throws SQLException {
      InclusionRuleReport.Summary summary = new InclusionRuleReport.Summary();
      summary.baseCount = rs.getLong("base_count");
      summary.finalCount = rs.getLong("final_count");

      double matchRatio = (summary.baseCount > 0) ? ((double) summary.finalCount / (double) summary.baseCount) : 0.0;
      summary.percentMatched = new BigDecimal(matchRatio * 100.0).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
      return summary;
    }
  };

  private final RowMapper<InclusionRuleReport.InclusionRuleStatistic> inclusionRuleStatisticMapper = new RowMapper<InclusionRuleReport.InclusionRuleStatistic>() {

    @Override
    public InclusionRuleReport.InclusionRuleStatistic mapRow(ResultSet rs, int rowNum) throws SQLException {
      InclusionRuleReport.InclusionRuleStatistic statistic = new InclusionRuleReport.InclusionRuleStatistic();
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
  
  private final RowMapper<Long[]> inclusionRuleResultItemMapper = new RowMapper<Long[]>() {

    @Override
    public Long[] mapRow(ResultSet rs, int rowNum) throws SQLException {
      Long[] resultItem = new Long[2];
      resultItem[0] = rs.getLong("inclusion_rule_mask");
      resultItem[1] = rs.getLong("person_count");
      return resultItem;
    }
  };  

  private CohortGenerationInfo findBySourceId(Set<CohortGenerationInfo> infoList, Integer sourceId) {
    for (CohortGenerationInfo info : infoList) {
      if (info.getId().getSourceId().equals(sourceId)) {
        return info;
      }
    }
    return null;
  }
  
  private InclusionRuleReport.Summary getInclusionRuleReportSummary(int id, Source source) {

    String sql = "select base_count, final_count from @tableQualifier.cohort_summary_stats where cohort_definition_id = @id";
    String tqName = "tableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, "id", whitelist(id), SessionUtils.sessionId());
    List<InclusionRuleReport.Summary> result = getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), summaryMapper);
    return result.isEmpty()? new InclusionRuleReport.Summary() : result.get(0);
  }

  private List<InclusionRuleReport.InclusionRuleStatistic> getInclusionRuleStatistics(int id, Source source) {

    String sql = "select i.rule_sequence, i.name, s.person_count, s.gain_count, s.person_total"
        + " from @tableQualifier.cohort_inclusion i join @tableQualifier.cohort_inclusion_stats s on i.cohort_definition_id = s.cohort_definition_id"
        + " and i.rule_sequence = s.rule_sequence"
        + " where i.cohort_definition_id = @id ORDER BY i.rule_sequence";
    String tqName = "tableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, "id", whitelist(id), SessionUtils.sessionId());
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), inclusionRuleStatisticMapper);
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
  
  private String getInclusionRuleTreemapData(int id, int inclusionRuleCount, Source source) {

    String sql = "select inclusion_rule_mask, person_count from @tableQualifier.cohort_inclusion_result where cohort_definition_id = @id";
    String tqName = "tableQualifier";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, "id", id, SessionUtils.sessionId());

    // [0] is the inclusion rule bitmask, [1] is the count of the match
    List<Long[]> items = this.getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), inclusionRuleResultItemMapper);
    Map<Integer, List<Long[]>> groups = new HashMap<>();
    for (Long[] item : items) {
      int bitsSet = countSetBits(item[0]);
      if (!groups.containsKey(bitsSet)) {
        groups.put(bitsSet, new ArrayList<Long[]>());
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

  public static class GenerateSqlRequest {

    public GenerateSqlRequest() {
    }

    @JsonProperty("expression")
    public CohortExpression expression;

    @JsonProperty("options")
    public CohortExpressionQueryBuilder.BuildExpressionQueryOptions options;

  }

  public static class GenerateSqlResult {

    @JsonProperty("templateSql")
    public String templateSql;
  }

  public static class CohortDefinitionListItem {

    public Integer id;
    public String name;
    public String description;
    public ExpressionType expressionType;
    public String createdBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm")
    public Date createdDate;
    public String modifiedBy;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd, HH:mm")
    public Date modifiedDate;
  }

  public static class CohortDefinitionDTO extends CohortDefinitionListItem {

    public String expression;
  }

  public CohortDefinitionDTO cohortDefinitionToDTO(CohortDefinition def) {
    CohortDefinitionDTO result = new CohortDefinitionDTO();

    result.id = def.getId();
    result.createdBy = def.getCreatedBy();
    result.createdDate = def.getCreatedDate();
    result.description = def.getDescription();
    result.expressionType = def.getExpressionType();
    result.expression = def.getDetails() != null ? def.getDetails().getExpression() : null;
    result.modifiedBy = def.getModifiedBy();
    result.modifiedDate = def.getModifiedDate();
    result.name = def.getName();

    return result;
  }

  @Context
  ServletContext context;

  @Path("sql")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public GenerateSqlResult generateSql(GenerateSqlRequest request) {
    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = request.options;
    GenerateSqlResult result = new GenerateSqlResult();
    if (options == null)
    {
      options = new CohortExpressionQueryBuilder.BuildExpressionQueryOptions();
    }
    String expressionSql = queryBuilder.buildExpressionQuery(request.expression, options);
    result.templateSql = SqlRender.renderSql(expressionSql, null, null);

    return result;
  }

  /**
   * Returns all cohort definitions in the cohort schema
   *
   * @return List of cohort_definition
   */
  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<CohortDefinitionListItem> getCohortDefinitionList() {
    ArrayList<CohortDefinitionListItem> result = new ArrayList<>();
    List<Object[]> defs = entityManager.createQuery("SELECT cd.id, cd.name, cd.description, cd.expressionType, cd.createdBy, cd.createdDate, cd.modifiedBy, cd.modifiedDate FROM CohortDefinition cd").getResultList();
    for (Object[] d : defs) {
      CohortDefinitionListItem item = new CohortDefinitionListItem();
      item.id = (Integer)d[0];
      item.name = (String)d[1];
      item.description = (String)d[2];
      item.expressionType = (ExpressionType)d[3];
      item.createdBy = (String)d[4];
      item.createdDate = (Date)d[5];
      item.modifiedBy = (String)d[6];
      item.modifiedDate = (Date)d[7];
      result.add(item);
    }
    return result;
  }

  /**
   * Creates the cohort definition
   *
   * @param def The cohort definition to create.
   * @return The new CohortDefinition
   */
  @POST
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public CohortDefinitionDTO createCohortDefinition(CohortDefinitionDTO def) {
    Date currentTime = Calendar.getInstance().getTime();

    //create definition in 2 saves, first to get the generated ID for the new def
    // then to associate the details with the definition
    CohortDefinition newDef = new CohortDefinition();
    newDef.setName(def.name)
            .setDescription(def.description)
            .setCreatedBy(security.getSubject())
            .setCreatedDate(currentTime)
            .setExpressionType(def.expressionType);

    newDef = this.cohortDefinitionRepository.save(newDef);

    // associate details
    CohortDefinitionDetails details = new CohortDefinitionDetails();
    details.setCohortDefinition(newDef)
            .setExpression(def.expression);

    newDef.setDetails(details);

    CohortDefinition createdDefinition = this.cohortDefinitionRepository.save(newDef);

    return cohortDefinitionToDTO(createdDefinition);
  }

  /**
   * Returns the cohort definition for the given id
   *
   * @param id The cohort definition id
   * @return The CohortDefinition
   */
  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDefinitionDTO getCohortDefinition(@PathParam("id") final int id) {
    CohortDefinition d = this.cohortDefinitionRepository.findOneWithDetail(id);
    return cohortDefinitionToDTO(d);
  }

  /**
   * Saves the cohort definition for the given id
   *
   * @param id The cohort definition id
   * @return The CohortDefinition
   */
  @PUT
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public CohortDefinitionDTO saveCohortDefinition(@PathParam("id") final int id, CohortDefinitionDTO def) {
    Date currentTime = Calendar.getInstance().getTime();

    CohortDefinition currentDefinition = this.cohortDefinitionRepository.findOneWithDetail(id);

    currentDefinition.setName(def.name)
            .setDescription(def.description)
            .setExpressionType(def.expressionType)
            .setModifiedBy(security.getSubject())
            .setModifiedDate(currentTime)
            .getDetails().setExpression(def.expression);

    this.cohortDefinitionRepository.save(currentDefinition);
    return getCohortDefinition(id);
  }

  /**
   * Queues up a generate cohort task for the specified cohort definition id.
   *
   * @param id - the Cohort Definition ID to generate
   * @return information about the Cohort Analysis Job
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}/generate/{sourceKey}")
  public JobExecutionResource generateCohort(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, @QueryParam("includeFeatures") final String includeFeatures) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);    
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);    
    String vocabularyTableQualifier = source.getTableQualifierOrNull(SourceDaimon.DaimonType.Vocabulary);
		
    DefaultTransactionDefinition requresNewTx = new DefaultTransactionDefinition();
    requresNewTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    TransactionStatus initStatus = this.getTransactionTemplate().getTransactionManager().getTransaction(requresNewTx);

    CohortDefinition currentDefinition = this.cohortDefinitionRepository.findOne(id);
    CohortGenerationInfo info = findBySourceId(currentDefinition.getGenerationInfoList(), source.getSourceId());
    if (info == null) {
      info = new CohortGenerationInfo(currentDefinition, source.getSourceId());
      currentDefinition.getGenerationInfoList().add(info);
    }
    info.setStatus(GenerationStatus.PENDING)
      .setStartTime(Calendar.getInstance().getTime());

		info.setIncludeFeatures(includeFeatures != null);
		
    this.cohortDefinitionRepository.save(currentDefinition);
    this.getTransactionTemplate().getTransactionManager().commit(initStatus);

    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString("jobName", "generating cohort " + currentDefinition.getId() + " : " + source.getSourceName() + " (" + source.getSourceKey() + ")");
    builder.addString("cdm_database_schema", cdmTableQualifier);
    builder.addString("results_database_schema", resultsTableQualifier);
    builder.addString("target_database_schema", resultsTableQualifier);
		if (vocabularyTableQualifier != null)
			builder.addString("vocabulary_database_schema", vocabularyTableQualifier);
    builder.addString("target_dialect", source.getSourceDialect());
    builder.addString("target_table", "cohort");
    builder.addString("cohort_definition_id", ("" + id));
    builder.addString("source_id", ("" + source.getSourceId()));
    builder.addString("generate_stats", Boolean.TRUE.toString());

    final JobParameters jobParameters = builder.toJobParameters();

    log.info(String.format("Beginning generate cohort for cohort definition id: \n %s", "" + id));

    GenerateCohortTasklet generateTasklet = new GenerateCohortTasklet(getSourceJdbcTemplate(source), getTransactionTemplate(), cohortDefinitionRepository);

    Step generateCohortStep = stepBuilders.get("cohortDefinition.generateCohort")
      .tasklet(generateTasklet)
    .build();

		SimpleJobBuilder generateJobBuilder = jobBuilders.get("generateCohort")
			.listener(new GenerationJobExecutionListener(cohortDefinitionRepository, this.getTransactionTemplateRequiresNew(), this.getSourceJdbcTemplate(source)))
			.start(generateCohortStep);

		if (includeFeatures != null) {
			GenerateCohortFeaturesTasklet generateCohortFeaturesTasklet = 
						new GenerateCohortFeaturesTasklet(getSourceJdbcTemplate(source), getTransactionTemplate());

			Step generateCohortFeaturesStep = stepBuilders.get("cohortFeatures.generateFeatures")
					.tasklet(generateCohortFeaturesTasklet)
					.build();
	
			generateJobBuilder.next(generateCohortFeaturesStep);			
		}
		
		Job generateCohortJob = generateJobBuilder.build();

    JobExecutionResource jobExec = this.jobTemplate.launch(generateCohortJob, jobParameters);
    return jobExec;
  }

  /**
   * Queues up a generate cohort task for the specified cohort definition id.
   *
   * @param id - the Cohort Definition ID to generate
   * @return information about the Cohort Analysis Job
   * @throws Exception
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @Path("/{id}/info")
  @Transactional
  public List<CohortGenerationInfo> getInfo(@PathParam("id") final int id) {
    CohortDefinition def = this.cohortDefinitionRepository.findOne(id);
    Set<CohortGenerationInfo> infoList = def.getGenerationInfoList();

    List<CohortGenerationInfo> result = new ArrayList<>();
    for (CohortGenerationInfo info : infoList) {
      result.add(info);
    }
    return result;
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
  @Transactional
  public CohortDefinitionDTO copy(@PathParam("id") final int id) {
    CohortDefinitionDTO sourceDef = getCohortDefinition(id);
    sourceDef.id = null; // clear the ID
    sourceDef.name = "COPY OF: " + sourceDef.name;

    CohortDefinitionDTO copyDef = createCohortDefinition(sourceDef);

    return copyDef;
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
		
		// perform the JPA update in a separate transaction
		this.getTransactionTemplateRequiresNew().execute(new TransactionCallbackWithoutResult() {
			@Override
			public void doInTransactionWithoutResult(final TransactionStatus status) {
				cohortDefinitionRepository.delete(id);
			}
		});

		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addString("jobName", String.format("Cleanup cohort %d.",id));
		builder.addString("cohort_definition_id", ("" + id));

		final JobParameters jobParameters = builder.toJobParameters();

		log.info(String.format("Beginning cohort cleanup for cohort definition id: \n %s", "" + id));

		CleanupCohortTasklet cleanupTasklet = new CleanupCohortTasklet(this.getTransactionTemplateNoTransaction(),this.getSourceRepository());

		Step cleanupStep = stepBuilders.get("cohortDefinition.cleanupCohort")
			.tasklet(cleanupTasklet)
			.build();

		SimpleJobBuilder cleanupJobBuilder = jobBuilders.get("cleanupCohort")
			.start(cleanupStep);

		Job cleanupCohortJob = cleanupJobBuilder.build();

		this.jobTemplate.launch(cleanupCohortJob, jobParameters);
	}
	
  private ArrayList<ConceptSetExport> getConceptSetExports(CohortDefinition def, SourceInfo vocabSource) throws RuntimeException {
    ArrayList<ConceptSetExport> exports = new ArrayList<>();
    ObjectMapper mapper = new ObjectMapper();
    CohortExpression expression;
    try {
      expression = mapper.readValue(def.getDetails().getExpression(), CohortExpression.class);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    for (ConceptSet cs : expression.conceptSets) {
      ConceptSetExport export = new ConceptSetExport();

      // Copy the concept set fields
      export.ConceptSetId = cs.id;
      export.ConceptSetName = cs.name;
      export.csExpression = cs.expression;

      // Lookup the identifiers
      export.identifierConcepts = vocabService.executeIncludedConceptLookup(vocabSource.sourceKey, cs.expression);
      // Lookup the mapped items
      export.mappedConcepts = vocabService.executeMappedLookup(vocabSource.sourceKey, cs.expression);

      exports.add(export);
    }
    return exports;
  }

  @GET
  @Path("/{id}/export/conceptset")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  public Response exportConceptSets(@PathParam("id") final int id)
  {
    
    SourceInfo sourceInfo = sourceService.getPriorityVocabularySourceInfo();
    CohortDefinition def = this.cohortDefinitionRepository.findOneWithDetail(id);
    
    ArrayList<ConceptSetExport> exports = getConceptSetExports(def, sourceInfo);
    ByteArrayOutputStream exportStream = ExportUtil.writeConceptSetExportToCSVAndZip(exports);

    Response response = Response
            .ok(exportStream)
            .type(MediaType.APPLICATION_OCTET_STREAM)
            .header("Content-Disposition", String.format("attachment; filename=\"cohortdefinition_%d_export.zip\"", def.getId()))
            .build();

    return response;
    
  }    

  @GET
  @Path("/{id}/report/{sourceKey}")
  @Produces(MediaType.APPLICATION_JSON)
  @Transactional
  public InclusionRuleReport getInclusionRuleReport(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey) {

    Source source = this.getSourceRepository().findBySourceKey(sourceKey);

    InclusionRuleReport.Summary summary = getInclusionRuleReportSummary(whitelist(id), source);
    List<InclusionRuleReport.InclusionRuleStatistic> inclusionRuleStats = getInclusionRuleStatistics(whitelist(id), source);
    String treemapData = getInclusionRuleTreemapData(whitelist(id), inclusionRuleStats.size(), source);

    InclusionRuleReport report = new InclusionRuleReport();
    report.summary = summary;
    report.inclusionRuleStats = inclusionRuleStats;
    report.treemapData = treemapData;

    return report;
  }  
}
