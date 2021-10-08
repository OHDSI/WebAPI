package org.ohdsi.webapi.reusable.repository;

import org.ohdsi.webapi.reusable.domain.Reusable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReusableRepository extends JpaRepository<Reusable, Integer> {
    Optional<Reusable> findByName(String name);

    @Query("SELECT r FROM Reusable r WHERE r.name LIKE ?1 ESCAPE '\\'")
    List<Reusable> findAllByNameStartsWith(String pattern);
}
