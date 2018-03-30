package org.ohdsi.webapi.source;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface SourceDaimonRepository extends CrudRepository<SourceDaimon, Integer> {

    List<SourceDaimon> findByDaimonType(SourceDaimon.DaimonType daimonType);
}
