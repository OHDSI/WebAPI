package org.ohdsi.webapi.cohortcharacterization.repository;

import com.cosium.spring.data.jpa.entity.graph.domain2.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import java.util.List;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;

public interface CcGenerationEntityRepository extends EntityGraphJpaRepository<CcGenerationEntity, Long> {
    List<CcGenerationEntity> findByCohortCharacterizationIdOrderByIdDesc(Long id, EntityGraph entityGraph);
    List<CcGenerationEntity> findByCohortCharacterizationIdAndSourceSourceKeyOrderByIdDesc(Long id, String sourceKey, EntityGraph entityGraph);
    List<CcGenerationEntity> findByStatusIn(List<String> statuses);
}
