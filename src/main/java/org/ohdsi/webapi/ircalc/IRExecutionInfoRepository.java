package org.ohdsi.webapi.ircalc;

import org.ohdsi.webapi.GenerationStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IRExecutionInfoRepository extends CrudRepository<ExecutionInfo, ExecutionInfoId> {
    List<ExecutionInfo> findByStatus(GenerationStatus status);
    List<ExecutionInfo> findByStatusIn(List<GenerationStatus> statuses);
    @Query("SELECT ei FROM IRAnalysisGenerationInfo ei JOIN Source s ON s.id = ei.source.id AND s.deletedDate IS NULL WHERE ei.analysis.id = :analysisId")
    List<ExecutionInfo> findByAnalysisId(@Param("analysisId") Integer analysisId);
}
