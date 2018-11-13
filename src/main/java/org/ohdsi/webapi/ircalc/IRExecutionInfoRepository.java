package org.ohdsi.webapi.ircalc;

import org.ohdsi.webapi.GenerationStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface IRExecutionInfoRepository extends CrudRepository<ExecutionInfo, ExecutionInfoId> {
    List<ExecutionInfo> findByStatus(GenerationStatus status);
    List<ExecutionInfo> findByStatusIn(List<GenerationStatus> statuses);
}
