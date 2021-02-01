package org.ohdsi.webapi.annotation.annotation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Set;
import org.ohdsi.webapi.annotation.annotation.Annotation;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {

  public Set<Annotation> findOneByCohortIdAndSubjectIdAndSetId(Long cohortId, Long subjectId, Long setId);
  public List<Annotation> findByCohortIdAndSetId(Long cohortId, Long setId);
  
}
