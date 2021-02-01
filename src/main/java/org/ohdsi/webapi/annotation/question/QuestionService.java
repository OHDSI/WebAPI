package org.ohdsi.webapi.annotation.question;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import org.ohdsi.webapi.annotation.question.QuestionRepository;
import org.ohdsi.webapi.annotation.question.Question;

@Service
public class QuestionService {

  @Autowired
  private QuestionRepository questionRepository;

  public List<Question> getAllQuestions() {
    List<Question> questions = new ArrayList();
    questionRepository.findAll()
    .forEach(questions::add);
    return questions;
  }

  public void addQuestion(Question question) {
    questionRepository.save(question);
  }

}
