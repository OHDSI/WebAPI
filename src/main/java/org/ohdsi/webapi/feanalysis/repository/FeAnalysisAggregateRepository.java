package org.ohdsi.webapi.feanalysis.repository;

import org.ohdsi.analysis.cohortcharacterization.design.StandardFeatureAnalysisDomain;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisAggregateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FeAnalysisAggregateRepository extends JpaRepository<FeAnalysisAggregateEntity, Integer> {

  List<FeAnalysisAggregateEntity> findByDomain(StandardFeatureAnalysisDomain domain);
}
