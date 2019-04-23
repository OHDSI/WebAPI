package org.ohdsi.webapi.feanalysis.repository;

import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysisType;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface FeAnalysisEntityRepository extends BaseFeAnalysisEntityRepository<FeAnalysisEntity> {

    List<FeAnalysisEntity> findAllByTypeAndRawDesignIn(FeatureAnalysisType type, List<String> designList);
    
    @Query("SELECT COUNT(fe) FROM FeAnalysisEntity fe WHERE fe.name = :name and fe.id <> :id")
    int getCountFeWithSameName(@Param("id") Integer id, @Param("name") String name);
}
