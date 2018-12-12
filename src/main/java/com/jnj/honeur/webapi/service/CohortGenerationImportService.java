package com.jnj.honeur.webapi.service;

import com.jnj.honeur.webapi.cohortdefinition.CohortGenerationResults;
import com.jnj.honeur.webapi.cohortdefinition.ImportCohortGenerationTasklet;
import com.jnj.honeur.webapi.cohortdefinition.ImportJobExecutionListener;
import com.jnj.honeur.webapi.cohortfeatures.CohortFeaturesRepository;
import com.jnj.honeur.webapi.cohortfeaturesanalysisref.CohortFeaturesAnalysisRefRepository;
import com.jnj.honeur.webapi.cohortfeaturesdist.CohortFeaturesDistRepository;
import com.jnj.honeur.webapi.cohortfeaturesref.CohortFeaturesRefRepository;
import com.jnj.honeur.webapi.cohortinclusion.CohortInclusionRepository;
import com.jnj.honeur.webapi.cohortinclusionresult.CohortInclusionResultRepository;
import com.jnj.honeur.webapi.cohortinclusionstats.CohortInclusionStatsRepository;
import com.jnj.honeur.webapi.cohortsummarystats.CohortSummaryStatsRepository;
import com.jnj.honeur.webapi.shiro.LiferayPermissionManager;
import org.ohdsi.webapi.cohort.CohortRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfoRepository;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.source.Source;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.job.builder.SimpleJobBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Optional;

import static org.ohdsi.webapi.Constants.GENERATE_COHORT;
import static org.ohdsi.webapi.Constants.Params.COHORT_DEFINITION_ID;
import static org.ohdsi.webapi.Constants.Params.SOURCE_ID;
import static org.ohdsi.webapi.Constants.Params.SOURCE_KEY;

@Component
@ConditionalOnExpression("${datasource.honeur.enabled} and ${webapi.central}")
public class CohortGenerationImportService extends AbstractDaoService {

    private final StepBuilderFactory stepBuilders;
    private final JobBuilderFactory jobBuilders;
    private final JobTemplate jobTemplate;
    private final CohortRepository cohortRepository;
    private final CohortInclusionRepository cohortInclusionRepository;
    private final CohortInclusionStatsRepository cohortInclusionStatsRepository;
    private final CohortInclusionResultRepository cohortInclusionResultRepository;
    private final CohortSummaryStatsRepository cohortSummaryStatsRepository;
    private final CohortFeaturesRepository cohortFeaturesRepository;
    private final CohortFeaturesAnalysisRefRepository cohortFeaturesAnalysisRefRepository;
    private final CohortFeaturesDistRepository cohortFeaturesDistRepository;
    private final CohortFeaturesRefRepository cohortFeaturesRefRepository;
    private final CohortGenerationInfoRepository cohortGenerationInfoRepository;
    private final CohortDefinitionRepository cohortDefinitionRepository;
    private final CohortDefinitionService cohortDefinitionService;
    private final LiferayPermissionManager authorizer;
    private final JobExplorer jobExplorer;

    @Autowired
    public CohortGenerationImportService(StepBuilderFactory stepBuilders, JobBuilderFactory jobBuilders,
                                         JobTemplate jobTemplate, CohortRepository cohortRepository,
                                         CohortInclusionRepository cohortInclusionRepository,
                                         CohortInclusionStatsRepository cohortInclusionStatsRepository,
                                         CohortInclusionResultRepository cohortInclusionResultRepository,
                                         CohortSummaryStatsRepository cohortSummaryStatsRepository,
                                         CohortFeaturesRepository cohortFeaturesRepository,
                                         CohortFeaturesAnalysisRefRepository cohortFeaturesAnalysisRefRepository,
                                         CohortFeaturesDistRepository cohortFeaturesDistRepository,
                                         CohortFeaturesRefRepository cohortFeaturesRefRepository,
                                         CohortGenerationInfoRepository cohortGenerationInfoRepository,
                                         CohortDefinitionRepository cohortDefinitionRepository,
                                         CohortDefinitionService cohortDefinitionService,
                                         LiferayPermissionManager authorizer,
                                         JobExplorer jobExplorer) {
        this.stepBuilders = stepBuilders;
        this.jobBuilders = jobBuilders;
        this.jobTemplate = jobTemplate;
        this.cohortRepository = cohortRepository;
        this.cohortInclusionRepository = cohortInclusionRepository;
        this.cohortInclusionStatsRepository = cohortInclusionStatsRepository;
        this.cohortInclusionResultRepository = cohortInclusionResultRepository;
        this.cohortSummaryStatsRepository = cohortSummaryStatsRepository;
        this.cohortFeaturesRepository = cohortFeaturesRepository;
        this.cohortFeaturesAnalysisRefRepository = cohortFeaturesAnalysisRefRepository;
        this.cohortFeaturesDistRepository = cohortFeaturesDistRepository;
        this.cohortFeaturesRefRepository = cohortFeaturesRefRepository;
        this.cohortGenerationInfoRepository = cohortGenerationInfoRepository;
        this.cohortDefinitionRepository = cohortDefinitionRepository;
        this.cohortDefinitionService = cohortDefinitionService;
        this.authorizer = authorizer;
        this.jobExplorer = jobExplorer;
    }

    public JobExecutionResource importCohortGeneration(int cohortDefinitionId,
                                                       CohortGenerationResults cohortGenerationResults,
                                                       String sourceKey) {
        ImportCohortGenerationTasklet
                importCohortGenerationTasklet =
                new ImportCohortGenerationTasklet(getTransactionTemplate(), cohortRepository, cohortInclusionRepository,
                        cohortInclusionStatsRepository, cohortInclusionResultRepository, cohortSummaryStatsRepository,
                        cohortFeaturesRepository, cohortFeaturesAnalysisRefRepository, cohortFeaturesDistRepository,
                        cohortFeaturesRefRepository);

        Step importCohortStep = stepBuilders.get("cohortDefinition.importCohortGeneration")
                .tasklet(importCohortGenerationTasklet)
                .build();

        SimpleJobBuilder importJobBuilder = jobBuilders.get(GENERATE_COHORT).start(importCohortStep);

        importJobBuilder.listener(new ImportJobExecutionListener(cohortGenerationResults, getTransactionTemplate(),
                cohortGenerationInfoRepository, cohortDefinitionRepository, cohortDefinitionService, authorizer));

        return jobTemplate.launch(importJobBuilder.build(),
                getJobParametersBuilder(cohortDefinitionId, sourceKey).toJobParameters());
    }

    public JobParametersBuilder getJobParametersBuilder(int cohortDefinitionId, String sourceKey) {
        JobParametersBuilder parametersBuilder = new JobParametersBuilder();

        parametersBuilder.addString(COHORT_DEFINITION_ID, String.valueOf(cohortDefinitionId));
        parametersBuilder.addString(SOURCE_KEY, sourceKey);

        return parametersBuilder;
    }

    public JobExecution getJobExecution(Long jobExecutionId) {
        return jobExplorer.getJobExecution(jobExecutionId);
    }

    public Optional<JobExecution> getJobExecution(Source source, Integer cohortDefinitionId) {

        return jobExplorer.findRunningJobExecutions(GENERATE_COHORT)
                .stream().filter(e -> {
                    JobParameters parameters = e.getJobParameters();
                    return Objects.equals(parameters.getString(COHORT_DEFINITION_ID), Integer.toString(cohortDefinitionId))
                            && Objects.equals(parameters.getString(SOURCE_KEY), source.getSourceKey());
                }).findFirst();
    }
}
