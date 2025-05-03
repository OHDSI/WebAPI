package org.ohdsi.webapi.annotation.answer;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class AnswerService {

  @Autowired
  private AnswerRepository answerRepository;

  public void addAnswer(Answer answer) {
    answerRepository.save(answer);
  }

  public Answer getAnswerById(int answerId){return answerRepository.findById(answerId);}
}
