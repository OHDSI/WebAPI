package org.ohdsi.webapi.annotation.annotation;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Set;
import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnnotationRepository extends JpaRepository<Annotation, Long> {

  public Set<Annotation> findOneByCohortSampleIdAndSubjectIdAndSetId(Long cohortSampleId, Long subjectId, Long setId);
  public List<Annotation> findByCohortSampleIdAndSetId(Long cohortSampleId, Long setId);

  @Query("Select a FROM Annotation a WHERE a.id = ?1")
  public Annotation findByAnnotationId(int annotation_id);
//  @Query(value="SELECT * FROM ohdsi.annotation",nativeQuery = true)
//  public List<Annotation> findAllAnnotations();
}
