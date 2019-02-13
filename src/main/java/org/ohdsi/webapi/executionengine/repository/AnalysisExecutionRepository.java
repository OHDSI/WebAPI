package org.ohdsi.webapi.executionengine.repository;

import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface AnalysisExecutionRepository extends JpaRepository<AnalysisExecution, Integer> {

    Optional<AnalysisExecution> findByJobExecutionId(Long jobExecutionId);
    List<AnalysisExecution> findByExecutedBeforeAndExecutionStatusIn(Date invalidate, List<AnalysisExecution.Status> statuses);
}
