package org.ohdsi.webapi.executionengine.repository;

import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnalysisExecutionRepository extends JpaRepository<ExecutionEngineAnalysisStatus, Integer> {

    List<ExecutionEngineAnalysisStatus> findByExecutionStatusIn(List<ExecutionEngineAnalysisStatus.Status> statuses);
}
