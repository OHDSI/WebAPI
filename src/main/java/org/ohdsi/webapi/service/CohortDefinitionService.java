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

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

/**
 *
 * @author cknoll1
 */
@Path("/cohortdefinition/")
public class CohortDefinitionService {

  @Context
  ServletContext context;

  @Path("generate")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public CohortDefinition generateSql(CohortDefinition definition) {
    return definition;
  }
}
