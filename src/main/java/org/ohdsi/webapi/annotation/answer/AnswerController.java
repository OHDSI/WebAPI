package org.ohdsi.webapi.annotation.answer;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("annotation/")
@Component
public class AnswerController {

  @Autowired
  private AnswerService answerService;

  @POST
  @Path("answers")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void addQuestion(Answer answer) {
    answerService.addAnswer(answer);
  }
}
