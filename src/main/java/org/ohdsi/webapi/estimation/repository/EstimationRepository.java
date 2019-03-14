package org.ohdsi.webapi.estimation.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.estimation.Estimation;

import java.util.Optional;

public interface EstimationRepository extends EntityGraphJpaRepository<Estimation, Integer> {
    int countByNameStartsWith(String pattern);
    Optional<Estimation> findByName(String name);
}
