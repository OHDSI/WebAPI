package org.ohdsi.webapi.executionengine.repository;

import java.util.Date;
import java.util.List;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecution;
import org.ohdsi.webapi.executionengine.entity.AnalysisExecutionType;
import org.springframework.data.repository.CrudRepository;

public interface AnalysisExecutionRepository extends CrudRepository<AnalysisExecution, Integer> {

    Iterable<AnalysisExecution> findByAnalysisIdAndAnalysisType(Integer analysisId, AnalysisExecutionType analysisType);
    List<AnalysisExecution> findByExecutedBeforeAndExecutionStatusIn(Date invalidate, List<AnalysisExecution.Status> statuses);
}
