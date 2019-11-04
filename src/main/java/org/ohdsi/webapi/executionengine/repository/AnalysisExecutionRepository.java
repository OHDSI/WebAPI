package org.ohdsi.webapi.executionengine.repository;

import java.util.Date;
import java.util.List;
import org.ohdsi.webapi.executionengine.entity.ExecutionEngineAnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AnalysisExecutionRepository extends JpaRepository<ExecutionEngineAnalysisStatus, Integer> {

    @Query(" SELECT st FROM ExecutionEngineAnalysisStatus st JOIN st.executionEngineGeneration ge " +
            " WHERE st.executionStatus in(:statuses) " +
            " AND   ge.startTime < :invalidate ")
    List<ExecutionEngineAnalysisStatus> findAllInvalidAnalysis(
            @Param("invalidate") Date invalidate,
            @Param("statuses") List<ExecutionEngineAnalysisStatus.Status> statuses
    );

}
