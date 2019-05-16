package org.ohdsi.webapi.cohortcharacterization.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;

import java.util.List;
import java.util.Optional;

public interface CcGenerationEntityRepository extends EntityGraphJpaRepository<CcGenerationEntity, Long> {
    List<CcGenerationEntity> findByCohortCharacterizationIdOrderByIdDesc(Long id, EntityGraph entityGraph);
    List<CcGenerationEntity> findByCohortCharacterizationIdAndSourceSourceKeyOrderByIdDesc(Long id, String sourceKey, EntityGraph source);
    List<CcGenerationEntity> findByStatusIn(List<String> statuses);
    Optional<CcGenerationEntity> findById(Long generationId);
    CcGenerationEntity findById(Long generationId, EntityGraph source);
}
