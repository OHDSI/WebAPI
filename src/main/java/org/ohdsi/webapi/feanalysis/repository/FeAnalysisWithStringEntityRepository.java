package org.ohdsi.webapi.feanalysis.repository;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisWithStringEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface FeAnalysisWithStringEntityRepository extends BaseFeAnalysisEntityRepository<FeAnalysisWithStringEntity> {
    List<FeAnalysisWithStringEntity> findByDesignIn(Collection<String> names);

    @Query("Select fe FROM FeAnalysisWithStringEntity fe WHERE fe.design = ?1")
    List<FeAnalysisWithStringEntity> findByDesign(String design);
}
