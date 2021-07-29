package org.ohdsi.webapi.annotation.result;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.annotation.annotation.AnnotationService;
import org.ohdsi.webapi.annotation.annotation.AnnotationSummary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.annotation.result.ResultService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Path("/annotations/results")
@Component
public class ResultController {

  @Autowired
  private ResultService resultService;

  @Autowired
  private AnnotationService annotationService;

  @GET
  @Path("/{annotationID}")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList getResults(@PathParam("annotationID") String annotationID) {
    int annotationIdInt=Integer.parseInt(annotationID);
    ArrayList al = new ArrayList();
    Annotation ourAnno =annotationService.getAnnotationsByAnnotationId(annotationIdInt);
    List<Result> tempResults = resultService.getResultsByAnnotationID(annotationIdInt);
    for(Result tempResult : tempResults ){
      System.out.println(tempResult);
      Map<String, String> testDict =new HashMap<String, String>();
      testDict.put("questionId",tempResult.getQuestionId().toString());
      testDict.put("answerId",tempResult.getAnswerId().toString());
      testDict.put("subjectId",Integer.toString(ourAnno.getSubjectId()));
      al.add(testDict);
    }
    return al;
  }

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Result> getResults(
          @QueryParam("questionId") final int questionId
  ) {
    List<Result> results = null;
    List<Annotation> blah = annotationService.getAnnotationsByQuestionSetId(questionId);
    return results;
  }

  @Path("/{cohortDefinitionId}/{sourceKey}")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void createResult(){

  }
}
