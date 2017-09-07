/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortcomparison.AttritionResult;
import org.ohdsi.webapi.cohortcomparison.BalanceResult;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysis;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisExecution;
import org.ohdsi.webapi.cohortcomparison.ComparativeCohortAnalysisInfo;
import org.ohdsi.webapi.cohortcomparison.ModelScoreDistributionValue;
import org.ohdsi.webapi.cohortcomparison.OutcomeModel;
import org.ohdsi.webapi.cohortcomparison.PropensityScoreModelCovariate;
import org.ohdsi.webapi.cohortcomparison.PropensityScoreModelReport;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.job.JobTemplate;
import org.ohdsi.webapi.rsb.RSBTasklet;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 * @author Frank DeFalco <fdefalco@ohdsi.org>
 */
@Component
@Path("/comparativecohortanalysis/")
public class ComparativeCohortAnalysisService extends AbstractDaoService {

    @Autowired
    private CohortDefinitionService cohortDefinitionService;

    @Autowired
    private ConceptSetService conceptSetService;

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private JobTemplate jobTemplate;

    @Autowired
    private JobBuilderFactory jobFactory;

    @Autowired
    private StepBuilderFactory stepFactory;

    @Autowired
    private Environment env;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Iterable<ComparativeCohortAnalysis> getComparativeCohortAnalyses() {
        return getComparativeCohortAnalysisRepository().findAll();
    }

   
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComparativeCohortAnalysis createComparativeCohortAnalysis(ComparativeCohortAnalysis comparativeCohortAnalysis) {

      Date d = new Date();
      comparativeCohortAnalysis.setAnalysisId(null);
      comparativeCohortAnalysis.setCreated(d);
      comparativeCohortAnalysis.setModified(d);
      comparativeCohortAnalysis = this.getComparativeCohortAnalysisRepository().save(comparativeCohortAnalysis);
      return comparativeCohortAnalysis;
    }

    @Path("/{id}")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public ComparativeCohortAnalysis updateComparativeCohortAnalysis(@PathParam("id") final int id, ComparativeCohortAnalysis comparativeCohortAnalysis) throws Exception {
        Date d = new Date();
        comparativeCohortAnalysis.setCreated(d); // temporary workaround until client sends the current created value.
        comparativeCohortAnalysis.setModified(d);
        
        comparativeCohortAnalysis = this.getComparativeCohortAnalysisRepository().save(comparativeCohortAnalysis);
        return comparativeCohortAnalysis;
    }
    
    /**
     * Deletes the specified comparative cohort analysis
     * 
     * @param id - the Comparative Cohort Analysis ID to delete
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public void delete(@PathParam("id") final int id) {
     this.getComparativeCohortAnalysisRepository().delete(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/model")
    public ComparativeCohortAnalysis getModel() {
        ComparativeCohortAnalysis cca = new ComparativeCohortAnalysis();
        return cca;
    }
    
    /**
     * @param id - the comparative cohort analysis identifier
     * @param sourceKey - the source database to run this execution against
     * @return job resource information
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/execute/{sourceKey}")
    public JobExecutionResource generateCohort(@PathParam("id") final int id, @PathParam("sourceKey") final String sourceKey) {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
        String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
        String connectionString = source.getSourceConnection();
        String dbms = source.getSourceDialect();

        ComparativeCohortAnalysis cca = getComparativeCohortAnalysisRepository().findOne(id);
        ComparativeCohortAnalysisExecution ccae = new ComparativeCohortAnalysisExecution(cca);
        Date executed = new Date();
        ccae.setSourceId(source.getSourceId());
        ccae.setExecuted(executed);
        ccae.setDuration(0);
        ccae.setExecutionStatus(ComparativeCohortAnalysisExecution.status.RUNNING);
        ccae.setUserId(0);
        getComparativeCohortAnalysisExecutionRepository().save(ccae);
       
        String functionName = "executeComparativeCohortAnalysis";
        HashMap<String, Object> parameters = new HashMap();
        parameters.put("treatmentId", ccae.getTreatmentId());
        parameters.put("comparatorId", ccae.getComparatorId());
        parameters.put("outcomeId", ccae.getOutcomeId());
        parameters.put("modelType", ccae.getModelType());
        parameters.put("executionId", ccae.getExecutionId());
        parameters.put("dbms", dbms);        
        parameters.put("connectionString", connectionString);
        parameters.put("cdmTableQualifier", cdmTableQualifier);
        parameters.put("resultsTableQualifier", resultsTableQualifier);
        parameters.put("timeAtRiskStart", ccae.getTimeAtRiskStart());
        parameters.put("timeAtRiskEnd", ccae.getTimeAtRiskEnd());
        parameters.put("addExposureDaysToEnd", ccae.getAddExposureDaysToEnd());
        parameters.put("minimumWashoutPeriod", ccae.getMinimumWashoutPeriod());
        parameters.put("minimumDaysAtRisk", ccae.getMinimumDaysAtRisk());
        parameters.put("rmSubjectsInBothCohorts", ccae.getRmSubjectsInBothCohorts());
        parameters.put("rmPriorOutcomes", ccae.getRmPriorOutcomes());
        parameters.put("psAdjustment", ccae.getPsAdjustment());
        
        if (ccae.getPsExclusionId() > 0) {
            ConceptSetExpression cse = conceptSetService.getConceptSetExpression(ccae.getPsExclusionId());
            Collection<Long> exclusions = vocabularyService.resolveConceptSetExpression(sourceKey, cse);
            parameters.put("psExclusions" , exclusions);
        }

        if (ccae.getPsExclusionId() > 0) {
            ConceptSetExpression cse = conceptSetService.getConceptSetExpression(ccae.getPsInclusionId());
            Collection<Long> inclusions = vocabularyService.resolveConceptSetExpression(sourceKey, cse);
            parameters.put("psInclusions" , inclusions);
        }
        
        parameters.put("psDemographics", ccae.getPsDemographics());
        parameters.put("psDemographicsGender", ccae.getPsDemographicsGender());
        parameters.put("psDemographicsRace", ccae.getPsDemographicsRace());
        parameters.put("psDemographicsEthnicity", ccae.getPsDemographicsEthnicity());
        parameters.put("psDemographicsAge", ccae.getPsDemographicsAge());
        parameters.put("psDemographicsYear", ccae.getPsDemographicsYear());
        parameters.put("psDemographicsMonth", ccae.getPsDemographicsMonth());
        parameters.put("psTrim", ccae.getPsTrim());
        parameters.put("psTrimFraction", ccae.getPsTrimFraction());
        parameters.put("psMatch", ccae.getPsMatch());
        parameters.put("psMatchMaxRatio", ccae.getPsMatchMaxRatio());
        parameters.put("psStrat", ccae.getPsStrat());
        parameters.put("psStratNumStrata", ccae.getPsStratNumStrata());
        parameters.put("psConditionOcc", ccae.getPsConditionOcc());
        parameters.put("psConditionOcc365d", ccae.getPsConditionOcc365d());
        parameters.put("psConditionOcc30d", ccae.getPsConditionOcc30d());
        parameters.put("psConditionOccInpt180d", ccae.getPsConditionOccInpt180d());
        parameters.put("psConditionEra", ccae.getPsConditionEra());
        parameters.put("psConditionEraEver", ccae.getPsConditionEraEver());
        parameters.put("psConditionEraOverlap", ccae.getPsConditionEraOverlap());
        parameters.put("psConditionGroup", ccae.getPsConditionGroup());
        parameters.put("psConditionGroupMeddra", ccae.getPsConditionGroupMeddra());
        parameters.put("psConditionGroupSnomed", ccae.getPsConditionGroupSnomed());
        parameters.put("psDrugExposure", ccae.getPsDrugExposure());
        parameters.put("psDrugExposure365d", ccae.getPsDrugExposure365d());
        parameters.put("psDrugExposure30d", ccae.getPsDrugEra30d());
        parameters.put("psDrugEra", ccae.getPsDrugEra());
        parameters.put("psDrugEra365d", ccae.getPsDrugEra365d());
        parameters.put("psDrugEra30d", ccae.getPsDrugEra30d());
        parameters.put("psDrugEraOverlap", ccae.getPsDrugEraOverlap());
        parameters.put("psDrugEraEver", ccae.getPsDrugEraEver());
        parameters.put("psDrugGroup", ccae.getPsDrugGroup());
        parameters.put("psProcedureOcc", ccae.getPsProcedureOcc());
        parameters.put("psProcedureOcc365d", ccae.getPsProcedureOcc365d());
        parameters.put("psProcedureOcc30d", ccae.getPsProcedureOcc30d());
        parameters.put("psProcedureGroup", ccae.getPsProcedureGroup());
        parameters.put("psObservation", ccae.getPsObservation());
        parameters.put("psObservation365d", ccae.getPsObservation365d());
        parameters.put("psObservation30d", ccae.getPsObservation30d());
        parameters.put("psObservationCount365d", ccae.getPsObservationCount365d());
        parameters.put("psMeasurement", ccae.getPsMeasurement());
        parameters.put("psMeasurement365d", ccae.getPsMeasurement365d());
        parameters.put("psMeasurement30d", ccae.getPsMeasurement30d());
        parameters.put("psMeasurementCount365d", ccae.getPsMeasurementCount365d());
        parameters.put("psMeasurementBelow", ccae.getPsMeasurementBelow());
        parameters.put("psMeasurementAbove", ccae.getPsMeasurementAbove());
        parameters.put("psConceptCounts", ccae.getPsConceptCounts());
        parameters.put("psRiskScores", ccae.getPsRiskScores());
        parameters.put("psRiskScoresCharlson", ccae.getPsRiskScoresCharlson());
        parameters.put("psRiskScoresDcsi", ccae.getPsRiskScoresDcsi());
        parameters.put("psRiskScoresChads2", ccae.getPsRiskScoresChads2());
        parameters.put("psRiskScoresChads2vasc", ccae.getPsRiskScoresChads2vasc());
        parameters.put("psInteractionYear", ccae.getPsInteractionYear());
        parameters.put("psInteractionMonth", ccae.getPsInteractionMonth());        
        parameters.put("omCovariates", ccae.getOmCovariates());
        
        if (ccae.getOmExclusionId() > 0) {
            ConceptSetExpression cse = conceptSetService.getConceptSetExpression(ccae.getOmExclusionId());
            Collection<Long> exclusions = vocabularyService.resolveConceptSetExpression(sourceKey, cse);
            parameters.put("omExclusions" , exclusions);
        }
        
        if (ccae.getOmInclusionId() > 0) {
            ConceptSetExpression cse = conceptSetService.getConceptSetExpression(ccae.getOmInclusionId());
            Collection<Long> inclusions = vocabularyService.resolveConceptSetExpression(sourceKey, cse);
            parameters.put("omInclusions" , inclusions);
        }        

        parameters.put("omDemographics", ccae.getOmDemographics());
        parameters.put("omDemographicsGender", ccae.getOmDemographicsGender());
        parameters.put("omDemographicsRace", ccae.getOmDemographicsRace());
        parameters.put("omDemographicsEthnicity", ccae.getOmDemographicsEthnicity());
        parameters.put("omDemographicsAge", ccae.getOmDemographicsAge());
        parameters.put("omDemographicsYear", ccae.getOmDemographicsYear());
        parameters.put("omDemographicsMonth", ccae.getOmDemographicsMonth());
        parameters.put("omTrim", ccae.getOmTrim());
        parameters.put("omTrimFraction", ccae.getOmTrimFraction());
        parameters.put("omMatch", ccae.getOmMatch());
        parameters.put("omMatchMaxRatio", ccae.getOmMatchMaxRatio());
        parameters.put("omStrat", ccae.getOmStrat());
        parameters.put("omStratNumStrata", ccae.getOmStratNumStrata());
        parameters.put("omConditionOcc", ccae.getOmConditionOcc());
        parameters.put("omConditionOcc365d", ccae.getOmConditionOcc365d());
        parameters.put("omConditionOcc30d", ccae.getOmConditionOcc30d());
        parameters.put("omConditionOccInpt180d", ccae.getOmConditionOccInpt180d());
        parameters.put("omConditionEra", ccae.getOmConditionEra());
        parameters.put("omConditionEraEver", ccae.getOmConditionEraEver());
        parameters.put("omConditionEraOverlap", ccae.getOmConditionEraOverlap());
        parameters.put("omConditionGroup", ccae.getOmConditionGroup());
        parameters.put("omConditionGroupMeddra", ccae.getOmConditionGroupMeddra());
        parameters.put("omConditionGroupSnomed", ccae.getOmConditionGroupSnomed());
        parameters.put("omDrugExposure", ccae.getOmDrugExposure());
        parameters.put("omDrugExposure365d", ccae.getOmDrugExposure365d());
        parameters.put("omDrugExposure30d", ccae.getOmDrugEra30d());
        parameters.put("omDrugEra", ccae.getOmDrugEra());
        parameters.put("omDrugEra365d", ccae.getOmDrugEra365d());
        parameters.put("omDrugEra30d", ccae.getOmDrugEra30d());
        parameters.put("omDrugEraOverlap", ccae.getOmDrugEraOverlap());
        parameters.put("omDrugEraEver", ccae.getOmDrugEraEver());
        parameters.put("omDrugGroup", ccae.getOmDrugGroup());
        parameters.put("omProcedureOcc", ccae.getOmProcedureOcc());
        parameters.put("omProcedureOcc365d", ccae.getOmProcedureOcc365d());
        parameters.put("omProcedureOcc30d", ccae.getOmProcedureOcc30d());
        parameters.put("omProcedureGroup", ccae.getOmProcedureGroup());
        parameters.put("omObservation", ccae.getOmObservation());
        parameters.put("omObservation365d", ccae.getOmObservation365d());
        parameters.put("omObservation30d", ccae.getOmObservation30d());
        parameters.put("omObservationCount365d", ccae.getOmObservationCount365d());
        parameters.put("omMeasurement", ccae.getOmMeasurement());
        parameters.put("omMeasurement365d", ccae.getOmMeasurement365d());
        parameters.put("omMeasurement30d", ccae.getOmMeasurement30d());
        parameters.put("omMeasurementCount365d", ccae.getOmMeasurementCount365d());
        parameters.put("omMeasurementBelow", ccae.getOmMeasurementBelow());
        parameters.put("omMeasurementAbove", ccae.getOmMeasurementAbove());
        parameters.put("omConceptCounts", ccae.getOmConceptCounts());
        parameters.put("omRiskScores", ccae.getOmRiskScores());
        parameters.put("omRiskScoresCharlson", ccae.getOmRiskScoresCharlson());
        parameters.put("omRiskScoresDcsi", ccae.getOmRiskScoresDcsi());
        parameters.put("omRiskScoresChads2", ccae.getOmRiskScoresChads2());
        parameters.put("omRiskScoresChads2vasc", ccae.getOmRiskScoresChads2vasc());
        parameters.put("omInteractionYear", ccae.getOmInteractionYear());
        parameters.put("omInteractionMonth", ccae.getOmInteractionMonth());

        parameters.put("delCovariatesSmallCount", ccae.getDelCovariatesSmallCount());
        
        if (ccae.getNegativeControlId() > 0) {
            ConceptSetExpression cse = conceptSetService.getConceptSetExpression(ccae.getNegativeControlId());
            Collection<Long> inclusions = vocabularyService.resolveConceptSetExpression(sourceKey, cse);
            parameters.put("negativeControls" , inclusions);
        }          
        
        RSBTasklet t = new RSBTasklet(getComparativeCohortAnalysisExecutionRepository());
        t.setFunctionName(functionName);
        t.setParameters(parameters);
        t.setExecutionId(ccae.getExecutionId());

        String rServiceHost = env.getRequiredProperty("r.serviceHost");
        t.setRServiceHost(rServiceHost);
        Step executeRSBStep = stepFactory.get("rsbTask")
                .tasklet(t)
                .build();

        JobParametersBuilder builder = new JobParametersBuilder();
        builder.addString("jobName", "executing cohort comparison on " + ccae.getSourceId());
        JobParameters jobParameters = builder.toJobParameters();
        Job executeRSBJob = jobFactory.get("executeRSB")
                .start(executeRSBStep)
                .build();
        JobExecutionResource jer = jobTemplate.launch(executeRSBJob, jobParameters);
        return jer;
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public ComparativeCohortAnalysisInfo getComparativeCohortAnalysis(@PathParam("id") int id) {
        ComparativeCohortAnalysis analysis = this.getComparativeCohortAnalysisRepository().findOne(id);
        ComparativeCohortAnalysisInfo info = new ComparativeCohortAnalysisInfo(analysis);

        if (analysis.getComparatorId() > 0) {
            try {
                CohortDefinitionDTO cd = cohortDefinitionService.getCohortDefinition(analysis.getComparatorId());
                info.setComparatorCaption(cd.name);
                info.setComparatorCohortDefinition(cd.expression);
            } catch (Exception e) {
                // Cohort definition no longer exists 
                log.debug("Cohort definition id = " + info.getComparatorId() + " no longer exists");
            }
        }
        if (analysis.getTreatmentId() > 0) {
            try {
                CohortDefinitionDTO cd = cohortDefinitionService.getCohortDefinition(analysis.getTreatmentId());
                info.setTreatmentCaption(cd.name);
                info.setTreatmentCohortDefinition(cd.expression);
            } catch (Exception e) {
                // Cohort definition no longer exists 
                log.debug("Cohort definition id = " + info.getTreatmentId() + " no longer exists");
            }
        }
        if (analysis.getOutcomeId() > 0) {
            try {
                CohortDefinitionDTO cd = cohortDefinitionService.getCohortDefinition(analysis.getOutcomeId());
                info.setOutcomeCaption(cd.name);
                info.setOutcomeCohortDefinition(cd.expression);            
            } catch (Exception e) {
                // Cohort definition no longer exists 
                log.debug("Cohort definition id = " + info.getOutcomeId() + " no longer exists");
            }
        }
        if (analysis.getPsInclusionId() > 0) {
            try {
                info.setPsInclusionCaption(conceptSetService.getConceptSet(analysis.getPsInclusionId()).getName());
                info.setPsInclusionConceptSet(conceptSetService.getConceptSetExpression(analysis.getPsInclusionId()));
                info.setPsInclusionConceptSetSql(vocabularyService.getConceptSetExpressionSQL(info.getPsInclusionConceptSet()));                
            } catch (Exception e) {
                log.debug("Concept set id = " + info.getPsInclusionId() + " no longer exists");
            }
        }
        if (analysis.getPsExclusionId() > 0) {
            try {
                info.setPsExclusionCaption(conceptSetService.getConceptSet(analysis.getPsExclusionId()).getName());
                info.setPsExclusionConceptSet(conceptSetService.getConceptSetExpression(analysis.getPsExclusionId()));
                info.setPsExclusionConceptSetSql(vocabularyService.getConceptSetExpressionSQL(info.getPsExclusionConceptSet()));                
            } catch (Exception e) {
                log.debug("Concept set id = " + info.getPsExclusionId() + " no longer exists");
            }
        }
        if (analysis.getOmInclusionId() > 0) {
            try {
                info.setOmInclusionCaption(conceptSetService.getConceptSet(analysis.getOmInclusionId()).getName());
                info.setOmInclusionConceptSet(conceptSetService.getConceptSetExpression(analysis.getOmInclusionId()));
                info.setOmInclusionConceptSetSql(vocabularyService.getConceptSetExpressionSQL(info.getOmInclusionConceptSet()));                
            } catch (Exception e) {
                log.debug("Concept set id = " + info.getOmInclusionId() + " no longer exists");               
            }
        }
        if (analysis.getOmExclusionId() > 0) {
            try {
                info.setOmExclusionCaption(conceptSetService.getConceptSet(analysis.getOmExclusionId()).getName());
                info.setOmExclusionConceptSet(conceptSetService.getConceptSetExpression(analysis.getOmExclusionId()));
                info.setOmExclusionConceptSetSql(vocabularyService.getConceptSetExpressionSQL(info.getOmExclusionConceptSet()));                
            } catch (Exception e) {
                log.debug("Concept set id = " + info.getOmExclusionId() + " no longer exists");               
            }
        }
        if (analysis.getNegativeControlId() > 0) {
            try {
                info.setNegativeControlCaption(conceptSetService.getConceptSet(analysis.getNegativeControlId()).getName());
                info.setNegativeControlConceptSet(conceptSetService.getConceptSetExpression(analysis.getNegativeControlId()));
                info.setNegativeControlConceptSetSql(vocabularyService.getConceptSetExpressionSQL(info.getNegativeControlConceptSet()));
            } catch (Exception e) {
                log.debug("Concept set id = " + info.getNegativeControlId() + " no longer exists");               
            }
        }
      
        return info;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("{id}/executions")
    public Iterable<ComparativeCohortAnalysisExecution> getComparativeCohortAnalysisExecutions(@PathParam("id") int comparativeCohortAnalysisId) {
        return getComparativeCohortAnalysisExecutionRepository().findAllByAnalysisId(comparativeCohortAnalysisId);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("execution/{eid}")
    public ComparativeCohortAnalysisExecution getComparativeCohortAnalysisExecution(@PathParam("eid") int executionId) {
        return getComparativeCohortAnalysisExecutionRepository().findByExecutionId(executionId);
    }

    private final RowMapper<PropensityScoreModelCovariate> covariateMapper = new RowMapper<PropensityScoreModelCovariate>() {
        @Override
        public PropensityScoreModelCovariate mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
            long id = resultSet.getLong("id");
            float coefficient = resultSet.getFloat("coefficient");
            String covariateName = resultSet.getString("covariate_name");

            PropensityScoreModelCovariate covariate = new PropensityScoreModelCovariate();
            covariate.setId(id);
            covariate.setName(covariateName);
            covariate.setValue(coefficient);
            return covariate;
        }
    };

    private final RowMapper<ModelScoreDistributionValue> ScoreDistributionMapper = new RowMapper<ModelScoreDistributionValue>() {
        @Override
        public ModelScoreDistributionValue mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
            float score = resultSet.getFloat("score");
            int treatment = resultSet.getInt("treatment");
            int comparator = resultSet.getInt("comparator");

            ModelScoreDistributionValue distributionValue = new ModelScoreDistributionValue();
            distributionValue.score = score;
            distributionValue.treatment = treatment;
            distributionValue.comparator = comparator;

            return distributionValue;
        }
    };

    private final RowMapper<Float> aucMapper = new RowMapper<Float>() {
        @Override
        public Float mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
            Float value = resultSet.getFloat("auc");
            return value;
        }
    };

    private final RowMapper<AttritionResult> attritionMapper = new RowMapper<AttritionResult>() {
        @Override
        public AttritionResult mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
            AttritionResult attritionResult = new AttritionResult();
            attritionResult.attritionOrder = resultSet.getInt("attrition_order");
            attritionResult.comparatorExposures = resultSet.getInt("comparator_exposures");
            attritionResult.comparatorPersons = resultSet.getInt("comparator_persons");
            attritionResult.treatedExposures = resultSet.getInt("treated_exposures");
            attritionResult.treatedPersons = resultSet.getInt("treated_persons");
            attritionResult.description = resultSet.getString("description");
            return attritionResult;
        }
    };
    
    private final RowMapper<BalanceResult> balanceMapper = new RowMapper<BalanceResult>() {
        @Override
        public BalanceResult mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
            BalanceResult balanceResult = new BalanceResult();
            balanceResult.covariateId = resultSet.getInt("covariate_id");
            balanceResult.conceptId = resultSet.getInt("concept_id");
            balanceResult.covariateName = resultSet.getString("covariate_name");
            balanceResult.beforeMatchingStdDiff = resultSet.getFloat("before_matching_std_diff");
            balanceResult.afterMatchingStdDiff = resultSet.getFloat("after_matching_std_diff");
            return balanceResult;
        }
    };    
    
    private final RowMapper<OutcomeModel> outcomeModelMapper = new RowMapper<OutcomeModel>() {
        @Override
        public OutcomeModel mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
            OutcomeModel om = new OutcomeModel();
            om.comparatorId = resultSet.getInt("comparator_id");
            om.treatmentId = resultSet.getInt("treatment_id");
            om.outcomeId = resultSet.getInt("outcome_id");
            om.estimate = resultSet.getFloat("estimate");
            om.lower95 = resultSet.getFloat("lower95");
            om.upper95 = resultSet.getFloat("upper95");
            om.logRr = resultSet.getFloat("log_rr");
            om.seLogRr = resultSet.getFloat("se_log_rr");
            return om;
        }
    };    

    @GET
    @Path("execution/{eid}/attrition")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<AttritionResult> getAttritionResults(@PathParam("eid") int executionId) {
        ComparativeCohortAnalysisExecution ccae = getComparativeCohortAnalysisExecution(executionId);
        Source source = getSourceRepository().findBySourceId(ccae.getSourceId());
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        String sqlAttrition = ResourceHelper.GetResourceAsString("/resources/cohortcomparison/sql/attrition.sql");
        sqlAttrition = SqlRender.renderSql(sqlAttrition, new String[]{"resultsTableQualifier", "executionId"}, new String[]{
            tableQualifier, Integer.toString(executionId)});
        sqlAttrition = SqlTranslate.translateSql(sqlAttrition, "sql server", source.getSourceDialect());
        return getSourceJdbcTemplate(source).query(sqlAttrition, attritionMapper);
    }
    
    @GET
    @Path("execution/{eid}/balance")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<BalanceResult> getBalanceResults(@PathParam("eid") int executionId) {
        ComparativeCohortAnalysisExecution ccae = getComparativeCohortAnalysisExecution(executionId);
        Source source = getSourceRepository().findBySourceId(ccae.getSourceId());
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        String sqlBalance = ResourceHelper.GetResourceAsString("/resources/cohortcomparison/sql/balance.sql");
        sqlBalance = SqlRender.renderSql(sqlBalance, new String[]{"resultsTableQualifier", "executionId"}, new String[]{
            tableQualifier, Integer.toString(executionId)});
        sqlBalance = SqlTranslate.translateSql(sqlBalance, "sql server", source.getSourceDialect());
        return getSourceJdbcTemplate(source).query(sqlBalance, balanceMapper);
    }    

    @GET
    @Path("execution/{eid}/om")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<OutcomeModel> getOutcomeModel(@PathParam("eid") int executionId) {
        ComparativeCohortAnalysisExecution ccae = getComparativeCohortAnalysisExecution(executionId);
        Source source = getSourceRepository().findBySourceId(ccae.getSourceId());
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        String sqlDist = ResourceHelper.GetResourceAsString("/resources/cohortcomparison/sql/outcome_model.sql");
        sqlDist = SqlRender.renderSql(sqlDist, new String[]{"resultsTableQualifier", "executionId"}, new String[]{
            tableQualifier, Integer.toString(executionId)});
        sqlDist = SqlTranslate.translateSql(sqlDist, "sql server", source.getSourceDialect());
        return getSourceJdbcTemplate(source).query(sqlDist, outcomeModelMapper);
    }
    
    @GET
    @Path("execution/{eid}/psmodelpropscore")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ModelScoreDistributionValue> getPsModelPropScore(@PathParam("eid") int executionId) {
        ComparativeCohortAnalysisExecution ccae = getComparativeCohortAnalysisExecution(executionId);
        Source source = getSourceRepository().findBySourceId(ccae.getSourceId());
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        String sqlDist = ResourceHelper.GetResourceAsString("/resources/cohortcomparison/sql/psmodel_prop_score.sql");
        sqlDist = SqlRender.renderSql(sqlDist, new String[]{"resultsTableQualifier", "executionId"}, new String[]{
            tableQualifier, Integer.toString(executionId)});
        sqlDist = SqlTranslate.translateSql(sqlDist, "sql server", source.getSourceDialect());
        return getSourceJdbcTemplate(source).query(sqlDist, ScoreDistributionMapper);
    }
    
    @GET
    @Path("execution/{eid}/psmodelprefscore")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ModelScoreDistributionValue> getPsModelPrefScore(@PathParam("eid") int executionId) {
        ComparativeCohortAnalysisExecution ccae = getComparativeCohortAnalysisExecution(executionId);
        Source source = getSourceRepository().findBySourceId(ccae.getSourceId());
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        String sqlDist = ResourceHelper.GetResourceAsString("/resources/cohortcomparison/sql/psmodel_pref_score.sql");
        sqlDist = SqlRender.renderSql(sqlDist, new String[]{"resultsTableQualifier", "executionId"}, new String[]{
            tableQualifier, Integer.toString(executionId)});
        sqlDist = SqlTranslate.translateSql(sqlDist, "sql server", source.getSourceDialect());
        return getSourceJdbcTemplate(source).query(sqlDist, ScoreDistributionMapper);
    }    

    @GET
    @Path("execution/{eid}/poppropdist")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<ModelScoreDistributionValue> getPopPropDistribution(@PathParam("eid") int executionId) {
        ComparativeCohortAnalysisExecution ccae = getComparativeCohortAnalysisExecution(executionId);
        Source source = getSourceRepository().findBySourceId(ccae.getSourceId());
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        String sqlDist = ResourceHelper.GetResourceAsString("/resources/cohortcomparison/sql/pop_prop_score.sql");
        sqlDist = SqlRender.renderSql(sqlDist, new String[]{"resultsTableQualifier", "executionId"}, new String[]{
            tableQualifier, Integer.toString(executionId)});
        sqlDist = SqlTranslate.translateSql(sqlDist, "sql server", source.getSourceDialect());
        return getSourceJdbcTemplate(source).query(sqlDist, ScoreDistributionMapper);
    }

    @GET
    @Path("execution/{eid}/psmodel")
    @Produces(MediaType.APPLICATION_JSON)
    public PropensityScoreModelReport getPropensityScoreModelReport(@PathParam("eid") int executionId) {
        ComparativeCohortAnalysisExecution ccae = getComparativeCohortAnalysisExecution(executionId);

        Source source = getSourceRepository().findBySourceId(ccae.getSourceId());
        String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);

        String sqlAuc = ResourceHelper.GetResourceAsString("/resources/cohortcomparison/sql/auc.sql");
        sqlAuc = SqlRender.renderSql(sqlAuc, new String[]{"resultsTableQualifier", "executionId"}, new String[]{
            tableQualifier, Integer.toString(executionId)});
        sqlAuc = SqlTranslate.translateSql(sqlAuc, "sql server", source.getSourceDialect());
        // TODO - why was object mapper throwing an error for the single value we are trying to retrieve
        Float auc = getSourceJdbcTemplate(source).query(sqlAuc, aucMapper).get(0);

        String sqlPsmodel = ResourceHelper.GetResourceAsString("/resources/cohortcomparison/sql/psmodel.sql");
        sqlPsmodel = SqlRender.renderSql(sqlPsmodel, new String[]{"resultsTableQualifier", "executionId"}, new String[]{
            tableQualifier, Integer.toString(executionId)});
        sqlPsmodel = SqlTranslate.translateSql(sqlPsmodel, "sql server", source.getSourceDialect());
        ArrayList<PropensityScoreModelCovariate> covariates = (ArrayList<PropensityScoreModelCovariate>) getSourceJdbcTemplate(source).query(sqlPsmodel, covariateMapper);

        PropensityScoreModelReport psmr = new PropensityScoreModelReport();
        psmr.setAuc(auc);
        psmr.setCovariates(covariates);
        return psmr;
    }
}
