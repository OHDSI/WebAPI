package org.ohdsi.webapi.estimation.repository;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphJpaRepository;
import org.ohdsi.webapi.estimation.Estimation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EstimationRepository extends EntityGraphJpaRepository<Estimation, Integer> {
    int countByNameStartsWith(String pattern);
    Optional<Estimation> findByName(String name);
    @Query("SELECT es FROM Estimation es LEFT JOIN FETCH es.createdBy LEFT JOIN FETCH es.modifiedBy WHERE es.name = :name and es.id <> :id")
    List<Estimation> estimationExists(@Param("id") Integer id, @Param("name") String name);
}
