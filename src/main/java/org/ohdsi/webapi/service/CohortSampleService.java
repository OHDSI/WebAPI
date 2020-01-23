package org.ohdsi.webapi.service;

import org.ohdsi.webapi.GenerationStatus;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfoId;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfoRepository;
import org.ohdsi.webapi.cohortsample.CohortSamplingService;
import org.ohdsi.webapi.cohortsample.dto.CohortSampleDTO;
import org.ohdsi.webapi.cohortsample.dto.CohortSampleListDTO;
import org.ohdsi.webapi.cohortsample.dto.SampleParametersDTO;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

@Path("/cohortsample/{cohortDefinitionId}/{sourceKey}")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class CohortSampleService {
    private final CohortDefinitionRepository cohortDefinitionRepository;
    private final CohortGenerationInfoRepository generationInfoRepository;
    private final CohortSamplingService samplingService;
    private final SourceRepository sourceRepository;

    @Autowired
    public CohortSampleService(
            CohortSamplingService samplingService,
            SourceRepository sourceRepository,
            CohortDefinitionRepository cohortDefinitionRepository,
            CohortGenerationInfoRepository generationInfoRepository
    ) {
        this.samplingService = samplingService;
        this.sourceRepository = sourceRepository;
        this.cohortDefinitionRepository = cohortDefinitionRepository;
        this.generationInfoRepository = generationInfoRepository;
    }

    @Path("/")
    @GET
    public CohortSampleListDTO listCohortSamples(
            @PathParam("cohortDefinitionId") int cohortDefinitionId,
            @PathParam("sourceKey") String sourceKey
    ) {
        Source source = getSource(sourceKey);
        CohortSampleListDTO result = new CohortSampleListDTO();

        result.setCohortDefinitionId(cohortDefinitionId);
        result.setSourceId(source.getId());

        CohortGenerationInfo generationInfo = generationInfoRepository.findOne(
                new CohortGenerationInfoId(cohortDefinitionId, source.getId()));
        result.setGenerationStatus(generationInfo != null ? generationInfo.getStatus() : null);

        result.setSamples(this.samplingService.listSamples(cohortDefinitionId, source.getId()));

        return result;
    }

    @Path("/{sampleId}")
    @GET
    public CohortSampleDTO getCohortSample(
            @PathParam("sampleId") Integer sampleId,
            @DefaultValue("recordCount") @QueryParam("fields") String fields
    ) {
        List<String> returnFields = Arrays.asList(fields.split(","));
        boolean withRecordCounts = returnFields.contains("recordCount");
        return this.samplingService.getSample(sampleId, withRecordCounts);
    }

    @Path("/")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public CohortSampleDTO createCohortSample(
            @PathParam("sourceKey") String sourceKey,
            @PathParam("cohortDefinitionId") int cohortDefinitionId,
            SampleParametersDTO sampleParameters
    ) {
        sampleParameters.validate();
        Source source = getSource(sourceKey);
        if (cohortDefinitionRepository.findOne(cohortDefinitionId) == null) {
            throw new NotFoundException("Cohort definition " + cohortDefinitionId + " does not exist.");
        }
        CohortGenerationInfo generationInfo = generationInfoRepository.findOne(
                new CohortGenerationInfoId(cohortDefinitionId, source.getId()));
        if (generationInfo == null || generationInfo.getStatus() != GenerationStatus.COMPLETE) {
            throw new BadRequestException("Cohort is not yet generated");
        }
        return samplingService.createSample(source, cohortDefinitionId, sampleParameters);
    }

    @Path("/{sampleId}")
    @DELETE
    public Response deleteCohortSample(
            @PathParam("sourceKey") String sourceKey,
            @PathParam("cohortDefinitionId") int cohortDefinitionId,
            @PathParam("sampleId") int sampleId
    ) {
        Source source = getSource(sourceKey);
        if (cohortDefinitionRepository.findOne(cohortDefinitionId) == null) {
            throw new NotFoundException("Cohort definition " + cohortDefinitionId + " does not exist.");
        }
        samplingService.deleteSample(cohortDefinitionId, source, sampleId);
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @Path("/")
    @DELETE
    public Response deleteCohortSamples(
            @PathParam("sourceKey") String sourceKey,
            @PathParam("cohortDefinitionId") int cohortDefinitionId
    ) {
        Source source = getSource(sourceKey);
        if (cohortDefinitionRepository.findOne(cohortDefinitionId) == null) {
            throw new NotFoundException("Cohort definition " + cohortDefinitionId + " does not exist.");
        }
        samplingService.launchDeleteSamplesTasklet(cohortDefinitionId, source.getId());
        return Response.status(Response.Status.ACCEPTED).build();
    }

    private Source getSource(String sourceKey) {
        Source source = sourceRepository.findBySourceKey(sourceKey);
        if (source == null) {
            throw new NotFoundException("Source " + sourceKey + " does not exist");
        }
        return source;
    }
}
