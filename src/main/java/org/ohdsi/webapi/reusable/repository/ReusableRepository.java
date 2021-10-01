package org.ohdsi.webapi.reusable.repository;

import org.ohdsi.webapi.reusable.domain.Reusable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReusableRepository extends JpaRepository<Reusable, Integer> {
}
