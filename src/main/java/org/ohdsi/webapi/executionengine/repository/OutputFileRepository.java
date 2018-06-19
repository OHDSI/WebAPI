package org.ohdsi.webapi.executionengine.repository;

import java.util.List;
import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutputFileRepository
        extends CrudRepository<AnalysisResultFile, Long> {

    List<AnalysisResultFile> findByExecutionId(Integer executionId);
}
