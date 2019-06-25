package org.ohdsi.webapi.executionengine.repository;

import org.ohdsi.webapi.executionengine.entity.ExecutionEngineGenerationEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExecutionEngineGenerationRepository
        extends CrudRepository<ExecutionEngineGenerationEntity, Long> {

    Optional<ExecutionEngineGenerationEntity> findById(Long id);
}
