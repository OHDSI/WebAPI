package org.ohdsi.webapi.annotation.set;

import java.util.List;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Path("annotations/")
@Component
public class QuestionSetController {

  @Autowired
  private QuestionSetService questionSetService;

  @GET
  @Path("sets")
  @Produces(MediaType.APPLICATION_JSON)
  public List<QuestionSet> getSets(@QueryParam("cohortId") final Integer cohortId) {

    if (cohortId != null) {
      return questionSetService.getSetsByCohortId(cohortId);
    }

    return questionSetService.getSets();
  }

  @GET
  @Path("getsets")
  @Produces(MediaType.APPLICATION_JSON)
  public List<QuestionSampleDto> getSets(
          @QueryParam("cohortId") final int cohortId
  ) {
    return questionSetService.getSamplesAndSetsByCohortId(cohortId);
  }

  @GET
  @Path("deleteSet/{questionSetId}")
  @Produces(MediaType.APPLICATION_JSON)
  public ResponseEntity<?> deleteSet(
          @PathParam("questionSetId") int questionSetId
  ) {
    if(questionSetService.deleteQuestionSet(questionSetId)){
      return ResponseEntity.status(200).body("The Question Set has been deleted");
    }
    else{
      return ResponseEntity.status(400).body("Could not delete the Question Set");
    }
  }

  @POST
  @Path("sets")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void addSet(QuestionSet set) {

    set.getQuestions().forEach((question) -> {
      set.addToQuestions(question);
      question.getAnswers().forEach((answer) -> {
        question.addToAnswers(answer);
      });
    });

    questionSetService.addSet(set);
  }
}
