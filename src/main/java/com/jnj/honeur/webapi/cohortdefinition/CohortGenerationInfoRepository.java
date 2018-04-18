package com.jnj.honeur.webapi.cohortdefinition;

import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface CohortGenerationInfoRepository extends CrudRepository<CohortGenerationInfo, Long>{

    @Query("select cgi from CohortGenerationInfo cgi where cgi.id.cohortDefinitionId=?1")
    List<CohortGenerationInfo> listGenerationInfoById(Integer id);
}
