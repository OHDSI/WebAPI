package org.ohdsi.webapi.annotation.study;

import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface StudyRepository extends JpaRepository<Study, Integer> {

    @Query("Select s FROM Study s WHERE s.id = ?1")
    public Study findByStudyId(int study_id);

    @Query("Select s FROM Study s WHERE s.questionSet.id = ?1 AND s.cohortSample.id = ?2")
    public Study findByQuestionSetIdAndSampleId(int questionSetId,int sampleId);
}