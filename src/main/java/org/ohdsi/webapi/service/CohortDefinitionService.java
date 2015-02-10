/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.model.CohortDefinition;
import org.ohdsi.webapi.sqlrender.TranslatedStatement;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 * @author cknoll1
 */
@Path("/cohortdefinition/")
@Component
public class CohortDefinitionService extends AbstractDaoService {

  private static final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
  
  @Context
  ServletContext context;
  
  private final RowMapper<CohortDefinition> cohortDefinitionMapper = new RowMapper<CohortDefinition>() {
      
      @Override
      public CohortDefinition mapRow(final ResultSet rs, final int arg1) throws SQLException {
          final CohortDefinition definition = new CohortDefinition();
          definition.setCohortDefinitionDescription(rs.getString(CohortDefinition.COHORT_DEFINITION_DESCRIPTION));
          definition.setCohortDefinitionId(rs.getInt(CohortDefinition.COHORT_DEFINITION_ID));
          definition.setCohortDefinitionName(rs.getString(CohortDefinition.COHORT_DEFINITION_NAME));
          definition.setCohortDefinitionSyntax(rs.getString(CohortDefinition.COHORT_DEFINITION_SYNTAX));
          definition.setCohortInitiationDate(rs.getDate(CohortDefinition.COHORT_INITIATION_DATE));
          return definition;
      }
  };
    
  @Path("generate")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public TranslatedStatement generateSql(CohortExpression expression) {
    
    String query = queryBuilder.buildExpressionQuery(expression);
    
    query = SqlRender.renderSql(query, new String[] { "CDM_schema"}, new String[] { getCdmSchema()});
    String translatedSql = SqlTranslate.translateSql(query, "sql server", getDialect());
    
    TranslatedStatement ts = new TranslatedStatement();
    ts.targetSQL = translatedSql;
    return ts;
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
      
      String sql = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/getCohortDefinitions.sql");
      sql = SqlRender.renderSql(sql, new String[] { "cohort_schema" }, new String[] { this.getCohortSchema() });
      sql = SqlTranslate.translateSql(sql, getSourceDialect(), getDialect());
      
      return getJdbcTemplate().query(sql, this.cohortDefinitionMapper);
  }
  
  /**
   * Returns the cohort definition for the given cohort_definition_id in the cohort schema
   * 
   * @param id The cohort_definition id
   * @return The cohort_defition
   */
  @GET
  @Path("/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public CohortDefinition getCohortDefinition(@PathParam("id") final int id) {
      
      String sql_statement = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/getCohortDefinitionsById.sql");
      sql_statement = SqlRender.renderSql(sql_statement, new String[] { "id", "cohort_schema" },
          new String[] { String.valueOf(id), this.getCohortSchema() });
      sql_statement = SqlTranslate.translateSql(sql_statement, getSourceDialect(), getDialect());
      
      CohortDefinition def = null;
      try {
          def = getJdbcTemplate().queryForObject(sql_statement, this.cohortDefinitionMapper);
      } catch (EmptyResultDataAccessException e) {
          log.debug(String.format("Request for cohortDefinition=%s resulted in 0 results", id));
          //returning null / i.e. no content
      }
      return def;
  }
  
  

}
