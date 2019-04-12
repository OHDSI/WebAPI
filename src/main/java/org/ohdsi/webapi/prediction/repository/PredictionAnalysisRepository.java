package org.ohdsi.webapi.prediction.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PredictionAnalysisRepository extends EntityGraphJpaRepository<PredictionAnalysis, Integer> {
    int countByNameStartsWith(String pattern);
    Optional<PredictionAnalysis> findByName(String name);
    @Query("SELECT COUNT(pa) FROM PredictionAnalysis pa WHERE pa.name = :name and pa.id <> :id")
    int getCountPredictionWithSameName(@Param("id") Integer id, @Param("name") String name);
}