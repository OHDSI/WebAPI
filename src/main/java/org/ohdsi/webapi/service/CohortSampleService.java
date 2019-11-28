package org.ohdsi.webapi.service;

import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortsample.CohortSample;
import org.ohdsi.webapi.cohortsample.CohortSamplingService;
import org.ohdsi.webapi.cohortsample.dto.SampleParametersDTO;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Path("/cohortsample/{cohortDefinitionId}/{sourceKey}")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class CohortSampleService {
    private final CohortDefinitionRepository cohortDefinitionRepository;
    private final CohortSamplingService samplingService;
    private final SourceRepository sourceRepository;

    @Autowired
    public CohortSampleService(
            CohortSamplingService samplingService,
            SourceRepository sourceRepository,
            CohortDefinitionRepository cohortDefinitionRepository
    ) {
        this.samplingService = samplingService;
        this.sourceRepository = sourceRepository;
        this.cohortDefinitionRepository = cohortDefinitionRepository;
    }

    @Path("/")
    @GET
    public List<CohortSample> listCohortSamples(
            @QueryParam("cohortDefinitionId") int cohortDefinitionId,
            @QueryParam("sourceKey") String sourceKey
    ) {
        Source source = getSource(sourceKey);
        return this.samplingService.findSamples(source, cohortDefinitionId);
    }

    @Path("/{sampleId}")
    @GET
    public CohortSample getCohortSample(
            @QueryParam("sourceKey") String sourceKey,
            @QueryParam("sampleId") int sampleId
    ) {
        Source source = getSource(sourceKey);
        CohortSample sample = samplingService.findSample(source, sampleId);
        if (sample == null) {
            throw new NotFoundException("Cohort sample with ID " + sampleId + " not found");
        }
        sample.setElements(this.samplingService.findSampleElements(source, sampleId));
        return sample;
    }


    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public CohortSample createCohortSample(
            @QueryParam("sourceKey") String sourceKey,
            @QueryParam("cohortDefinitionId") int cohortDefinitionId,
            SampleParametersDTO sampleParameters
    ) {
        Source source = getSource(sourceKey);
        if (cohortDefinitionRepository.findOne(cohortDefinitionId) == null) {
            throw new NotFoundException("Cohort definition " + cohortDefinitionId + " does not exist.");
        }
        return samplingService.createSample(source, cohortDefinitionId, sampleParameters);
    }

    @Path("/{sampleId}")
    @DELETE
    public Response deleteCohortSample(
            @QueryParam("sourceKey") String sourceKey,
            @QueryParam("sampleId") int sampleId
    ) {
        Source source = getSource(sourceKey);
        samplingService.deleteSample(source, sampleId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    private Source getSource(String sourceKey) {
        Source source = sourceRepository.findBySourceKey(sourceKey);
        if (source == null) {
            throw new NotFoundException("Source " + sourceKey + " does not exist");
        }
        return source;
    }
}
