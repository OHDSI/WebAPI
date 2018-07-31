package org.ohdsi.webapi.cohortcharacterization;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CcService {
    CohortCharacterizationEntity createCc(CohortCharacterizationEntity entity);

    CohortCharacterizationEntity updateCc(CohortCharacterizationEntity entity);

    CohortCharacterizationEntity importCc(CohortCharacterizationEntity entity);

    String serializeCc(Long id);

    String serializeCc(CohortCharacterizationEntity cohortCharacterizationEntity);

    CohortCharacterizationEntity findById(Long id);

    CohortCharacterizationEntity findByIdWithLinkedEntities(Long id);
    
    Page<CohortCharacterizationEntity> getPageWithLinkedEntities(Pageable pageable);

    Page<CohortCharacterizationEntity> getPage(Pageable pageable);
}
