/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;

import org.ohdsi.webapi.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortExpressionQueryBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 *
 * @author cknoll1
 */
@Path("/cohortdefinition/")
@Component
public class CohortDefinitionService {

  private static final CohortExpressionQueryBuilder queryBuilder = new CohortExpressionQueryBuilder();
  
  @Context
  ServletContext context;

    @Value("${datasource.cdm.schema}")
    private String cdmSchema;
    
    @Value("${datasource.dialect}")
    private String dialect;
    
  @Path("generate")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public String generateSql(CohortExpression expression) {
    
    String query = queryBuilder.buildExpressionQuery(expression);
    
    query = SqlRender.renderSql(query, new String[] { "CDM_schema"}, new String[] { this.cdmSchema});
    String sql_statement = SqlTranslate.translateSql(query, "sql server", this.dialect);
    
    return sql_statement;
  }
}
