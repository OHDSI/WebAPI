package org.ohdsi.webapi.executionengine.repository;

import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InputFileRepository
        extends CrudRepository<AnalysisFile, Long> {

}
