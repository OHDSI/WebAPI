package org.ohdsi.webapi.reusable.repository;

import org.ohdsi.webapi.reusable.domain.Reusable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReusableRepository extends JpaRepository<Reusable, Integer> {
    Optional<Reusable> findByName(String name);

    @Query("SELECT r FROM Reusable r WHERE r.name LIKE ?1 ESCAPE '\\'")
    List<Reusable> findAllByNameStartsWith(String pattern);

    @Query("SELECT COUNT(r) FROM Reusable r WHERE r.name = :name and r.id <> :id")
    int existsCount(@Param("id") Integer id, @Param("name") String name);

    @Query("SELECT DISTINCT r FROM Reusable r JOIN FETCH r.tags t WHERE lower(t.name) in :tagNames")
    List<Reusable> findByTags(@Param("tagNames") List<String> tagNames);
}
