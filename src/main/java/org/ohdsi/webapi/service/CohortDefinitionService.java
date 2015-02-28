/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;
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
    public Date createdDate;
    public String modifiedBy;
    public Date modifiedDate;
  }

  
  private static final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();

  @Context
  ServletContext context;

  @Path("generate")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public GenerateSqlResult generateSql(GenerateSqlRequest request) {
    CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = request.options;
    if (options != null) {
      options.cohortId = options.cohortId == null ? -1 : options.cohortId;
      options.cdmSchema = (options.cdmSchema == null || options.cdmSchema.trim().length() == 0) ? this.getCdmSchema() : options.cdmSchema.trim();
      options.targetTable = (options.targetTable == null || options.targetTable.trim().length() == 0) ? "cohort" : options.targetTable.trim();
      options.targetSchema = (options.targetSchema == null || options.targetSchema.trim().length() == 0) ? this.getOhdsiSchema() : options.targetSchema.trim();
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
  public CohortDefinition createCohortDefinition(CohortDefinition def) {
    Date currentTime = Calendar.getInstance().getTime();

    CohortDefinitionDetails details = def.getDetails();
    def.setDetails(null); // we don't know the foreign key yet, and we can't save it with a NULL value
    
    def.setCreatedBy("system")
      .setCreatedDate(currentTime);

    CohortDefinition createdDefinition = this.cohortDefinitionRepository.save(def);
    
    // associate details to newly created definition
    details.setCohortDefinition(createdDefinition);
    createdDefinition.setDetails(details);
    createdDefinition = this.cohortDefinitionRepository.save(createdDefinition);

    return createdDefinition;
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
  public CohortDefinition getCohortDefinition(@PathParam("id") final int id) {
    CohortDefinition d = this.cohortDefinitionRepository.findById(id);
    return d;
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
  public CohortDefinition getCohortDefinition(@PathParam("id") final int id, CohortDefinition def) {
    Date currentTime = Calendar.getInstance().getTime();

    CohortDefinition currentDefinition = this.cohortDefinitionRepository.findById(id);
    currentDefinition.setName(def.getName())
            .setDescription(def.getDescription())
            .setExpressionType(def.getExpressionType())
            .setModifiedBy("system")
            .setModifiedDate(currentTime);    

    currentDefinition.getDetails().setExpression(def.getDetails().getExpression());
    
    this.cohortDefinitionRepository.save(currentDefinition);
    return this.cohortDefinitionRepository.findById(id);
  }  
}
