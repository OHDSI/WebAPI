package org.ohdsi.webapi.cohortdefinition;

import org.ohdsi.webapi.GenerationStatus;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CohortGenerationInfoRepository extends CrudRepository<CohortGenerationInfo, CohortGenerationInfoId> {

    List<CohortGenerationInfo> findByStatus(GenerationStatus status);
    List<CohortGenerationInfo> findByStatusIn(List<GenerationStatus> statuses);
}
