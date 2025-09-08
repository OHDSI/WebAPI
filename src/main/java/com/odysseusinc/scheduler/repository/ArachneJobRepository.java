package com.odysseusinc.scheduler.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import com.odysseusinc.scheduler.model.ArachneJob;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ArachneJobRepository<T extends ArachneJob> extends EntityGraphJpaRepository<T, Long> {

    List<T> findAllByEnabledTrueAndIsClosedFalse(EntityGraph entityGraph);

    List<T> findAllByEnabledTrueAndIsClosedFalse();
}
