package org.ohdsi.webapi.service;

import org.ohdsi.webapi.ircalc.AnalysisReport;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.springframework.transaction.annotation.Transactional;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
public interface IRAnalysisResource {

    /**
     * Returns all IR Analysis in a list.
     *
     * @return List of IncidenceRateAnalysis
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    List<IRAnalysisService.IRAnalysisListItem> getIRAnalysisList();

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
    @Transactional
    IRAnalysisService.IRAnalysisDTO createAnalysis(IRAnalysisService.IRAnalysisDTO analysis);

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Transactional(readOnly = true)
    IRAnalysisService.IRAnalysisDTO getAnalysis(@PathParam("id") final int id);

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    IRAnalysisService.IRAnalysisDTO saveAnalysis(@PathParam("id") final int id, IRAnalysisService.IRAnalysisDTO analysis);

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
    @Transactional(readOnly = true)
    List<IRAnalysisService.AnalysisInfoDTO> getAnalysisInfo(@PathParam("id") final int id);

    @GET
    @Path("/{id}/info/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional(readOnly = true)
    IRAnalysisService.AnalysisInfoDTO getAnalysisInfo(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey);

    /**
     * Deletes the specified cohort definition
     *
     * @param id - the Cohort Definition ID to copy
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/info/{sourceKey}")
    @Transactional
    void deleteInfo(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey);

    /**
     * Deletes the specified cohort definition
     *
     * @param id - the Cohort Definition ID to copy
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    @Transactional
    void delete(@PathParam("id") final int id);

    /**
     * Exports the analysis definition and results
     *
     * @param id - the IR Analysis ID to export
     * @return Response containing binary stream of zipped data
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/export")
    @Transactional
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
    @Transactional
    IRAnalysisService.IRAnalysisDTO copy(@PathParam("id") final int id);

    @GET
    @Path("/{id}/report/{sourceKey}")
    @Produces(MediaType.APPLICATION_JSON)
    @Transactional
    AnalysisReport getAnalysisReport(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey, 
                                     @QueryParam("targetId") final int targetId, @QueryParam("outcomeId") final int outcomeId );
}
