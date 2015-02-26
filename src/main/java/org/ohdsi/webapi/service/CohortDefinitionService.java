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
import org.apache.commons.lang3.StringUtils;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortExpressionQueryBuilder;
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

  public class GenerateSqlResult {

    @JsonProperty("templateSql")
    public String templateSql;
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
  public List<CohortDefinition> getCohortDefinitionList() {
    ArrayList<CohortDefinition> defs = new ArrayList<>();
    for (CohortDefinition d : this.cohortDefinitionRepository.findAll())
      defs.add(d);
    return defs;
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
    
    if (details != null) // need to sync object on both ends
    {
      details.setCohortDefinition(createdDefinition);
      createdDefinition.setDetails(details);
      createdDefinition = this.cohortDefinitionRepository.save(createdDefinition);
    }
    
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
    return this.cohortDefinitionRepository.findOne(id);
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

    CohortDefinition currentDefinition = this.cohortDefinitionRepository.findOne(id);
    currentDefinition.setName(def.getName())
            .setDescription(def.getDescription())
            .setExpressionType(def.getExpressionType())
            .setModifiedBy("system")
            .setModifiedDate(currentTime);    
    if (currentDefinition.getDetails() != null)
    {
      if (def.getDetails() != null)
        currentDefinition.getDetails().setExpression(def.getDetails().getExpression());
      else
        currentDefinition.setDetails(null);
    }
    else
    {
      if (def.getDetails() != null)
      {
        currentDefinition.setDetails(def.getDetails());
        currentDefinition.getDetails().setCohortDefinition(currentDefinition);
      }
    }
    
    currentDefinition = this.cohortDefinitionRepository.save(currentDefinition);
    return currentDefinition;
  }  
}
