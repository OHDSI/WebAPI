package org.ohdsi.webapi.service;

import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortsample.CohortSample;
import org.ohdsi.webapi.cohortsample.CohortSampleRepository;
import org.ohdsi.webapi.cohortsample.CohortSamplingService;
import org.ohdsi.webapi.cohortsample.SampleElement;
import org.ohdsi.webapi.cohortsample.dto.CohortSampleDTO;
import org.ohdsi.webapi.cohortsample.dto.SampleParametersDTO;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
import java.util.stream.Collectors;

@Path("/cohortsample/{cohortDefinitionId}/{sourceKey}")
@Component
@Produces(MediaType.APPLICATION_JSON)
public class CohortSampleService {
    private final CohortDefinitionRepository cohortDefinitionRepository;
    private final CohortSampleRepository sampleRepository;
    private final CohortSamplingService samplingService;
    private final SourceRepository sourceRepository;

    @Autowired
    public CohortSampleService(
            CohortSampleRepository sampleRepository,
            CohortSamplingService samplingService,
            SourceRepository sourceRepository,
            CohortDefinitionRepository cohortDefinitionRepository
    ) {
        this.sampleRepository = sampleRepository;
        this.samplingService = samplingService;
        this.sourceRepository = sourceRepository;
        this.cohortDefinitionRepository = cohortDefinitionRepository;
    }

    @Path("/")
    @GET
    public List<CohortSampleDTO> listCohortSamples(
            @PathParam("cohortDefinitionId") int cohortDefinitionId,
            @PathParam("sourceKey") String sourceKey
    ) {
        Source source = getSource(sourceKey);
        return this.sampleRepository.findByCohortDefinitionIdAndSourceId(cohortDefinitionId, source.getId()).stream()
                .map(s -> samplingService.sampleToSampleDTO(s, null))
                .collect(Collectors.toList());
    }

    @Path("/{sampleId}")
    @GET
    public CohortSampleDTO getCohortSample(
            @PathParam("sampleId") Integer sampleId,
            @DefaultValue("recordCount") @QueryParam("fields") String fields
    ) {
        CohortSample sample = sampleRepository.findOne(sampleId);
        if (sample == null) {
            throw new NotFoundException("Cohort sample with ID " + sampleId + " not found");
        }
        List<String> returnFields = Arrays.asList(fields.split(","));
        boolean withRecordCounts = returnFields.contains("recordCount");
        Source source = sourceRepository.findBySourceId(sample.getSourceId());
        List<SampleElement> elements = this.samplingService.findSampleElements(source, sampleId, withRecordCounts);
        return samplingService.sampleToSampleDTO(sample, elements);
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
        samplingService.deleteSamples(cohortDefinitionId, source);
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
