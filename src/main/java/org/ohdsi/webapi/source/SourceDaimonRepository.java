package org.ohdsi.webapi.source;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface SourceDaimonRepository extends CrudRepository<SourceDaimon, Integer> {

    @Query("select sd from SourceDaimon sd join sd.source s where s.deletedDate is null and sd.daimonType = :daimonType and sd.priority >= 0")
    List<SourceDaimon> findByDaimonType(@Param("daimonType") SourceDaimon.DaimonType daimonType);

    List<SourceDaimon> findBySource(Source source);
}
