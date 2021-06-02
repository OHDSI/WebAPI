package org.ohdsi.webapi.annotation.result;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.annotation.result.ResultService;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.ArrayList;
import java.util.List;

@Path("annotation/results/")
@Component
public class ResultController {

  @Autowired
  private ResultService resultService;

  @GET
  @Path("/{annotationID}")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList getResults(@PathVariable("annotationID") String annotationID) {
    ArrayList al = new ArrayList();

    return al;
  }
}
