package org.ohdsi.webapi.annotation.result;

import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.cohortsample.CohortSample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.ohdsi.webapi.annotation.result.Result;

import java.util.List;
import java.util.Set;

import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;

@Transactional
public interface ResultRepository extends JpaRepository<Result, Long> {
//    public Set<Result> findBySampleNameAndSubjectId(String sampleName, Long subject_id);

    @Query("select s.value from Result s where s.annotation=?1 and s.questionId = ?2")
    public Set<Result> getAnswers(Annotation annotation, Long questionID);

    @Query("select r FROM Result r WHERE r.annotation.id = ?1")
    List<Result> findByAnnotationId(int annotationId);
}
