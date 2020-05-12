package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.cohortcharacterization.dto.ExecutionResultRequest;
import org.ohdsi.webapi.cohortcharacterization.dto.ExportExecutionResultRequest;
import org.ohdsi.webapi.cohortcharacterization.dto.GenerationResults;
import org.ohdsi.webapi.cohortdefinition.event.CohortDefinitionChangedEvent;
import org.ohdsi.webapi.feanalysis.event.FeAnalysisChangedEvent;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.OutputStream;
import java.util.List;

public interface CcService {
    CohortCharacterizationEntity createCc(CohortCharacterizationEntity entity);

    CohortCharacterizationEntity updateCc(CohortCharacterizationEntity entity);

    int getCountCcWithSameName(Long id, String name);

    void deleteCc(Long ccId);

    CohortCharacterizationEntity importCc(CohortCharacterizationEntity entity);

    String getNameForCopy(String dtoName);

    String getNameWithSuffix(String dtoName);

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

    GenerationResults findResult(Long generationId, ExecutionResultRequest params);
    
    List<CcResult> findResultAsList(Long generationId, float thresholdLevel);

    List<CcPrevalenceStat> getPrevalenceStatsByGenerationId(final Long id, Long analysisId, final Long cohortId, final Long covariateId);

    void hydrateAnalysis(Long analysisId, String packageName, OutputStream out);

    void deleteCcGeneration(Long generationId);

    void cancelGeneration(Long id, String sourceKey);

    Long getCCResultsTotalCount(Long id);

    GenerationResults exportExecutionResult(Long generationId, ExportExecutionResultRequest params);

    GenerationResults findData(final Long generationId, ExecutionResultRequest params);

    @EventListener
    void onCohortDefinitionChanged(CohortDefinitionChangedEvent event);

    @EventListener
    void onFeAnalysisChanged(FeAnalysisChangedEvent event);
}
