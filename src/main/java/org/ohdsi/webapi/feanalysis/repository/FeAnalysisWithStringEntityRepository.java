package org.ohdsi.webapi.feanalysis.repository;

import java.util.Collection;
import java.util.List;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;

public interface FeAnalysisWithStringEntityRepository extends BaseFeAnalysisEntityRepository<FeAnalysisWithStringEntity> {
    List<FeAnalysisWithStringEntity> findByDesignIn(Collection<String> names);
}
