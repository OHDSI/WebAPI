package org.ohdsi.webapi.executionengine.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecutionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

public interface AnalysisExecutionRepository extends JpaRepository<AnalysisExecution, Integer> {

    Optional<AnalysisExecution> findByJobExecutionId(Long jobExecutionId);
    Iterable<AnalysisExecution> findByAnalysisIdAndAnalysisType(Integer analysisId, AnalysisExecutionType analysisType);
    List<AnalysisExecution> findByExecutedBeforeAndExecutionStatusIn(Date invalidate, List<AnalysisExecution.Status> statuses);
}
