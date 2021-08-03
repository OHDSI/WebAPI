package org.ohdsi.webapi.annotation.set;

import org.ohdsi.webapi.annotation.question.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;
import org.ohdsi.webapi.annotation.set.QuestionSet;
import org.springframework.data.jpa.repository.Query;


public interface QuestionSetRepository extends JpaRepository<QuestionSet, Integer> {

  public Set<QuestionSet> findByCohortId(Integer cohortId);
  public QuestionSet findById(int id);

  @Query("Select a FROM QuestionSet a WHERE a.id = ?1")
  public QuestionSet findByQuestionSetId(int questionId);

  @Query("Select distinct new org.ohdsi.webapi.annotation.set.QuestionSampleDto(q.id,c.id,q.name,c.name) FROM QuestionSet q LEFT JOIN Annotation a ON a.questionSet.id = q.id LEFT JOIN CohortSample c ON c.id = a.cohortSampleId WHERE q.cohortId = ?1")
  public List<QuestionSampleDto> findSamplesAndSetsByCohortId(int cohortId);
}
