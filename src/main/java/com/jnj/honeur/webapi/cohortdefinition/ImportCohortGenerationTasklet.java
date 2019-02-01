package com.jnj.honeur.webapi.cohortdefinition;

import com.google.common.collect.Lists;
import com.jnj.honeur.webapi.SourceDaimonContextHolder;
import com.jnj.honeur.webapi.cohortfeatures.CohortFeaturesEntity;
import com.jnj.honeur.webapi.cohortfeatures.CohortFeaturesRepository;
import com.jnj.honeur.webapi.cohortfeaturesanalysisref.CohortFeaturesAnalysisRefEntity;
import com.jnj.honeur.webapi.cohortfeaturesanalysisref.CohortFeaturesAnalysisRefRepository;
import com.jnj.honeur.webapi.cohortfeaturesdist.CohortFeaturesDistEntity;
import com.jnj.honeur.webapi.cohortfeaturesdist.CohortFeaturesDistRepository;
import com.jnj.honeur.webapi.cohortfeaturesref.CohortFeaturesRefEntity;
import com.jnj.honeur.webapi.cohortfeaturesref.CohortFeaturesRefRepository;
import com.jnj.honeur.webapi.cohortinclusion.CohortInclusionEntity;
import com.jnj.honeur.webapi.cohortinclusion.CohortInclusionRepository;
import com.jnj.honeur.webapi.cohortinclusionresult.CohortInclusionResultEntity;
import com.jnj.honeur.webapi.cohortinclusionresult.CohortInclusionResultRepository;
import com.jnj.honeur.webapi.cohortinclusionstats.CohortInclusionStatsEntity;
import com.jnj.honeur.webapi.cohortinclusionstats.CohortInclusionStatsRepository;
import com.jnj.honeur.webapi.cohortsummarystats.CohortSummaryStatsEntity;
import com.jnj.honeur.webapi.cohortsummarystats.CohortSummaryStatsRepository;
import com.jnj.honeur.webapi.source.SourceDaimonContext;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cohort.CohortEntity;
import org.ohdsi.webapi.cohort.CohortRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfoRepository;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.StoppableTasklet;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ImportCohortGenerationTasklet implements StoppableTasklet {

    private TransactionTemplate transactionTemplate;
    private CohortRepository cohortRepository;
    private CohortInclusionRepository cohortInclusionRepository;
    private CohortInclusionStatsRepository cohortInclusionStatsRepository;
    private CohortInclusionResultRepository cohortInclusionResultRepository;
    private CohortSummaryStatsRepository cohortSummaryStatsRepository;
    private CohortFeaturesRepository cohortFeaturesRepository;
    private CohortFeaturesAnalysisRefRepository cohortFeaturesAnalysisRefRepository;
    private CohortFeaturesDistRepository cohortFeaturesDistRepository;
    private CohortFeaturesRefRepository cohortFeaturesRefRepository;
    private boolean stopped = false;

    public ImportCohortGenerationTasklet(TransactionTemplate transactionTemplate, CohortRepository cohortRepository,
                                         CohortInclusionRepository cohortInclusionRepository,
                                         CohortInclusionStatsRepository cohortInclusionStatsRepository,
                                         CohortInclusionResultRepository cohortInclusionResultRepository,
                                         CohortSummaryStatsRepository cohortSummaryStatsRepository,
                                         CohortFeaturesRepository cohortFeaturesRepository,
                                         CohortFeaturesAnalysisRefRepository cohortFeaturesAnalysisRefRepository,
                                         CohortFeaturesDistRepository cohortFeaturesDistRepository,
                                         CohortFeaturesRefRepository cohortFeaturesRefRepository) {
        this.transactionTemplate = transactionTemplate;
        this.cohortRepository = cohortRepository;
        this.cohortInclusionRepository = cohortInclusionRepository;
        this.cohortInclusionStatsRepository = cohortInclusionStatsRepository;
        this.cohortInclusionResultRepository = cohortInclusionResultRepository;
        this.cohortSummaryStatsRepository = cohortSummaryStatsRepository;
        this.cohortFeaturesRepository = cohortFeaturesRepository;
        this.cohortFeaturesAnalysisRefRepository = cohortFeaturesAnalysisRefRepository;
        this.cohortFeaturesDistRepository = cohortFeaturesDistRepository;
        this.cohortFeaturesRefRepository = cohortFeaturesRefRepository;
    }

    private int[] doTask(ChunkContext chunkContext) {

        int[] result = new int[0];

        Map<String, Object> jobParams = chunkContext.getStepContext().getJobParameters();
        Integer defId = Integer.valueOf(jobParams.get(Constants.Params.COHORT_DEFINITION_ID).toString());
        String sourceKey = jobParams.get(Constants.Params.SOURCE_KEY).toString();

        SourceDaimonContextHolder.setCurrentSourceDaimonContext(new SourceDaimonContext(sourceKey, SourceDaimon.DaimonType.Results));

        DefaultTransactionDefinition completeTx = new DefaultTransactionDefinition();
        completeTx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionStatus completeStatus = this.transactionTemplate.getTransactionManager().getTransaction(completeTx);

        CohortGenerationResults cohortGenerationResults =
                (CohortGenerationResults) chunkContext.getStepContext().getJobExecutionContext()
                        .get(ImportJobExecutionListener.COHORT_GENERATION_RESULTS);


        importCohortGenerationResults(defId, cohortGenerationResults);
        if(stopped){
            return result;
        }

		this.transactionTemplate.getTransactionManager().commit(completeStatus);

        SourceDaimonContextHolder.clear();

        return result;
    }

    CohortGenerationResults importCohortGenerationResults(int id, CohortGenerationResults cohortGenerationResults) {
        CohortGenerationResults newResults = new CohortGenerationResults();

        List<CohortEntity> cohortEntities = new ArrayList<>();
        for (CohortEntity cohort : cohortGenerationResults.getCohort()) {
            if(stopped){
                return newResults;
            }
            CohortEntity cohortEntity = new CohortEntity();
            cohortEntity.setCohortDefinitionId((long) id);
            cohortEntity.setCohortEndDate(cohort.getCohortEndDate());
            cohortEntity.setCohortStartDate(cohort.getCohortStartDate());
            cohortEntity.setSubjectId(cohort.getSubjectId());
            cohortEntities.add(cohortEntity);
        }
        newResults.setCohort(Lists.newArrayList(cohortRepository.save(cohortEntities)));

        List<CohortInclusionEntity> cohortInclusionEntities = new ArrayList<>();
        for (CohortInclusionEntity cohortInclusion : cohortGenerationResults.getCohortInclusion()) {
            if(stopped){
                return newResults;
            }
            CohortInclusionEntity cohortInclusionEntity = new CohortInclusionEntity();
            cohortInclusionEntity.setCohortDefinitionId((long) id);
            cohortInclusionEntity.setDescription(cohortInclusion.getDescription());
            cohortInclusionEntity.setName(cohortInclusion.getName());
            cohortInclusionEntity.setRuleSequence(cohortInclusion.getRuleSequence());
            cohortInclusionEntities.add(cohortInclusionEntity);
        }
        newResults.setCohortInclusion(Lists.newArrayList(cohortInclusionRepository.save(cohortInclusionEntities)));

        List<CohortInclusionResultEntity> cohortInclusionResultEntities = new ArrayList<>();
        for (CohortInclusionResultEntity cohortInclusionResult : cohortGenerationResults.getCohortInclusionResult()) {
            if(stopped){
                return newResults;
            }
            CohortInclusionResultEntity cohortInclusionResultEntity = new CohortInclusionResultEntity();
            cohortInclusionResultEntity.setCohortDefinitionId((long) id);
            cohortInclusionResultEntity.setInclusionRuleMask(cohortInclusionResult.getInclusionRuleMask());
            cohortInclusionResultEntity.setPersonCount(cohortInclusionResult.getPersonCount());
            cohortInclusionResultEntities.add(cohortInclusionResultEntity);
        }
        newResults.setCohortInclusionResult(
                Lists.newArrayList(cohortInclusionResultRepository.save(cohortInclusionResultEntities)));

        List<CohortInclusionStatsEntity> cohortInclusionStatsList = new ArrayList<>();
        for (CohortInclusionStatsEntity cohortInclusionStats : cohortGenerationResults.getCohortInclusionStats()) {
            if(stopped){
                return newResults;
            }
            CohortInclusionStatsEntity cohortInclusionStatsEntity = new CohortInclusionStatsEntity();
            cohortInclusionStatsEntity.setCohortDefinitionId((long) id);
            cohortInclusionStatsEntity.setGainCount(cohortInclusionStats.getGainCount());
            cohortInclusionStatsEntity.setPersonCount(cohortInclusionStats.getPersonCount());
            cohortInclusionStatsEntity.setPersonTotal(cohortInclusionStats.getPersonTotal());
            cohortInclusionStatsEntity.setRuleSequence(cohortInclusionStats.getRuleSequence());
            cohortInclusionStatsList.add(cohortInclusionStatsEntity);
        }
        newResults.setCohortInclusionStats(
                Lists.newArrayList(cohortInclusionStatsRepository.save(cohortInclusionStatsList)));

        List<CohortSummaryStatsEntity> cohortSummaryStatsList = new ArrayList<>();
        for (CohortSummaryStatsEntity cohortSummaryStats : cohortGenerationResults.getCohortSummaryStats()) {
            if(stopped){
                return newResults;
            }
            CohortSummaryStatsEntity cohortSummaryStatsEntity = new CohortSummaryStatsEntity();
            cohortSummaryStatsEntity.setCohortDefinitionId((long) id);
            cohortSummaryStatsEntity.setBaseCount(cohortSummaryStats.getBaseCount());
            cohortSummaryStatsEntity.setFinalCount(cohortSummaryStats.getFinalCount());
            cohortSummaryStatsList.add(cohortSummaryStatsEntity);
        }
        newResults.setCohortSummaryStats(Lists.newArrayList(cohortSummaryStatsRepository.save(cohortSummaryStatsList)));

        if (cohortGenerationResults.getCohortGenerationInfo().isIncludeFeatures()) {
            List<CohortFeaturesEntity> cohortFeaturesEntities = new ArrayList<>();
            for (CohortFeaturesEntity cohortFeatures : cohortGenerationResults.getCohortFeatures()) {
                if(stopped){
                    return newResults;
                }
                CohortFeaturesEntity cohortFeaturesEntity = new CohortFeaturesEntity();
                cohortFeaturesEntity.setCohortDefinitionId((long) id);
                cohortFeaturesEntity.setAverageValue(cohortFeatures.getAverageValue());
                cohortFeaturesEntity.setCovariateId(cohortFeatures.getCovariateId());
                cohortFeaturesEntity.setSumValue(cohortFeatures.getSumValue());
                cohortFeaturesEntities.add(cohortFeaturesEntity);
            }
            newResults.setCohortFeatures(Lists.newArrayList(cohortFeaturesRepository.save(cohortFeaturesEntities)));

            List<CohortFeaturesAnalysisRefEntity> cohortFeaturesAnalysisRefEntities = new ArrayList<>();
            for (CohortFeaturesAnalysisRefEntity cohortFeaturesAnalysisRef : cohortGenerationResults
                    .getCohortFeaturesAnalysisRef()) {
                if(stopped){
                    return newResults;
                }
                CohortFeaturesAnalysisRefEntity cohortFeaturesAnalysisRefEntity = new CohortFeaturesAnalysisRefEntity();
                cohortFeaturesAnalysisRefEntity.setCohortDefinitionId((long) id);
                cohortFeaturesAnalysisRefEntity.setAnalysisId(cohortFeaturesAnalysisRef.getAnalysisId());
                cohortFeaturesAnalysisRefEntity.setAnalysisName(cohortFeaturesAnalysisRef.getAnalysisName());
                cohortFeaturesAnalysisRefEntity.setBinary(cohortFeaturesAnalysisRef.getBinary());
                cohortFeaturesAnalysisRefEntity.setDomainId(cohortFeaturesAnalysisRef.getDomainId());
                cohortFeaturesAnalysisRefEntity.setEndDay(cohortFeaturesAnalysisRef.getEndDay());
                cohortFeaturesAnalysisRefEntity.setMissingMeansZero(cohortFeaturesAnalysisRef.getMissingMeansZero());
                cohortFeaturesAnalysisRefEntity.setStartDay(cohortFeaturesAnalysisRef.getStartDay());
                cohortFeaturesAnalysisRefEntities.add(cohortFeaturesAnalysisRefEntity);
            }
            newResults.setCohortFeaturesAnalysisRef(
                    Lists.newArrayList(cohortFeaturesAnalysisRefRepository.save(cohortFeaturesAnalysisRefEntities)));

            List<CohortFeaturesDistEntity> cohortFeaturesDistEntities = new ArrayList<>();
            for (CohortFeaturesDistEntity cohortFeaturesDist : cohortGenerationResults.getCohortFeaturesDist()) {
                if(stopped){
                    return newResults;
                }
                CohortFeaturesDistEntity cohortFeaturesDistEntity = new CohortFeaturesDistEntity();
                cohortFeaturesDistEntity.setCohortDefinitionId((long) id);
                cohortFeaturesDistEntity.setCovariateId(cohortFeaturesDist.getCovariateId());
                cohortFeaturesDistEntity.setCountValue(cohortFeaturesDist.getCountValue());
                cohortFeaturesDistEntity.setMinValue(cohortFeaturesDist.getMinValue());
                cohortFeaturesDistEntity.setMaxValue(cohortFeaturesDist.getMaxValue());
                cohortFeaturesDistEntity.setAverageValue(cohortFeaturesDist.getAverageValue());
                cohortFeaturesDistEntity.setStandardDeviation(cohortFeaturesDist.getStandardDeviation());
                cohortFeaturesDistEntity.setMedianValue(cohortFeaturesDist.getMedianValue());
                cohortFeaturesDistEntity.setP10Value(cohortFeaturesDist.getP10Value());
                cohortFeaturesDistEntity.setP25Value(cohortFeaturesDist.getP25Value());
                cohortFeaturesDistEntity.setP75Value(cohortFeaturesDist.getP75Value());
                cohortFeaturesDistEntity.setP90Value(cohortFeaturesDist.getP90Value());
                cohortFeaturesDistEntities.add(cohortFeaturesDistEntity);
            }
            newResults.setCohortFeaturesDist(
                    Lists.newArrayList(cohortFeaturesDistRepository.save(cohortFeaturesDistEntities)));

            List<CohortFeaturesRefEntity> cohortFeaturesRefEntities = new ArrayList<>();
            for (CohortFeaturesRefEntity cohortFeaturesRef : cohortGenerationResults.getCohortFeaturesRef()) {
                if(stopped){
                    return newResults;
                }
                CohortFeaturesRefEntity cohortFeaturesRefEntity = new CohortFeaturesRefEntity();
                cohortFeaturesRefEntity.setCohortDefinitionId((long) id);
                cohortFeaturesRefEntity.setCovariateId(cohortFeaturesRef.getCovariateId());
                cohortFeaturesRefEntity.setCovariateName(cohortFeaturesRef.getCovariateName());
                cohortFeaturesRefEntity.setAnalysisId(cohortFeaturesRef.getAnalysisId());
                cohortFeaturesRefEntity.setConceptId(cohortFeaturesRef.getConceptId());
                cohortFeaturesRefEntities.add(cohortFeaturesRefEntity);
            }
            newResults.setCohortFeaturesRef(
                    Lists.newArrayList(cohortFeaturesRefRepository.save(cohortFeaturesRefEntities)));
        }
        return newResults;
    }

    @Override
    public RepeatStatus execute(StepContribution stepContribution, ChunkContext chunkContext) throws Exception {

        final int[] ret = this.transactionTemplate.execute(new TransactionCallback<int[]>() {
            @Override
            public int[] doInTransaction(final TransactionStatus status) {
                return doTask(chunkContext);
            }
        });
        if (this.stopped) {
            stepContribution.setExitStatus(ExitStatus.STOPPED);
        }
        return RepeatStatus.FINISHED;
    }

    @Override
    public void stop() {
        this.stopped = true;
    }
}
