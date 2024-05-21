package org.ohdsi.webapi.pathway.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PathwayAnalysisEntityRepository extends EntityGraphJpaRepository<PathwayAnalysisEntity, Integer> {
  @Query("SELECT pa FROM pathway_analysis pa WHERE pa.name LIKE ?1 ESCAPE '\\'")
  List<PathwayAnalysisEntity> findAllByNameStartsWith(String pattern);

  Optional<PathwayAnalysisEntity> findByName(String name);

  @Query("SELECT COUNT(pa) FROM pathway_analysis pa WHERE pa.name = :name and pa.id <> :id")
  int getCountPAWithSameName(@Param("id") Integer id, @Param("name") String name);

  @Query("SELECT DISTINCT pa FROM pathway_analysis pa JOIN FETCH pa.tags t WHERE lower(t.name) in :tagNames")
  List<PathwayAnalysisEntity> findByTags(@Param("tagNames") List<String> tagNames);
}
