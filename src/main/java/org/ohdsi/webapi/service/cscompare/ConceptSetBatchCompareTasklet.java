package org.ohdsi.webapi.service.cscompare;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;
import org.ohdsi.webapi.conceptset.ConceptSetRepository;
import org.ohdsi.webapi.executionengine.job.BaseExecutionTasklet;
import org.ohdsi.webapi.job.artifact.ConceptSetBatchCompareArtifactGenerator;
import org.ohdsi.webapi.service.ConceptSetExpressionResolver;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobAuthorEntity;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobDiffEntity;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobEntity;
import org.ohdsi.webapi.service.cscompare.entity.ConceptSetCompareJobStatsEntity;
import org.ohdsi.webapi.service.cscompare.repository.ConceptSetCompareJobAuthorRepository;
import org.ohdsi.webapi.service.cscompare.repository.ConceptSetCompareJobDiffRepository;
import org.ohdsi.webapi.service.cscompare.repository.ConceptSetCompareJobRepository;
import org.ohdsi.webapi.service.cscompare.repository.ConceptSetCompareJobStatsRepository;
import org.ohdsi.webapi.service.dto.CompareConceptSetsResponse;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConceptSetBatchCompareTasklet extends BaseExecutionTasklet {

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private final VocabularyService vocabularyService;
	private final ConceptSetRepository conceptSetRepository;
	private final SourceRepository sourceRepository;
	private final ConceptSetExpressionResolver conceptSetExpressionResolver;
	private final ConceptSetCompareJobRepository compareJobRepository;
	private final ConceptSetCompareJobDiffRepository compareJobDiffRepository;
	private final ConceptSetCompareJobStatsRepository compareJobStatsRepository;
	private final ConceptSetFilterService filterService;
	private final ConceptSetCompareJobAuthorRepository compareJobAuthorRepository;
	private final UserRepository userRepository;
	private final ConceptSetBatchCompareArtifactGenerator artifactGenerator;
	private final ApplicationEventPublisher eventPublisher;
	private final JobExplorer jobExplorer;

	public ConceptSetBatchCompareTasklet(
		VocabularyService vocabularyService,
		ConceptSetRepository conceptSetRepository,
		SourceRepository sourceRepository,
		ConceptSetExpressionResolver conceptSetExpressionResolver,
		ConceptSetCompareJobRepository compareJobRepository,
		ConceptSetCompareJobDiffRepository compareJobDiffRepository,
		ConceptSetCompareJobStatsRepository compareJobStatsRepository,
		ConceptSetFilterService filterService,
		ConceptSetCompareJobAuthorRepository compareJobAuthorRepository,
		UserRepository userRepository,
		ConceptSetBatchCompareArtifactGenerator artifactGenerator,
		ApplicationEventPublisher eventPublisher,
		JobExplorer jobExplorer
	) {
		this.vocabularyService = vocabularyService;
		this.conceptSetRepository = conceptSetRepository;
		this.sourceRepository = sourceRepository;
		this.conceptSetExpressionResolver = conceptSetExpressionResolver;
		this.compareJobRepository = compareJobRepository;
		this.compareJobDiffRepository = compareJobDiffRepository;
		this.compareJobStatsRepository = compareJobStatsRepository;
		this.filterService = filterService;
		this.compareJobAuthorRepository = compareJobAuthorRepository;
		this.userRepository = userRepository;
		this.artifactGenerator = artifactGenerator;
		this.eventPublisher = eventPublisher;
		this.jobExplorer = jobExplorer;
	}

	@Override
	@Transactional
	public RepeatStatus execute(StepContribution stepContribution, ChunkContext context) throws Exception {
		try {
			Map<String, Object> jobParams = context.getStepContext().getJobParameters();
			Long executionId = context.getStepContext().getStepExecution().getJobExecutionId();

			String source1Key = (String) jobParams.get("source1Key");
			String source2Key = (String) jobParams.get("source2Key");
			String source1Version = (String) jobParams.get("source1Version");
			String source2Version = (String) jobParams.get("source2Version");
			String createdDateFrom = (String) jobParams.get("createdDateFrom");
			String createdDateTo = (String) jobParams.get("createdDateTo");
			String updatedDateFrom = (String) jobParams.get("updatedDateFrom");
			String updatedDateTo = (String) jobParams.get("updatedDateTo");
			String tagsParam = (String) jobParams.get("tagsIds");
			Boolean skipLocked = Boolean.parseBoolean((String) jobParams.get("skipLocked"));
			String authorIdsParam = (String) jobParams.get("authorIds");
			Boolean compareSourceCodes = Boolean.parseBoolean((String) jobParams.get("compareSourceCodes"));
			String conceptSetIdsParam = (String) jobParams.get("conceptSetIds");

			log.info("Executing batch compare with parameters: source1Key={}, source2Key={}, " +
					"createdDateFrom={}, createdDateTo={}, updatedDateFrom={}, updatedDateTo={}, " +
					"tags={}, skipLocked={}, authorIds={}, compareSourceCodes={}, conceptSetIds={}",
				source1Key, source2Key, createdDateFrom, createdDateTo,
				updatedDateFrom, updatedDateTo, tagsParam, skipLocked, authorIdsParam,
				compareSourceCodes, conceptSetIdsParam);

			Source source1 = sourceRepository.findBySourceKey(source1Key);
			Source source2 = sourceRepository.findBySourceKey(source2Key);

			if (source1 == null || source2 == null) {
				throw new IllegalArgumentException("Invalid source keys provided");
			}

			// Build filter criteria
			ConceptSetFilterService.ConceptSetFilterCriteria criteria = buildFilterCriteria(
				createdDateFrom, createdDateTo, updatedDateFrom, updatedDateTo,
				tagsParam, skipLocked, authorIdsParam, conceptSetIdsParam
			);

			// Get filtered concept sets
			List<ConceptSet> conceptSets = filterService.filterConceptSets(criteria);

			log.info("Found {} concept sets matching filter criteria", conceptSets.size());

			// Save ConceptSetCompareJobEntity with vocabulary versions
			ConceptSetCompareJobEntity compareJob = createAndSaveCompareJob(
				source1, source2, source1Version, source2Version, createdDateFrom, createdDateTo,
				updatedDateFrom, updatedDateTo, tagsParam, skipLocked, authorIdsParam, compareSourceCodes, conceptSetIdsParam,
				executionId, conceptSets.size()
			);

			if (conceptSets.isEmpty()) {
				log.warn("No concept sets found matching the specified criteria");
				compareJob.setConceptSetsWithDiffs(0);
				compareJobRepository.save(compareJob);
				return RepeatStatus.FINISHED;
			}

			// Track which concept sets have differences
			Set<Integer> conceptSetsWithDiffs = new HashSet<>();

			// Process comparisons
			List<ConceptSetComparisonResult> results = conceptSets.stream()
				.map(conceptSet -> {
					try {
						VocabularyService.CompareConceptSetsRequest request = toCompareRequest(conceptSet, source1, source2, compareSourceCodes);
						CompareConceptSetsResponse response = vocabularyService.compareConceptSetsOverDiffVocabs(request);
						return new ConceptSetComparisonResult(conceptSet.getId(), response);
					} catch (Exception e) {
						log.error("Error comparing concept set ID {}: {}", conceptSet.getId(), e.getMessage(), e);
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

			log.info("Successfully compared {} concept sets", results.size());

			// Save comparison results, statistics, and track concept sets with differences
			results.forEach(result -> {
				boolean hasDiffs = saveJobDiff(result.conceptSetId, result.response.getComparisons(), compareJob);
				if (hasDiffs) {
					conceptSetsWithDiffs.add(result.conceptSetId);
				}

				// Save statistics for this concept set
				saveJobStats(result.conceptSetId, result.response, hasDiffs, compareJob);
			});

			// Update the compare job with the count of concept sets with differences
			compareJob.setConceptSetsWithDiffs(conceptSetsWithDiffs.size());
			compareJobRepository.save(compareJob);

			log.info("Batch compare job completed successfully. Analyzed: {}, With Differences: {}",
				conceptSets.size(), conceptSetsWithDiffs.size());

			return RepeatStatus.FINISHED;

		} catch (Exception e) {
			log.error("Error executing batch compare tasklet", e);
			throw e;
		}
	}

	private ConceptSetFilterService.ConceptSetFilterCriteria buildFilterCriteria(
		String createdDateFrom, String createdDateTo,
		String updatedDateFrom, String updatedDateTo,
		String tagsParam, Boolean skipLocked, String authorIdsParam,
		String conceptSetIdsParam
	) {
		ConceptSetFilterService.ConceptSetFilterCriteria criteria =
			new ConceptSetFilterService.ConceptSetFilterCriteria();

		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

		if (StringUtils.isNotBlank(createdDateFrom)) {
			criteria.setCreatedFrom(LocalDate.parse(createdDateFrom, formatter));
		}
		if (StringUtils.isNotBlank(createdDateTo)) {
			criteria.setCreatedTo(LocalDate.parse(createdDateTo, formatter));
		}
		if (StringUtils.isNotBlank(updatedDateFrom)) {
			criteria.setUpdatedFrom(LocalDate.parse(updatedDateFrom, formatter));
		}
		if (StringUtils.isNotBlank(updatedDateTo)) {
			criteria.setUpdatedTo(LocalDate.parse(updatedDateTo, formatter));
		}
		if (StringUtils.isNotBlank(tagsParam)) {
			List<Integer> tagIds = Arrays.stream(tagsParam.split(","))
				.map(String::trim)
				.filter(StringUtils::isNotBlank)
				.map(Integer::parseInt)
				.collect(Collectors.toList());
			criteria.setTagIds(tagIds);
		}

		criteria.setSkipLocked(skipLocked != null && skipLocked);

		if (StringUtils.isNotBlank(authorIdsParam)) {
			List<Long> authorIds = Arrays.stream(authorIdsParam.split(","))
				.map(String::trim)
				.filter(StringUtils::isNotBlank)
				.map(Long::parseLong)
				.collect(Collectors.toList());
			criteria.setAuthorIds(authorIds);
		}

		if (StringUtils.isNotBlank(conceptSetIdsParam)) {
			List<Integer> conceptSetIds = Arrays.stream(conceptSetIdsParam.split(","))
				.map(String::trim)
				.filter(StringUtils::isNotBlank)
				.map(Integer::parseInt)
				.collect(Collectors.toList());
			criteria.setConceptSetIds(conceptSetIds);
			log.debug("Setting concept set ID filter with {} IDs: {}",
				conceptSetIds.size(), conceptSetIds);
		}

		return criteria;
	}

	private ConceptSetCompareJobEntity createAndSaveCompareJob(
		Source source1, Source source2, String source1Version, String source2Version,
		String createdDateFrom, String createdDateTo, String updatedDateFrom, String updatedDateTo,
		String tags, Boolean skipLocked, String authorIdsParam, Boolean compareSourceCodes, String conceptSetIdsParam, Long executionId,
		int conceptSetsAnalyzed
	) {
		ConceptSetCompareJobEntity compareJob = new ConceptSetCompareJobEntity();
		compareJob.setExecutionId(executionId);
		compareJob.setSource1Key(source1.getSourceKey());
		compareJob.setSource2Key(source2.getSourceKey());
		compareJob.setVocab1Version(source1Version);
		compareJob.setVocab2Version(source2Version);
		compareJob.setSkipLocked(skipLocked != null ? skipLocked : false);
		compareJob.setCompareSourceCodes(compareSourceCodes != null ? compareSourceCodes : false);
		compareJob.setConceptSetsAnalyzed(conceptSetsAnalyzed);

		DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

		if (StringUtils.isNotBlank(createdDateFrom)) {
			compareJob.setCreatedFrom(LocalDate.parse(createdDateFrom, formatter));
		}
		if (StringUtils.isNotBlank(createdDateTo)) {
			compareJob.setCreatedTo(LocalDate.parse(createdDateTo, formatter));
		}
		if (StringUtils.isNotBlank(updatedDateFrom)) {
			compareJob.setUpdatedFrom(LocalDate.parse(updatedDateFrom, formatter));
		}
		if (StringUtils.isNotBlank(updatedDateTo)) {
			compareJob.setUpdatedTo(LocalDate.parse(updatedDateTo, formatter));
		}
		if (StringUtils.isNotBlank(tags)) {
			compareJob.setTags(tags);
		}

		if (StringUtils.isNotBlank(conceptSetIdsParam)) {
			compareJob.setConceptSetIds(conceptSetIdsParam);
			log.debug("Stored concept set IDs filter: {}", conceptSetIdsParam);
		}

		compareJob = compareJobRepository.save(compareJob);

		if (StringUtils.isNotBlank(authorIdsParam)) {
			List<Long> authorIds = Arrays.stream(authorIdsParam.split(","))
				.map(String::trim)
				.filter(StringUtils::isNotBlank)
				.map(Long::parseLong)
				.collect(Collectors.toList());

			for (Long authorId : authorIds) {
				UserEntity user = userRepository.findOne(authorId);
				if (user != null) {
					ConceptSetCompareJobAuthorEntity authorEntity = new ConceptSetCompareJobAuthorEntity();
					authorEntity.setUser(user);
					compareJob.addAuthor(authorEntity);
				}
			}

			compareJob = compareJobRepository.save(compareJob);
		}

		return compareJob;
	}

	private boolean saveJobDiff(Integer conceptSetId, Collection<ConceptSetComparison> comparisons,
															ConceptSetCompareJobEntity compareJob) {
		if (comparisons == null || comparisons.isEmpty()) {
			return false;
		}

		List<ConceptSetCompareJobDiffEntity> diffEntities = comparisons.stream()
			.filter(this::hasDifference)
			.map(comparison -> createDiffEntity(conceptSetId, comparison, compareJob))
			.collect(Collectors.toList());

		if (!diffEntities.isEmpty()) {
			diffEntities.forEach(compareJobDiffRepository::save);
			log.debug("Saved {} diff entries for concept set ID {}", diffEntities.size(), conceptSetId);
			return true;
		}

		return false;
	}

	private void saveJobStats(Integer conceptSetId, CompareConceptSetsResponse response,
														boolean hasDifferences, ConceptSetCompareJobEntity compareJob) {
		ConceptSetCompareJobStatsEntity statsEntity = new ConceptSetCompareJobStatsEntity();
		statsEntity.setCompareJob(compareJob);
		statsEntity.setConceptSetId(conceptSetId);
		statsEntity.setCs1IncludedConceptsCount(response.getCs1IncludedConceptsCount());
		statsEntity.setCs1IncludedSourceCodesCount(response.getCs1IncludedSourceCodesCount());
		statsEntity.setCs2IncludedConceptsCount(response.getCs2IncludedConceptsCount());
		statsEntity.setCs2IncludedSourceCodesCount(response.getCs2IncludedSourceCodesCount());
		statsEntity.setHasDifferences(hasDifferences);

		compareJobStatsRepository.save(statsEntity);
		log.debug("Saved statistics for concept set ID {}: CS1 concepts={}, CS1 source codes={}, " +
				"CS2 concepts={}, CS2 source codes={}, hasDifferences={}",
			conceptSetId, response.getCs1IncludedConceptsCount(), response.getCs1IncludedSourceCodesCount(),
			response.getCs2IncludedConceptsCount(), response.getCs2IncludedSourceCodesCount(), hasDifferences);
	}

	private boolean hasDifference(ConceptSetComparison comparison) {
		return (comparison.conceptInCS1Only != null && comparison.conceptInCS1Only > 0) ||
			(comparison.conceptInCS2Only != null && comparison.conceptInCS2Only > 0) ||
			comparison.nameMismatch ||
			comparison.standardConceptMismatch ||
			comparison.invalidReasonMismatch ||
			comparison.conceptCodeMismatch ||
			comparison.domainIdMismatch ||
			comparison.vocabularyIdMismatch ||
			comparison.conceptClassIdMismatch ||
			comparison.validStartDateMismatch ||
			comparison.validEndDateMismatch;
	}

	private ConceptSetCompareJobDiffEntity createDiffEntity(
		Integer conceptSetId, ConceptSetComparison comparison, ConceptSetCompareJobEntity compareJob
	) {
		ConceptSetCompareJobDiffEntity diffEntity = new ConceptSetCompareJobDiffEntity();

		diffEntity.setCompareJob(compareJob);
		diffEntity.setConceptSetId(conceptSetId);
		diffEntity.setConceptId(comparison.conceptId != null ? comparison.conceptId.intValue() : null);
		diffEntity.setIsSourceCode(comparison.isSourceCode);
		diffEntity.setConceptInCS1Only(comparison.conceptInCS1Only);
		diffEntity.setConceptInCS2Only(comparison.conceptInCS2Only);
		diffEntity.setConceptInCS1AndCS2(comparison.conceptInCS1AndCS2);

		if (comparison.nameMismatch) {
			diffEntity.setVocab1ConceptName(comparison.vocab1ConceptName);
			diffEntity.setVocab2ConceptName(comparison.vocab2ConceptName);
		}

		if (comparison.standardConceptMismatch) {
			diffEntity.setVocab1StandardConcept(comparison.vocab1StandardConcept);
			diffEntity.setVocab2StandardConcept(comparison.vocab2StandardConcept);
		}

		if (comparison.invalidReasonMismatch) {
			diffEntity.setVocab1InvalidReason(comparison.vocab1InvalidReason);
			diffEntity.setVocab2InvalidReason(comparison.vocab2InvalidReason);
		}

		if (comparison.conceptCodeMismatch) {
			diffEntity.setVocab1ConceptCode(comparison.vocab1ConceptCode);
			diffEntity.setVocab2ConceptCode(comparison.vocab2ConceptCode);
		}

		if (comparison.domainIdMismatch) {
			diffEntity.setVocab1DomainId(comparison.vocab1DomainId);
			diffEntity.setVocab2DomainId(comparison.vocab2DomainId);
		}

		if (comparison.vocabularyIdMismatch) {
			diffEntity.setVocab1VocabularyId(comparison.vocab1VocabularyId);
			diffEntity.setVocab2VocabularyId(comparison.vocab2VocabularyId);
		}

		if (comparison.conceptClassIdMismatch) {
			diffEntity.setVocab1ConceptClassId(comparison.vocab1ConceptClassId);
			diffEntity.setVocab2ConceptClassId(comparison.vocab2ConceptClassId);
		}

		if (comparison.validStartDateMismatch) {
			diffEntity.setVocab1ValidStartDate(comparison.vocab1ValidStartDate);
			diffEntity.setVocab2ValidStartDate(comparison.vocab2ValidStartDate);
		}

		if (comparison.validEndDateMismatch) {
			diffEntity.setVocab1ValidEndDate(comparison.vocab1ValidEndDate);
			diffEntity.setVocab2ValidEndDate(comparison.vocab2ValidEndDate);
		}

		diffEntity.setNameMismatch(comparison.nameMismatch);
		diffEntity.setStandardConceptMismatch(comparison.standardConceptMismatch);
		diffEntity.setInvalidReasonMismatch(comparison.invalidReasonMismatch);
		diffEntity.setConceptCodeMismatch(comparison.conceptCodeMismatch);
		diffEntity.setDomainIdMismatch(comparison.domainIdMismatch);
		diffEntity.setVocabularyIdMismatch(comparison.vocabularyIdMismatch);
		diffEntity.setConceptClassIdMismatch(comparison.conceptClassIdMismatch);
		diffEntity.setValidStartDateMismatch(comparison.validStartDateMismatch);
		diffEntity.setValidEndDateMismatch(comparison.validEndDateMismatch);

		return diffEntity;
	}

	private VocabularyService.CompareConceptSetsRequest toCompareRequest(
		ConceptSet conceptSet, Source source1, Source source2, Boolean compareSourceCodes
	) {
		VocabularyService.CompareConceptSetsRequest compareConceptSetsRequest =
			new VocabularyService.CompareConceptSetsRequest();

		Integer id = conceptSet.getId();

		ConceptSetExpression expression1 = conceptSetExpressionResolver.getConceptSetExpression(source1, id);
		ConceptSetExpression expression2 = conceptSetExpressionResolver.getConceptSetExpression(source2, id);

		compareConceptSetsRequest.source1Key = source1.getSourceKey();
		compareConceptSetsRequest.source2Key = source2.getSourceKey();
		compareConceptSetsRequest.expression1 = expression1;
		compareConceptSetsRequest.expression2 = expression2;
		compareConceptSetsRequest.compareSourceCodes = compareSourceCodes != null ? compareSourceCodes : false;

		return compareConceptSetsRequest;
	}

	private static class ConceptSetComparisonResult {
		final Integer conceptSetId;
		final CompareConceptSetsResponse response;

		ConceptSetComparisonResult(Integer conceptSetId, CompareConceptSetsResponse response) {
			this.conceptSetId = conceptSetId;
			this.response = response;
		}
	}
}