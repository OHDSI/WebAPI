package org.ohdsi.webapi.estimation.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.estimation.Estimation;

public interface EstimationRepository extends EntityGraphJpaRepository<Estimation, Integer> {
    
}
