package org.ohdsi.webapi.estimation;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;

public interface EstimationRepository extends EntityGraphJpaRepository<Estimation, Integer> {
    
}
