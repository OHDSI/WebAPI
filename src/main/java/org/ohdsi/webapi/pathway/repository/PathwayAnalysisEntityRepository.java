package org.ohdsi.webapi.pathway.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PathwayAnalysisEntityRepository extends EntityGraphJpaRepository<PathwayAnalysisEntity, Integer> {

  int countByNameStartsWith(String pattern);

  Optional<PathwayAnalysisEntity> findByName(String name);

  @Query("SELECT pa FROM pathway_analysis pa LEFT JOIN FETCH pa.createdBy LEFT JOIN FETCH pa.modifiedBy " +
          "LEFT JOIN FETCH pa.targetCohorts LEFT JOIN FETCH pa.eventCohorts WHERE pa.name = :name and pa.id <> :id")
  List<PathwayAnalysisEntity> pathwayAnalysisExists(@Param("id") Integer id, @Param("name") String name);
}
