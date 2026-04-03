package org.ohdsi.webapi.ircalc;

import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.common.generation.GenerateSqlResult;
import org.ohdsi.webapi.ircalc.AnalysisReport;
import org.ohdsi.webapi.ircalc.dto.IRAnalysisDTO;
import org.ohdsi.webapi.ircalc.dto.IRAnalysisShortDTO;
import org.ohdsi.webapi.ircalc.dto.IRVersionFullDTO;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.tag.domain.HasTags;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RequestMapping(path = "/ir")
public interface IRAnalysisResource extends HasTags<Integer> {

    /**
     * Returns all IR Analysis in a list.
     *
     * @return List of IncidenceRateAnalysis
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    List<IRAnalysisShortDTO> getIRAnalysisList();

    @GetMapping(path = "/{id}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    int getCountIRWithSameName(@PathVariable("id") final int id, @RequestParam(value = "name", required = false) String name);

    /**
     * Creates the incidence rate analysis
     *
     * @param analysis The analysis to create.
     * @return The new FeasibilityStudy
     */
    @PostMapping(path = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    IRAnalysisDTO createAnalysis(@RequestBody IRAnalysisDTO analysis);

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    IRAnalysisDTO getAnalysis(@PathVariable("id") final int id);

    @PostMapping(path = "/design", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    IRAnalysisDTO doImport(@RequestBody final IRAnalysisDTO dto);

    @GetMapping(path = "/{id}/design", produces = MediaType.APPLICATION_JSON_VALUE)
    IRAnalysisDTO export(@PathVariable("id") final Integer id);

    @PutMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    IRAnalysisDTO saveAnalysis(@PathVariable("id") final int id, @RequestBody IRAnalysisDTO analysis);

    @GetMapping(path = "/{analysis_id}/execute/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    JobExecutionResource performAnalysis(@PathVariable("analysis_id") final int analysisId, @PathVariable("sourceKey") final String sourceKey);

    @DeleteMapping(path = "/{analysis_id}/execute/{sourceKey}")
    void cancelAnalysis(@PathVariable("analysis_id") final int analysisId, @PathVariable("sourceKey") final String sourceKey);

    @GetMapping(path = "/{id}/info", produces = MediaType.APPLICATION_JSON_VALUE)
    List<AnalysisInfoDTO> getAnalysisInfo(@PathVariable("id") final int id);

    @GetMapping(path = "/{id}/info/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    AnalysisInfoDTO getAnalysisInfo(@PathVariable("id") final int id, @PathVariable("sourceKey") final String sourceKey);

    /**
     * Deletes the specified cohort definition
     *
     * @param id - the Cohort Definition ID to copy
     */
    @DeleteMapping(path = "/{id}/info/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    void deleteInfo(@PathVariable("id") final int id, @PathVariable("sourceKey") final String sourceKey);

    /**
     * Deletes the specified cohort definition
     *
     * @param id - the Cohort Definition ID to copy
     */
    @DeleteMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    void delete(@PathVariable("id") final int id);

    /**
     * Exports the analysis definition and results
     *
     * @param id - the IR Analysis ID to export
     * @return Response containing binary stream of zipped data
     */
    @GetMapping(path = "/{id}/export")
    ResponseEntity<byte[]> export(@PathVariable("id") final int id);

    /**
     * Copies the specified cohort definition
     *
     * @param id - the Cohort Definition ID to copy
     * @return the copied cohort definition as a CohortDefinitionDTO
     */
    @GetMapping(path = "/{id}/copy", produces = MediaType.APPLICATION_JSON_VALUE)
    IRAnalysisDTO copy(@PathVariable("id") final int id);

    @GetMapping(path = "/{id}/report/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    AnalysisReport getAnalysisReport(@PathVariable("id") final int id, @PathVariable("sourceKey") final String sourceKey,
                                     @RequestParam("targetId") final int targetId, @RequestParam("outcomeId") final int outcomeId );

    @PostMapping(path = "/sql", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public GenerateSqlResult generateSql(@RequestBody IRAnalysisService.GenerateSqlRequest request);

    @PostMapping(path = "/check", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public CheckResult runDiagnostics(@RequestBody IRAnalysisDTO irAnalysisDTO);

    /**
     * Assign tag to IR Analysis
     *
     * @param id
     * @param tagId
     */
    @PostMapping(path = "/{id}/tag/", produces = MediaType.APPLICATION_JSON_VALUE)
    void assignTag(@PathVariable("id") final Integer id, @RequestParam("tagId") final int tagId);

    /**
     * Unassign tag from IR Analysis
     *
     * @param id
     * @param tagId
     */
    @DeleteMapping(path = "/{id}/tag/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
    void unassignTag(@PathVariable("id") final Integer id, @PathVariable("tagId") final int tagId);

    /**
     * Assign protected tag to IR Analysis
     *
     * @param id
     * @param tagId
     */
    @PostMapping(path = "/{id}/protectedtag/", produces = MediaType.APPLICATION_JSON_VALUE)
    void assignPermissionProtectedTag(@PathVariable("id") final int id, @RequestParam("tagId") final int tagId);

    /**
     * Unassign protected tag from IR Analysis
     *
     * @param id
     * @param tagId
     */
    @DeleteMapping(path = "/{id}/protectedtag/{tagId}", produces = MediaType.APPLICATION_JSON_VALUE)
    void unassignPermissionProtectedTag(@PathVariable("id") final int id, @PathVariable("tagId") final int tagId);

    /**
     * Get list of versions of IR Analysis
     *
     * @param id
     * @return
     */
    @GetMapping(path = "/{id}/version/", produces = MediaType.APPLICATION_JSON_VALUE)
    List<VersionDTO> getVersions(@PathVariable("id") final long id);

    /**
     * Get version of IR Analysis
     *
     * @param id
     * @param version
     * @return
     */
    @GetMapping(path = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    IRVersionFullDTO getVersion(@PathVariable("id") final int id, @PathVariable("version") final int version);

    /**
     * Update version of IR Analysis
     *
     * @param id
     * @param version
     * @param updateDTO
     * @return
     */
    @PutMapping(path = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    VersionDTO updateVersion(@PathVariable("id") final int id, @PathVariable("version") final int version,
                                    @RequestBody VersionUpdateDTO updateDTO);

    /**
     * Delete version of IR Analysis
     *
     * @param id
     * @param version
     */
    @DeleteMapping(path = "/{id}/version/{version}", produces = MediaType.APPLICATION_JSON_VALUE)
    void deleteVersion(@PathVariable("id") final int id, @PathVariable("version") final int version) ;

    /**
     * Create a new asset form version of IR Analysis
     *
     * @param id
     * @param version
     * @return
     */
    @PutMapping(path = "/{id}/version/{version}/createAsset", produces = MediaType.APPLICATION_JSON_VALUE)
    IRAnalysisDTO copyAssetFromVersion(@PathVariable("id") final int id, @PathVariable("version") final int version);

    /**
     * Get list of incidence rates with assigned tags
     *
     * @param requestDTO
     * @return
     */
    @PostMapping(path = "/byTags", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    List<IRAnalysisDTO> listByTags(@RequestBody TagNameListRequestDTO requestDTO);
}
