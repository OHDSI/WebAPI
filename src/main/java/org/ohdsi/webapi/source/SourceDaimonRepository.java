package org.ohdsi.webapi.source;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SourceDaimonRepository extends CrudRepository<SourceDaimon, Integer> {

    List<SourceDaimon> findByDaimonType(SourceDaimon.DaimonType daimonType);
}
