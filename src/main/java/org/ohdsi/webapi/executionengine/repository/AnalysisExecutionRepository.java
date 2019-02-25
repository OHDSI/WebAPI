package org.ohdsi.webapi.executionengine.repository;

import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalysisExecutionRepository extends JpaRepository<ExecutionEngineAnalysisStatus, Integer> {

    Optional<ExecutionEngineAnalysisStatus> findByJobExecutionId(Long jobExecutionId);
    List<ExecutionEngineAnalysisStatus> findByExecutionStatusIn(List<ExecutionEngineAnalysisStatus.Status> statuses);
}
