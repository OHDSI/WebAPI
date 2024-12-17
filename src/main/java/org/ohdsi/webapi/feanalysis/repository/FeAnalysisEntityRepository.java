package org.ohdsi.webapi.feanalysis.repository;

import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface FeAnalysisEntityRepository extends BaseFeAnalysisEntityRepository<FeAnalysisEntity> {
    @Query("Select fe FROM FeAnalysisEntity fe WHERE fe.name LIKE ?1 ESCAPE '\\'")
    List<FeAnalysisEntity> findAllByNameStartsWith(String pattern);

    @Query("SELECT COUNT(fe) FROM FeAnalysisEntity fe WHERE fe.name = :name and fe.id <> :id")
    int getCountFeWithSameName(@Param("id") Integer id, @Param("name") String name);
    
    @Query("SELECT fe FROM FeAnalysisEntity fe WHERE fe.id IN :ids")
    Set<FeAnalysisEntity> findByListIds(@Param("ids") List<Integer> ids);
}
