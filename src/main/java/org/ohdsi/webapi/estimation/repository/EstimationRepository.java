package org.ohdsi.webapi.estimation.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.estimation.Estimation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstimationRepository extends EntityGraphJpaRepository<Estimation, Integer> {
    @Query("SELECT es FROM Estimation es WHERE es.name LIKE ?1 ESCAPE '\\'")
    List<Estimation> findAllByNameStartsWith(String pattern);
    Optional<Estimation> findByName(String name);
    @Query("SELECT COUNT(es) FROM Estimation es WHERE es.name = :name and es.id <> :id")
    int getCountEstimationWithSameName(@Param("id") Integer id, @Param("name") String name);
}
