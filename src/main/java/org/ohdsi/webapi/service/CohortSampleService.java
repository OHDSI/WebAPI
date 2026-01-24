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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@RequestMapping("/cohortsample")
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

	/**
	 * Get information about cohort samples for a data source
	 *
	 * @param cohortDefinitionId The id for an existing cohort definition
	 * @param sourceKey
	 * @return JSON containing information about cohort samples
	 */
	@GetMapping(value = "/{cohortDefinitionId}/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
	public CohortSampleListDTO listCohortSamples(
			@PathVariable("cohortDefinitionId") int cohortDefinitionId,
			@PathVariable("sourceKey") String sourceKey
	) {
		Source source = getSource(sourceKey);
		CohortSampleListDTO result = new CohortSampleListDTO();

		result.setCohortDefinitionId(cohortDefinitionId);
		result.setSourceId(source.getId());

		CohortGenerationInfo generationInfo = generationInfoRepository.findById(
				new CohortGenerationInfoId(cohortDefinitionId, source.getId())).orElse(null);
		result.setGenerationStatus(generationInfo != null ? generationInfo.getStatus() : null);
		result.setIsValid(generationInfo != null && generationInfo.isIsValid());

		result.setSamples(this.samplingService.listSamples(cohortDefinitionId, source.getId()));

		return result;
	}

	/**
	 * Get an existing cohort sample
	 * @param cohortDefinitionId
	 * @param sourceKey
	 * @param sampleId
	 * @param fields
	 * @return personId, gender, age of each person in the cohort sample
	 */
	@GetMapping(value = "/{cohortDefinitionId}/{sourceKey}/{sampleId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public CohortSampleDTO getCohortSample(
			@PathVariable("cohortDefinitionId") int cohortDefinitionId,
			@PathVariable("sourceKey") String sourceKey,
			@PathVariable("sampleId") Integer sampleId,
			@RequestParam(defaultValue = "") String fields
	) {
		List<String> returnFields = Arrays.asList(fields.split(","));
		boolean withRecordCounts = returnFields.contains("recordCount");
		return this.samplingService.getSample(sampleId, withRecordCounts);
	}

	/**
	 * @summary Refresh a cohort sample
	 * Refresh a cohort sample for a given source key. This will re-sample persons from the cohort.
	 * @param cohortDefinitionId
	 * @param sourceKey
	 * @param sampleId
	 * @param fields
	 * @return A sample of persons from a cohort
	 */
	@PostMapping(value = "/{cohortDefinitionId}/{sourceKey}/{sampleId}/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
	public CohortSampleDTO refreshCohortSample(
			@PathVariable("cohortDefinitionId") int cohortDefinitionId,
			@PathVariable("sourceKey") String sourceKey,
			@PathVariable("sampleId") Integer sampleId,
			@RequestParam(defaultValue = "") String fields
	) {
		List<String> returnFields = Arrays.asList(fields.split(","));
		boolean withRecordCounts = returnFields.contains("recordCount");
		this.samplingService.refreshSample(sampleId);
		return this.samplingService.getSample(sampleId, withRecordCounts);
	}

	/**
	 * Does an existing cohort have samples?
	 * @param cohortDefinitionId
	 * @return true or false
	 */
	@GetMapping(value = "/has-samples/{cohortDefinitionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Boolean> hasSamples(
			@PathVariable("cohortDefinitionId") int cohortDefinitionId
	) {
		int nSamples = this.samplingService.countSamples(cohortDefinitionId);
		return Collections.singletonMap("hasSamples", nSamples > 0);
	}

	/**
	 * Does an existing cohort have samples from a particular source?
	 * @param sourceKey
	 * @param cohortDefinitionId
	 * @return true or false
	 */
	@GetMapping(value = "/has-samples/{cohortDefinitionId}/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Map<String, Boolean> hasSamplesForSource(
			@PathVariable("sourceKey") String sourceKey,
			@PathVariable("cohortDefinitionId") int cohortDefinitionId
	) {
		Source source = getSource(sourceKey);
		int nSamples = this.samplingService.countSamples(cohortDefinitionId, source.getId());
		return Collections.singletonMap("hasSamples", nSamples > 0);
	}

	/**
	 * Create a new cohort sample
	 * @param sourceKey
	 * @param cohortDefinitionId
	 * @param sampleParameters
	 * @return
	 */
	@PostMapping(value = "/{cohortDefinitionId}/{sourceKey}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public CohortSampleDTO createCohortSample(
			@PathVariable("sourceKey") String sourceKey,
			@PathVariable("cohortDefinitionId") int cohortDefinitionId,
			@RequestBody SampleParametersDTO sampleParameters
	) {
		sampleParameters.validate();
		Source source = getSource(sourceKey);
		if (cohortDefinitionRepository.findById(cohortDefinitionId).orElse(null) == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cohort definition " + cohortDefinitionId + " does not exist.");
		}
		CohortGenerationInfo generationInfo = generationInfoRepository.findById(
				new CohortGenerationInfoId(cohortDefinitionId, source.getId())).orElse(null);
		if (generationInfo == null || generationInfo.getStatus() != GenerationStatus.COMPLETE) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cohort is not yet generated");
		}
		return samplingService.createSample(source, cohortDefinitionId, sampleParameters);
	}

	/**
	 * Delete a cohort sample
	 * @param sourceKey
	 * @param cohortDefinitionId
	 * @param sampleId
	 * @return
	 */
	@DeleteMapping("/{cohortDefinitionId}/{sourceKey}/{sampleId}")
	public ResponseEntity<Void> deleteCohortSample(
			@PathVariable("sourceKey") String sourceKey,
			@PathVariable("cohortDefinitionId") int cohortDefinitionId,
			@PathVariable("sampleId") int sampleId
	) {
		Source source = getSource(sourceKey);
		if (cohortDefinitionRepository.findById(cohortDefinitionId).orElse(null) == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cohort definition " + cohortDefinitionId + " does not exist.");
		}
		samplingService.deleteSample(cohortDefinitionId, source, sampleId);
		return ResponseEntity.noContent().build();
	}

	/**
	 * Delete all samples for a cohort on a data source
	 * @param sourceKey
	 * @param cohortDefinitionId
	 * @return
	 */
	@DeleteMapping("/{cohortDefinitionId}/{sourceKey}")
	public ResponseEntity<Void> deleteCohortSamples(
			@PathVariable("sourceKey") String sourceKey,
			@PathVariable("cohortDefinitionId") int cohortDefinitionId
	) {
		Source source = getSource(sourceKey);
		if (cohortDefinitionRepository.findById(cohortDefinitionId).orElse(null) == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cohort definition " + cohortDefinitionId + " does not exist.");
		}
		samplingService.launchDeleteSamplesTasklet(cohortDefinitionId, source.getId());
		return ResponseEntity.status(HttpStatus.ACCEPTED).build();
	}

	private Source getSource(String sourceKey) {
		Source source = sourceRepository.findBySourceKey(sourceKey);
		if (source == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Source " + sourceKey + " does not exist");
		}
		return source;
	}
}
