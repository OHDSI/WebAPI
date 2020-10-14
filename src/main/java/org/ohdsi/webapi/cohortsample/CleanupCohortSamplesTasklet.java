package org.ohdsi.webapi.cohortsample;

import org.ohdsi.webapi.cohortdefinition.CleanupCohortTasklet;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceRepository;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Map;

import static org.ohdsi.webapi.Constants.Params.COHORT_DEFINITION_ID;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;

public class CleanupCohortSamplesTasklet implements Tasklet {
	private static final Logger log = LoggerFactory.getLogger(CleanupCohortTasklet.class);

	private final TransactionTemplate transactionTemplate;
	private final SourceRepository sourceRepository;
	private final CohortSamplingService samplingService;
	private final CohortSampleRepository sampleRepository;

	public CleanupCohortSamplesTasklet(
			final TransactionTemplate transactionTemplate,
			final SourceRepository sourceRepository,
			CohortSamplingService samplingService,
			CohortSampleRepository sampleRepository
	) {
		this.transactionTemplate = transactionTemplate;
		this.sourceRepository = sourceRepository;
		this.samplingService = samplingService;
		this.sampleRepository = sampleRepository;
	}

	private Integer doTask(ChunkContext chunkContext) {
		Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
		int cohortDefinitionId = Integer.parseInt(jobParams.get(COHORT_DEFINITION_ID).toString());

		if (jobParams.containsKey(SOURCE_ID)) {
			int sourceId = Integer.parseInt(jobParams.get(SOURCE_ID).toString());
			Source source = this.sourceRepository.findOne(sourceId);
			if (source != null) {
				return mapSource(source, cohortDefinitionId);
			} else {
				return 0;
			}
		} else {
			return this.sourceRepository.findAll().stream()
					.filter(source-> source.getDaimons()
							.stream()
							.anyMatch(daimon -> daimon.getDaimonType() == SourceDaimon.DaimonType.Results))
					.mapToInt(source -> mapSource(source, cohortDefinitionId))
					.sum();
		}
	}

	private int mapSource(Source source, int cohortDefinitionId) {
		try {
			String resultSchema = source.getTableQualifier(SourceDaimon.DaimonType.Results);
			return transactionTemplate.execute(transactionStatus -> {
				List<CohortSample> samples = sampleRepository.findByCohortDefinitionIdAndSourceId(cohortDefinitionId, source.getId());
				if (samples.isEmpty()) {
					return 0;
				}

				sampleRepository.delete(samples);

				int[] cohortSampleIds = samples.stream()
						.mapToInt(CohortSample::getId)
						.toArray();

				PreparedStatementRenderer renderer = new PreparedStatementRenderer(
						source,
						"/resources/cohortsample/sql/deleteSampleElementsById.sql",
						"results_schema",
						resultSchema,
						"cohortSampleId",
						cohortSampleIds);

				samplingService.getSourceJdbcTemplate(source)
						.update(renderer.getSql(), renderer.getOrderedParams());
				return cohortSampleIds.length;
			});
		} catch (Exception e) {
			log.error("Error deleting samples for cohort: {}, cause: {}", cohortDefinitionId, e.getMessage());
			return 0;
		}
	}

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) throws Exception {
		this.transactionTemplate.execute(status -> doTask(chunkContext));

		return RepeatStatus.FINISHED;
	}

	public JobExecutionResource launch(JobBuilderFactory jobBuilders, StepBuilderFactory stepBuilders, JobTemplate jobTemplate, int cohortDefinitionId) {
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addString(JOB_NAME, String.format("Cleanup cohort samples of cohort definition %d.", cohortDefinitionId));
		builder.addString(COHORT_DEFINITION_ID, String.valueOf(cohortDefinitionId));

		log.info("Beginning cohort cleanup for cohort definition id: {}", cohortDefinitionId);
		return launch(jobBuilders, stepBuilders, jobTemplate, builder.toJobParameters());
	}

	public JobExecutionResource launch(JobBuilderFactory jobBuilders, StepBuilderFactory stepBuilders, JobTemplate jobTemplate, int cohortDefinitionId, int sourceId) {
		JobParametersBuilder builder = new JobParametersBuilder();
		builder.addString(JOB_NAME, String.format("Cleanup cohort samples of cohort definition %d.", cohortDefinitionId));
		builder.addString(COHORT_DEFINITION_ID, String.valueOf(cohortDefinitionId));
		builder.addString(SOURCE_ID, String.valueOf(sourceId));

		log.info("Beginning cohort cleanup for cohort definition id {} and source ID {}", cohortDefinitionId, sourceId);
		return launch(jobBuilders, stepBuilders, jobTemplate, builder.toJobParameters());
	}

	private JobExecutionResource launch(JobBuilderFactory jobBuilders, StepBuilderFactory stepBuilders, JobTemplate jobTemplate, JobParameters jobParameters) {
		Step cleanupStep = stepBuilders.get("cohortSample.cleanupSamples")
				.tasklet(this)
				.build();

		SimpleJobBuilder cleanupJobBuilder = jobBuilders.get("cleanupSamples")
				.start(cleanupStep);

		Job cleanupCohortJob = cleanupJobBuilder.build();

		return jobTemplate.launch(cleanupCohortJob, jobParameters);
	}
}
