package org.ohdsi.webapi.annotation.set;

import org.ohdsi.webapi.annotation.question.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;
import org.ohdsi.webapi.annotation.set.QuestionSet;
import org.springframework.data.jpa.repository.Query;


public interface QuestionSetRepository extends JpaRepository<QuestionSet, Integer> {

  public Set<QuestionSet> findByCohortId(Integer cohortId);
  public QuestionSet findById(int id);

  @Query("Select a FROM QuestionSet a WHERE a.id = ?1")
  public QuestionSet findByQuestionSetId(int questionId);
}
