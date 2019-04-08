package org.ohdsi.webapi.cohortcharacterization.repository;

import java.util.List;
import java.util.Optional;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CcRepository extends EntityGraphJpaRepository<CohortCharacterizationEntity, Long> {
    Optional<CohortCharacterizationEntity> findById(final Long id);
    int countByNameStartsWith(String pattern);
    Optional<CohortCharacterizationEntity> findByName(String name);
    
    @Query("SELECT cc FROM CohortCharacterizationEntity cc WHERE cc.name = :ccName and cc.id <> :ccId")
    List<CohortCharacterizationEntity> ccExists(@Param("ccId") Long ccId, @Param("ccName") String ccName);
}
