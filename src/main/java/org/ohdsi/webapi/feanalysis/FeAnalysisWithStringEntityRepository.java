package org.ohdsi.webapi.feanalysis;

import java.util.Collection;
import java.util.List;

public interface FeAnalysisWithStringEntityRepository extends BaseFeAnalysisEntityRepository<FeAnalysisWithStringEntity> {
    List<FeAnalysisWithStringEntity> findByDesignIn(Collection<String> names);
}
