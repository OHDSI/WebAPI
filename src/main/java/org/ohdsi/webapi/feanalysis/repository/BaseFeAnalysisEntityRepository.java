package org.ohdsi.webapi.feanalysis.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import java.util.Set;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseFeAnalysisEntityRepository<T extends FeAnalysisEntity> extends EntityGraphJpaRepository<T, Long> {
    Set<T> findAllByCohortCharacterizations(CohortCharacterizationEntity cohortCharacterization);
}
