package org.ohdsi.webapi.annotation.annotation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Set;
import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnotationRepository extends JpaRepository<Annotation, Integer> {

  public Set<Annotation> findOneByCohortSampleIdAndSubjectIdAndQuestionSetId(int cohortSampleId, int subjectId, int questionSetId);
  public List<Annotation> findByCohortSampleIdAndQuestionSetId(int cohortSampleId, int questionSetId);
  public List<Annotation> findByCohortSampleId(int cohortSampleId);
  public List<Annotation> findByQuestionSetId(int questionSetId);
  public Annotation findById(int annotation_id);
}
