package org.ohdsi.webapi.annotation.set;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Set;
import org.ohdsi.webapi.annotation.set.QuestionSet;


public interface QuestionSetRepository extends JpaRepository<QuestionSet, Long> {

  public Set<QuestionSet> findByCohortId(Integer cohortId);
  public QuestionSet findById(Long id);

}
