package org.ohdsi.webapi.annotation.set;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.annotation.set.QuestionSetService;
import org.ohdsi.webapi.annotation.set.QuestionSet;

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
