package org.ohdsi.webapi.cohortcharacterization;

import java.util.List;

import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CcService {
    CohortCharacterizationEntity createCc(CohortCharacterizationEntity entity);

    CohortCharacterizationEntity updateCc(CohortCharacterizationEntity entity);
    
    void deleteCc(Long ccId);

    CohortCharacterizationEntity importCc(CohortCharacterizationEntity entity);

    String getNameForCopy(String dtoName);

    String serializeCc(Long id);

    String serializeCc(CohortCharacterizationEntity cohortCharacterizationEntity);

    CohortCharacterizationEntity findById(Long id);

    CohortCharacterizationEntity findByIdWithLinkedEntities(Long id);

    CohortCharacterization findDesignByGenerationId(final Long id);
    
    Page<CohortCharacterizationEntity> getPageWithLinkedEntities(Pageable pageable);

    Page<CohortCharacterizationEntity> getPage(Pageable pageable);

    JobExecutionResource generateCc(Long id, final String sourceKey);

    List<CcGenerationEntity> findGenerationsByCcId(Long id);

    CcGenerationEntity findGenerationById(final Long id);

    List<CcGenerationEntity> findGenerationsByCcIdAndSource(Long id, String sourceKey);

    List<CcResult> findResults(Long generationId, float thresholdLevel);

    List<CcPrevalenceStat> getPrevalenceStatsByGenerationId(final Long id, Long analysisId, final Long cohortId, final Long covariateId);

    void deleteCcGeneration(Long generationId);

    void cancelGeneration(Long id, String sourceKey);

    int countLikeName(String copyName);
}
