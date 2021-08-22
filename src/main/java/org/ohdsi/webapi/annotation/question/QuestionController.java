package org.ohdsi.webapi.annotation.question;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Path("annotations/")
@Component
public class QuestionController {

  @Autowired
  private QuestionService questionService;

  @GET
  @Path("questions")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Question> getAllQuestions() {
    return questionService.getAllQuestions();
  }

  @POST
  @Path("questions")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void addQuestion(Question question) {

    question.getAnswers().forEach((answer) -> {
			question.addToAnswers(answer);
		});

    questionService.addQuestion(question);
  }
}
