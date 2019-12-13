package org.ohdsi.webapi.feanalysis.repository;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;

import java.util.Collection;
import java.util.List;

public interface FeAnalysisWithStringEntityRepository extends BaseFeAnalysisEntityRepository<FeAnalysisWithStringEntity> {
    List<FeAnalysisWithStringEntity> findByDesignIn(Collection<String> names);

    List<FeAnalysisWithStringEntity> findByDesign(String design);
}
