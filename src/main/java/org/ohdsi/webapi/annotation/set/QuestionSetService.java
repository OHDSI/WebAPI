package org.ohdsi.webapi.annotation.set;

import org.ohdsi.webapi.annotation.annotation.AnnotationService;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;

@Service
public class QuestionSetService {

  @Autowired
  private QuestionSetRepository questionSetRepository;

  @Autowired
  private AnnotationService annotationService;

  public List<QuestionSet> getSets() {
    List<QuestionSet> sets = new ArrayList();
    questionSetRepository.findAll()
    .forEach(sets::add);
    return sets;
  }

  public List<QuestionSet> getSetsByCohortId(Integer cohortId) {
    List<QuestionSet> sets = new ArrayList();
    questionSetRepository.findByCohortId(cohortId)
    .forEach(sets::add);
    return sets;
  }

  public void addSet(QuestionSet set) {
    questionSetRepository.save(set);
  }

  public QuestionSet findQuestionSetByQuestionSetId(int questionSetId){
    return questionSetRepository.findById(questionSetId);
  }

  public List<QuestionSampleDto> getSamplesAndSetsByCohortId(int cohortId) {
    return questionSetRepository.findSamplesAndSetsByCohortId(cohortId);
  }

  public boolean deleteQuestionSet(int questionSetId) {
    if(annotationService.getAnnotationsByQuestionSetId(questionSetId).isEmpty()){
      questionSetRepository.delete(questionSetId);
      return true;
    }
    else{
      return false;
    }
  }
}
