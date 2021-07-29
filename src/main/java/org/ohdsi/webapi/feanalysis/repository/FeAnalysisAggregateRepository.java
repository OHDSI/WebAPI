package org.ohdsi.webapi.feanalysis.repository;

import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisAggregateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface FeAnalysisAggregateRepository extends JpaRepository<FeAnalysisAggregateEntity, Integer> {

  List<FeAnalysisAggregateEntity> findByDomain(StandardFeatureAnalysisDomain domain);
  @Query("select fa from FeAnalysisAggregateEntity fa where fa.isDefault = true")
  Optional<FeAnalysisAggregateEntity> findDefault();
}
