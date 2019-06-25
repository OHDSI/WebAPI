package org.ohdsi.webapi.executionengine.repository;

import org.ohdsi.webapi.executionengine.entity.AnalysisResultFile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutputFileRepository
        extends CrudRepository<AnalysisResultFile, Long> {

    List<AnalysisResultFile> findByExecutionId(Integer executionId);
}
