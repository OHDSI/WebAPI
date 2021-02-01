package org.ohdsi.webapi.annotation.result;

import org.springframework.data.jpa.repository.JpaRepository;
import org.ohdsi.webapi.annotation.result.Result;
import java.util.Set;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;

@Transactional
public interface ResultRepository extends JpaRepository<Result, Long> {
    public Set<Result> findBySampleNameAndSubjectId(String sampleName, Long subject_id);

    @Query("select s.value from Result s where s.sampleName=?1 and s.questionId = ?2 and s.subjectId=?3")
    public Set<Result> getAnswers(String name, Long cohortID, Long subjectID);
}
