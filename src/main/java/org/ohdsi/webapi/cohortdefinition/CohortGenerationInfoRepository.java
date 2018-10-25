package org.ohdsi.webapi.cohortdefinition;

import org.ohdsi.webapi.GenerationStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortGenerationInfoRepository extends CrudRepository<CohortGenerationInfo, CohortGenerationInfoId> {

    List<CohortGenerationInfo> findByStatus(GenerationStatus status);
    List<CohortGenerationInfo> findByStatusIn(List<GenerationStatus> statuses);

    @Query("select cgi from CohortGenerationInfo cgi where cgi.id.cohortDefinitionId=?1 and cgi.id.sourceId=?2")
    CohortGenerationInfo findGenerationInfoByIdAndSourceId(Integer id, Integer sourceId);
}
