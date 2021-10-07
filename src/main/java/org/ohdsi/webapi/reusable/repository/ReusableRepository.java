package org.ohdsi.webapi.reusable.repository;

import org.ohdsi.webapi.reusable.domain.Reusable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReusableRepository extends JpaRepository<Reusable, Integer> {

    Optional<Reusable> findByName(String name);
}
