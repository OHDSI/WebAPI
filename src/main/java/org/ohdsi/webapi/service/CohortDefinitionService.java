/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import com.fasterxml.jackson.annotation.JsonProperty;
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
import org.ohdsi.webapi.sqlrender.SourceStatement;
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

    String sql = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/getCohortDefinitions.sql");
      sql = SqlRender.renderSql(sql, new String[] { "results_schema" }, new String[] { this.getOhdsiSchema()});
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
      sql_statement = SqlRender.renderSql(sql_statement, new String[] { "id", "results_schema" },
          new String[] { String.valueOf(id), this.getOhdsiSchema() });
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
