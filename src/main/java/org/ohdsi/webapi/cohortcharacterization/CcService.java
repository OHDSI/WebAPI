package org.ohdsi.webapi.cohortcharacterization;

import java.util.List;
import org.ohdsi.webapi.cohortcharacterization.dto.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
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

    String generateCc(Long id, final String sourceKey);

    List<CcGenerationEntity> findGenerationsByCcId(Long id);

    List<CcGenerationEntity> findGenerationsByCcIdAndSource(Long id, String sourceKey);

    List<CcResult> findResults(Long generationId);
}
