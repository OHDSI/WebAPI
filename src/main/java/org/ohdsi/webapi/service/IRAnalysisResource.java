package org.ohdsi.webapi.service;

import org.ohdsi.webapi.check.CheckResult;
import org.ohdsi.webapi.common.generation.GenerateSqlResult;
import org.ohdsi.webapi.ircalc.AnalysisReport;
import org.ohdsi.webapi.ircalc.dto.IRVersionFullDTO;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.dto.AnalysisInfoDTO;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.ohdsi.webapi.service.dto.IRAnalysisShortDTO;
import org.ohdsi.webapi.tag.domain.HasTags;
import org.ohdsi.webapi.tag.dto.TagNameListRequestDTO;
import org.ohdsi.webapi.versioning.dto.VersionDTO;
import org.ohdsi.webapi.versioning.dto.VersionUpdateDTO;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/ir/")
public interface IRAnalysisResource extends HasTags<Integer> {

    /**
     * Returns all IR Analysis in a list.
     *
     * @return List of IncidenceRateAnalysis
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    List<IRAnalysisShortDTO> getIRAnalysisList();

    @GET
    @Path("/{id}/exists")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    int getCountIRWithSameName(@PathParam("id") @DefaultValue("0") final int id, @QueryParam("name") String name);

    /**
     * Creates the incidence rate analysis
     *
     * @param analysis The analysis to create.
     * @return The new FeasibilityStudy
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    IRAnalysisDTO createAnalysis(IRAnalysisDTO analysis);

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    IRAnalysisDTO getAnalysis(@PathParam("id") final int id);

    @POST
    @Path("/design")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    IRAnalysisDTO doImport(final IRAnalysisDTO dto);

    @GET
    @Path("/{id}/design")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    IRAnalysisDTO export(@PathParam("id") final Integer id);

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    IRAnalysisDTO saveAnalysis(@PathParam("id") final int id, IRAnalysisDTO analysis);

    @GET
    @Path("/{analysis_id}/execute/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    JobExecutionResource performAnalysis(@PathParam("analysis_id") final int analysisId, @PathParam("sourceKey") final String sourceKey);

    @DELETE
    @Path("/{analysis_id}/execute/{sourceKey}")
    void cancelAnalysis(@PathParam("analysis_id") final int analysisId, @PathParam("sourceKey") final String sourceKey);

    @GET
    @Path("/{id}/info")
    @Produces(MediaType.APPLICATION_JSON)
    List<AnalysisInfoDTO> getAnalysisInfo(@PathParam("id") final int id);

    @GET
    @Path("/{id}/info/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    AnalysisInfoDTO getAnalysisInfo(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey);

    /**
     * Deletes the specified cohort definition
     *
     * @param id - the Cohort Definition ID to copy
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/info/{sourceKey}")
    void deleteInfo(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey);

    /**
     * Deletes the specified cohort definition
     *
     * @param id - the Cohort Definition ID to copy
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    void delete(@PathParam("id") final int id);

    /**
     * Exports the analysis definition and results
     *
     * @param id - the IR Analysis ID to export
     * @return Response containing binary stream of zipped data
     */
    @GET
    @Path("/{id}/export")
    Response export(@PathParam("id") final int id);

    /**
     * Copies the specified cohort definition
     *
     * @param id - the Cohort Definition ID to copy
     * @return the copied cohort definition as a CohortDefinitionDTO
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/copy")
    IRAnalysisDTO copy(@PathParam("id") final int id);

    @GET
    @Path("/{id}/report/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    AnalysisReport getAnalysisReport(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey,
                                     @QueryParam("targetId") final int targetId, @QueryParam("outcomeId") final int outcomeId );

    @Path("/sql")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public GenerateSqlResult generateSql(IRAnalysisService.GenerateSqlRequest request);

    @POST
    @Path("/check")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public CheckResult runDiagnostics(IRAnalysisDTO irAnalysisDTO);

    /**
     * Assign tag to IR Analysis
     *
     * @param id
     * @param tagId
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tag/")
    void assignTag(@PathParam("id") final Integer id, final int tagId);

    /**
     * Unassign tag from IR Analysis
     *
     * @param id
     * @param tagId
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/tag/{tagId}")
    void unassignTag(@PathParam("id") final Integer id, @PathParam("tagId") final int tagId);

    /**
     * Assign protected tag to IR Analysis
     *
     * @param id
     * @param tagId
     */
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/protectedtag/")
    void assignPermissionProtectedTag(@PathParam("id") final int id, final int tagId);

    /**
     * Unassign protected tag from IR Analysis
     *
     * @param id
     * @param tagId
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/protectedtag/{tagId}")
    void unassignPermissionProtectedTag(@PathParam("id") final int id, @PathParam("tagId") final int tagId);

    /**
     * Get list of versions of IR Analysis
     *
     * @param id
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/")
    List<VersionDTO> getVersions(@PathParam("id") final long id);

    /**
     * Get version of IR Analysis
     *
     * @param id
     * @param version
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}")
    IRVersionFullDTO getVersion(@PathParam("id") final int id, @PathParam("version") final int version);

    /**
     * Update version of IR Analysis
     *
     * @param id
     * @param version
     * @param updateDTO
     * @return
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}")
    VersionDTO updateVersion(@PathParam("id") final int id, @PathParam("version") final int version,
                                    VersionUpdateDTO updateDTO);

    /**
     * Delete version of IR Analysis
     *
     * @param id
     * @param version
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}")
    void deleteVersion(@PathParam("id") final int id, @PathParam("version") final int version) ;

    /**
     * Create a new asset form version of IR Analysis
     *
     * @param id
     * @param version
     * @return
     */
    @PUT
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/version/{version}/createAsset")
    IRAnalysisDTO copyAssetFromVersion(@PathParam("id") final int id, @PathParam("version") final int version);

    /**
     * Get list of incidence rates with assigned tags
     *
     * @param requestDTO
     * @return
     */
    @POST
    @Path("/byTags")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    List<IRAnalysisDTO> listByTags(TagNameListRequestDTO requestDTO);
}
