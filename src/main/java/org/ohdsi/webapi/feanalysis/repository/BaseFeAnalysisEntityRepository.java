package org.ohdsi.webapi.feanalysis.repository;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisType;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface BaseFeAnalysisEntityRepository<T extends FeAnalysisEntity> extends EntityGraphJpaRepository<T, Integer> {
    Set<T> findAllByCohortCharacterizations(CohortCharacterizationEntity cohortCharacterization);
    List<T> findAllByType(StandardFeatureAnalysisType preset);
    Optional<T> findById(Integer id);
    Optional<T> findById(Integer id, EntityGraph entityGraph);
    Optional<T> findByName(String name);
}
