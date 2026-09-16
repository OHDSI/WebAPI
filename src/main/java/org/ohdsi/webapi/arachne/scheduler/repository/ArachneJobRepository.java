package org.ohdsi.webapi.arachne.scheduler.repository;

import org.ohdsi.webapi.arachne.scheduler.model.ArachneJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface ArachneJobRepository<T extends ArachneJob> extends JpaRepository<T, Long> {

    List<T> findAllByEnabledTrueAndIsClosedFalse();
}
