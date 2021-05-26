package org.ohdsi.webapi.pathway;

import org.ohdsi.webapi.ircalc.IncidenceRateAnalysis;
import org.ohdsi.webapi.ircalc.dto.IRVersionFullDTO;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisEntity;
import org.ohdsi.webapi.pathway.domain.PathwayAnalysisGenerationEntity;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayVersionFullDTO;
import org.ohdsi.webapi.pathway.dto.internal.PathwayAnalysisResult;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.ohdsi.webapi.shiro.annotations.PathwayAnalysisGenerationId;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.versioning.domain.IRVersion;
import org.ohdsi.webapi.versioning.domain.PathwayVersion;
import org.ohdsi.webapi.versioning.domain.VersionBase;
import org.ohdsi.webapi.versioning.domain.VersionType;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface PathwayService {

    PathwayAnalysisEntity create(PathwayAnalysisEntity pathwayAnalysisEntity);

    PathwayAnalysisEntity importAnalysis(PathwayAnalysisEntity toImport);

    String getNameForCopy(String dtoName);
    
    String getNameWithSuffix(String dtoName);

    Page<PathwayAnalysisEntity> getPage(final Pageable pageable);

    int getCountPAWithSameName(Integer id, String name);

    PathwayAnalysisEntity getById(Integer id);

    PathwayAnalysisEntity update(PathwayAnalysisEntity pathwayAnalysisEntity);

    void delete(Integer id);

    Map<Integer, Integer> getEventCohortCodes(PathwayAnalysisEntity pathwayAnalysis);

    String buildAnalysisSql(Long generationId, PathwayAnalysisEntity pathwayAnalysis, Integer sourceId);

    String buildAnalysisSql(Long generationId, PathwayAnalysisEntity pathwayAnalysis, Integer sourceId, String cohortTable, String sessionId);

    JobExecutionResource generatePathways(final Integer pathwayAnalysisId, final Integer sourceId);

    List<PathwayAnalysisGenerationEntity> getPathwayGenerations(final Integer pathwayAnalysisId);

    PathwayAnalysisGenerationEntity getGeneration(Long generationId);

    PathwayAnalysisResult getResultingPathways(final Long generationId);

    void cancelGeneration(Integer pathwayAnalysisId, Integer sourceId);

    String findDesignByGenerationId(@PathwayAnalysisGenerationId final Long id);

    void assignTag(int id, int tagId, boolean isPermissionProtected);

    void unassignTag(int id, int tagId, boolean isPermissionProtected);

    List<VersionDTO> getVersions(long id);

    PathwayVersionFullDTO getVersion(int id, int version);

    VersionDTO updateVersion(int id, int version, VersionUpdateDTO updateDTO);

    void deleteVersion(int id, int version);

    PathwayAnalysisDTO copyAssetFromVersion(int id, int version);

    PathwayVersion saveVersion(int id);
}
