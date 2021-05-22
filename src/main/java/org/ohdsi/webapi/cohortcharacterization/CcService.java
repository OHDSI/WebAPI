package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.cohortcharacterization.domain.CcGenerationEntity;
import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;
import org.ohdsi.webapi.cohortcharacterization.dto.CcPrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.dto.CcResult;
import org.ohdsi.webapi.cohortcharacterization.dto.CcVersionFullDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.ExecutionResultRequest;
import org.ohdsi.webapi.cohortcharacterization.dto.ExportExecutionResultRequest;
import org.ohdsi.webapi.cohortcharacterization.dto.GenerationResults;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortVersionFullDTO;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.cohortdefinition.event.CohortDefinitionChangedEvent;
import org.ohdsi.webapi.feanalysis.event.FeAnalysisChangedEvent;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.versioning.domain.CharacterizationVersion;
import org.ohdsi.webapi.versioning.domain.CohortVersion;
import org.ohdsi.webapi.versioning.domain.Version;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.event.TransactionalEventListener;

import javax.transaction.Transactional;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    List<ConceptSetExport> exportConceptSets(CohortCharacterization cohortCharacterization);

    GenerationResults exportExecutionResult(Long generationId, ExportExecutionResultRequest params);

    GenerationResults findData(final Long generationId, ExecutionResultRequest params);

    @EventListener
    void onCohortDefinitionChanged(CohortDefinitionChangedEvent event);

    @EventListener
    void onFeAnalysisChanged(FeAnalysisChangedEvent event);

    void assignTag(long id, int tagId, boolean isPermissionProtected);

    void unassignTag(long id, int tagId, boolean isPermissionProtected);

    List<VersionDTO> getVersions(long id);

    CcVersionFullDTO getVersion(long id, int version);

    VersionDTO updateVersion(long id, int version, VersionUpdateDTO updateDTO);

    void deleteVersion(long id, int version);

    CohortCharacterizationDTO copyAssetFromVersion(long id, int version);

    CharacterizationVersion saveVersion(long id);
}
