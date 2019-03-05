package org.ohdsi.webapi.ircalc;

import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.source.Source;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface IRExecutionInfoRepository extends CrudRepository<ExecutionInfo, ExecutionInfoId> {
    List<ExecutionInfo> findByStatus(GenerationStatus status);
    List<ExecutionInfo> findByStatusIn(List<GenerationStatus> statuses);
    Optional<ExecutionInfo> findByAnalysisAndSource(IncidenceRateAnalysis analysis, Source source);
}
