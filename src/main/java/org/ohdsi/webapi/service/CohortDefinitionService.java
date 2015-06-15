/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.ohdsi.webapi.TerminateJobStepExceptionHandler;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;
import org.ohdsi.webapi.cohortdefinition.GenerateCohortTasklet;
import org.ohdsi.webapi.cohortdefinition.GenerationStatus;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author cknoll1
 */
@Path("/cohortdefinition/")
@Component
public class CohortDefinitionService extends AbstractDaoService {

  @Autowired
  private CohortDefinitionRepository cohortDefinitionRepository;  

  @Autowired
  private JobBuilderFactory jobBuilders;

  @Autowired
  private StepBuilderFactory stepBuilders;
  
  @Autowired
  private JobTemplate jobTemplate;  

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
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd, HH:mm")    
    public Date createdDate;
    public String modifiedBy;
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd, HH:mm")    
    public Date modifiedDate;
  }
  
  public static class CohortDefinitionDTO extends CohortDefinitionListItem {
    public String expression;
  }

  public CohortDefinitionDTO cohortDefinitionToDTO(CohortDefinition def)
  {
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
  
  private static final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();

  @Context
  ServletContext context;

  @Path("sql")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public GenerateSqlResult generateSql(GenerateSqlRequest request) {
    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = request.options;
    if (options != null) {
      options.cohortId = options.cohortId == null ? -1 : options.cohortId;
      options.cdmSchema = (options.cdmSchema == null || options.cdmSchema.trim().length() == 0) ? this.getCdmSchema() : options.cdmSchema.trim();
      options.targetTable = (options.targetTable == null || options.targetTable.trim().length() == 0) ? "cohort" : options.targetTable.trim();
    }
    
    GenerateSqlResult result = new GenerateSqlResult();
    result.templateSql = queryBuilder.buildExpressionQuery(request.expression, options);

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
    Iterable<CohortDefinition> defs = this.cohortDefinitionRepository.findAll();
    for (CohortDefinition d : defs)
    {
      CohortDefinitionListItem item = new CohortDefinitionListItem();
      item.id = d.getId();
      item.name = d.getName();
      item.description = d.getDescription();
      item.expressionType = d.getExpressionType();
      item.createdBy = d.getCreatedBy();
      item.createdDate = d.getCreatedDate();
      item.modifiedBy = d.getModifiedBy();
      item.modifiedDate = d.getModifiedDate();
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
  @PUT
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
            .setCreatedBy("system")
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
            .setModifiedBy("system")
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
  @Path("/{id}/generate")    
  public JobExecutionResource generateCohort(@PathParam("id") final int id) {

    CohortDefinition currentDefinition = this.cohortDefinitionRepository.findOneWithDetail(id);
    CohortGenerationInfo info = currentDefinition.getGenerationInfo();
    if (info == null)
    {
      info = new CohortGenerationInfo(currentDefinition);
      currentDefinition.setGenerationInfo(info);
    }
    info.setStatus(GenerationStatus.PENDING)
      .setStartTime(Calendar.getInstance().getTime());

    this.cohortDefinitionRepository.save(currentDefinition);

    JobParametersBuilder builder = new JobParametersBuilder();
    builder.addString("cdm_database_schema", this.getCdmSchema());
    builder.addString("target_database_schema", this.getOhdsiSchema());
    builder.addString("target_dialect", this.getDialect());
    builder.addString("target_table", "cohort");
    builder.addString("cohort_definition_id", ("" + id));

    final JobParameters jobParameters = builder.toJobParameters();

    log.info(String.format("Beginning generate cohort for cohort definition id: \n %s", "" + id));

    GenerateCohortTasklet generateTasklet = new GenerateCohortTasklet(getJdbcTemplate(), getTransactionTemplate(), cohortDefinitionRepository);

    Step generateCohortStep = stepBuilders.get("cohortDefinition.generateCohort")
      .tasklet(generateTasklet)
      .exceptionHandler(new TerminateJobStepExceptionHandler())
    .build();

    Job generateCohortJob = jobBuilders.get("generateCohort")
      .start(generateCohortStep)
      .build();

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
  public CohortGenerationInfo getInfo(@PathParam("id") final int id) {
    CohortDefinition def = this.cohortDefinitionRepository.findOne(id);
    return def.getGenerationInfo();
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
   cohortDefinitionRepository.delete(id);
  }      
}
