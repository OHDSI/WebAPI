package org.ohdsi.webapi.prediction;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.common.DesignImportService;
import org.ohdsi.webapi.common.generation.AnalysisExecutionSupport;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.conceptset.ConceptSetCrossReferenceImpl;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.featureextraction.specification.CovariateSettingsImpl;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.prediction.domain.PredictionGenerationEntity;
import org.ohdsi.webapi.prediction.repository.PredictionAnalysisGenerationRepository;
import org.ohdsi.webapi.prediction.repository.PredictionAnalysisRepository;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.util.EntityUtils;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.InternalServerErrorException;
import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.GENERATE_PREDICTION_ANALYSIS;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Params.PREDICTION_ANALYSIS_ID;
import static org.ohdsi.webapi.Constants.Params.PREDICTION_SKELETON_VERSION;

@Service
@Transactional
public class PredictionServiceImpl extends AnalysisExecutionSupport implements PredictionService, GeneratesNotification {

    private static final EntityGraph DEFAULT_ENTITY_GRAPH = EntityGraphUtils.fromAttributePaths("source", "analysisExecution.resultFiles");

    private final EntityGraph COMMONS_ENTITY_GRAPH = EntityUtils.fromAttributePaths(
            "createdBy",
            "modifiedBy"
    );

    @Autowired
    private PredictionAnalysisRepository predictionAnalysisRepository;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private ConceptSetService conceptSetService;

    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private GenerationUtils generationUtils;

    @Autowired
    private JobService jobService;

    @Autowired
    private PredictionAnalysisGenerationRepository generationRepository;

    @Autowired
    private Environment env;

    @Autowired
    private SourceAccessor sourceAccessor;

    @Autowired
    private DesignImportService designImportService;

    @Autowired
    private ConversionService conversionService;

    private final String EXEC_SCRIPT = ResourceHelper.GetResourceAsString("/resources/prediction/r/runAnalysis.R");

    @Override
    public Iterable<PredictionAnalysis> getAnalysisList() {

        return predictionAnalysisRepository.findAll(COMMONS_ENTITY_GRAPH);
    }

    @Override
    public int getCountPredictionWithSameName(Integer id, String name) {

        return predictionAnalysisRepository.getCountPredictionWithSameName(id, name);
    }

    @Override
    public PredictionAnalysis getById(Integer id) {
        return predictionAnalysisRepository.findOne(id, COMMONS_ENTITY_GRAPH);
    }

    @Override
    public void delete(final int id) {
        this.predictionAnalysisRepository.delete(id);
    }

    @Override
    public PredictionAnalysis createAnalysis(PredictionAnalysis pred) {
        Date currentTime = Calendar.getInstance().getTime();
        pred.setCreatedBy(getCurrentUser());
        pred.setCreatedDate(currentTime);
        // Fields with information about modifications have to be reseted
        pred.setModifiedBy(null);
        pred.setModifiedDate(null);

        pred.setName(StringUtils.trim(pred.getName()));

        return save(pred);
    }

    @Override
    public PredictionAnalysis updateAnalysis(final int id, PredictionAnalysis pred) {
        PredictionAnalysis predFromDB = getById(id);
        Date currentTime = Calendar.getInstance().getTime();

        pred.setModifiedBy(getCurrentUser());
        pred.setModifiedDate(currentTime);
        // Prevent any updates to protected fields like created/createdBy
        pred.setCreatedDate(predFromDB.getCreatedDate());
        pred.setCreatedBy(predFromDB.getCreatedBy());

        pred.setName(StringUtils.trim(pred.getName()));

        return save(pred);
    }

    private List<String> getNamesLike(String name) {
        return predictionAnalysisRepository.findAllByNameStartsWith(name).stream().map(PredictionAnalysis::getName).collect(Collectors.toList());
    }

    @Override
    public PredictionAnalysis copy(final int id) {
        PredictionAnalysis analysis = this.predictionAnalysisRepository.findOne(id);
        entityManager.detach(analysis); // Detach from the persistence context in order to save a copy
        analysis.setId(null);
        analysis.setName(getNameForCopy(analysis.getName()));
        return this.createAnalysis(analysis);
    }

    @Override
    public PredictionAnalysis getAnalysis(int id) {

        return this.predictionAnalysisRepository.findOne(id, COMMONS_ENTITY_GRAPH);
    }

    @Override
    public PatientLevelPredictionAnalysisImpl exportAnalysis(int id) {

        return exportAnalysis(id, sourceService.getPriorityVocabularySource().getSourceKey());
    }

    @Override
    public PatientLevelPredictionAnalysisImpl exportAnalysis(int id, String sourceKey) {
        PredictionAnalysis pred = predictionAnalysisRepository.findOne(id);
        PatientLevelPredictionAnalysisImpl expression;
        try {
            expression = Utils.deserialize(pred.getSpecification(), PatientLevelPredictionAnalysisImpl.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Set the root properties
        expression.setId(pred.getId());
        expression.setName(StringUtils.trim(pred.getName()));
        expression.setDescription(pred.getDescription());
        expression.setOrganizationName(env.getRequiredProperty("organization.name"));
        expression.setSkeletonVersion(PREDICTION_SKELETON_VERSION);

        // Retrieve the cohort definition details
        ArrayList<AnalysisCohortDefinition> detailedList = new ArrayList<>();
        for (AnalysisCohortDefinition c : expression.getCohortDefinitions()) {
            CohortDefinition cd = cohortDefinitionRepository.findOneWithDetail(c.getId());
            detailedList.add(new AnalysisCohortDefinition(cd));
        }
        expression.setCohortDefinitions(detailedList);

        // Retrieve the concept set expressions
        ArrayList<AnalysisConceptSet> pcsList = new ArrayList<>();
        HashMap<Integer, ArrayList<Long>> conceptIdentifiers = new HashMap<>();
        for (AnalysisConceptSet pcs : expression.getConceptSets()) {
            pcs.expression = conceptSetService.getConceptSetExpression(pcs.id, sourceKey);
            pcsList.add(pcs);
            conceptIdentifiers.put(pcs.id, new ArrayList<>(vocabularyService.resolveConceptSetExpression(pcs.expression)));
        }
        expression.setConceptSets(pcsList);

        // Resolve all ConceptSetCrossReferences
        for (ConceptSetCrossReferenceImpl xref : expression.getConceptSetCrossReference()) {
            if (xref.getTargetName().equalsIgnoreCase("covariateSettings")) {
                if (xref.getPropertyName().equalsIgnoreCase("includedCovariateConceptIds")) {
                    expression.getCovariateSettings().get(xref.getTargetIndex()).setIncludedCovariateConceptIds(conceptIdentifiers.get(xref.getConceptSetId()));
                } else if (xref.getPropertyName().equalsIgnoreCase("excludedCovariateConceptIds")) {
                    expression.getCovariateSettings().get(xref.getTargetIndex()).setExcludedCovariateConceptIds(conceptIdentifiers.get(xref.getConceptSetId()));
                }
            }
        }

        ExportUtil.clearCreateAndUpdateInfo(expression);
        return expression;
    }

    @Override
    public PredictionAnalysis importAnalysis(PatientLevelPredictionAnalysisImpl analysis) throws Exception {
        try {
            if (Objects.isNull(analysis.getCohortDefinitions()) || Objects.isNull(analysis.getCovariateSettings())) {
                log.error("Failed to import Prediction. Invalid source JSON.");
                throw new InternalServerErrorException();
            }
            // Create all of the cohort definitions
            // and map the IDs from old -> new
            List<BigDecimal> newTargetIds = new ArrayList<>();
            List<BigDecimal> newOutcomeIds = new ArrayList<>();
            analysis.getCohortDefinitions().forEach((analysisCohortDefinition) -> {
                BigDecimal oldId = new BigDecimal(analysisCohortDefinition.getId());
                analysisCohortDefinition.setId(null);
                CohortDefinition cd = designImportService.persistCohortOrGetExisting(conversionService.convert(analysisCohortDefinition, CohortDefinition.class), true);
                if (analysis.getTargetIds().contains(oldId)) {
                    newTargetIds.add(new BigDecimal(cd.getId()));
                }
                if (analysis.getOutcomeIds().contains(oldId)) {
                    newOutcomeIds.add(new BigDecimal(cd.getId()));
                }
                analysisCohortDefinition.setId(cd.getId());
                analysisCohortDefinition.setName(cd.getName());
            });

            // Create all of the concept sets and map
            // the IDs from old -> new
            Map<Integer, Integer> conceptSetIdMap = new HashMap<>();
            analysis.getConceptSets().forEach((pcs) -> {
                int oldId = pcs.id;
                ConceptSetDTO cs = designImportService.persistConceptSet(pcs);
                pcs.id = cs.getId();
                pcs.name = cs.getName();
                conceptSetIdMap.put(oldId, cs.getId());
            });

            // Replace all of the cohort definitions
            analysis.setTargetIds(newTargetIds);
            analysis.setOutcomeIds(newOutcomeIds);

            // Replace all of the concept sets
            analysis.getConceptSetCrossReference().forEach((ConceptSetCrossReferenceImpl xref) -> {
                Integer newConceptSetId = conceptSetIdMap.get(xref.getConceptSetId());
                xref.setConceptSetId(newConceptSetId);
            });

            // Clear all of the concept IDs from the covariate settings
            analysis.getCovariateSettings().forEach((CovariateSettingsImpl cs) -> {
                cs.setIncludedCovariateConceptIds(new ArrayList<>());
                cs.setExcludedCovariateConceptIds(new ArrayList<>());
            });

            // Remove the ID
            analysis.setId(null);

            // Create the prediction analysis
            PredictionAnalysis pa = new PredictionAnalysis();
            pa.setDescription(analysis.getDescription());
            pa.setSpecification(Utils.serialize(analysis));
            pa.setName(NameUtils.getNameWithSuffix(analysis.getName(), this::getNamesLike));

            PredictionAnalysis savedAnalysis = this.createAnalysis(pa);
            return predictionAnalysisRepository.findOne(savedAnalysis.getId(), COMMONS_ENTITY_GRAPH);
        } catch (Exception e) {
            log.debug("Error while importing prediction analysis: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public String getNameForCopy(String dtoName) {
        return NameUtils.getNameForCopy(dtoName, this::getNamesLike, predictionAnalysisRepository.findByName(dtoName));
    }

    @Override
    public void hydrateAnalysis(PatientLevelPredictionAnalysisImpl analysis, String packageName, OutputStream out) throws JsonProcessingException {

        if (packageName == null || !Utils.isAlphaNumeric(packageName)) {
            throw new IllegalArgumentException("The package name must be alphanumeric only.");
        }
        analysis.setSkeletonVersion(PREDICTION_SKELETON_VERSION);
        analysis.setPackageName(packageName);
        super.hydrateAnalysis(analysis, out);
    }


    
    @Override
    @DataSourceAccess
    public JobExecutionResource runGeneration(final PredictionAnalysis predictionAnalysis,
                                              @SourceKey final String sourceKey) throws IOException {

        final Source source = sourceService.findBySourceKey(sourceKey);
        final Integer predictionAnalysisId = predictionAnalysis.getId();

        String packageName = String.format("PredictionAnalysis.%s", SessionUtils.sessionId());
        String packageFilename = String.format("prediction_study_%d.zip", predictionAnalysisId);
        List<AnalysisFile> analysisFiles = new ArrayList<>();
        AnalysisFile analysisFile = new AnalysisFile();
        analysisFile.setFileName(packageFilename);
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
          PatientLevelPredictionAnalysisImpl analysis = exportAnalysis(predictionAnalysisId, sourceKey);
          hydrateAnalysis(analysis, packageName, out);
          analysisFile.setContents(out.toByteArray());
        }
        analysisFiles.add(analysisFile);
        analysisFiles.add(prepareAnalysisExecution(packageName, packageFilename, predictionAnalysisId));

        JobParametersBuilder builder = prepareJobParametersBuilder(source, predictionAnalysisId, packageName, packageFilename)
                .addString(PREDICTION_ANALYSIS_ID, predictionAnalysisId.toString())
                .addString(JOB_NAME, String.format("Generating Prediction Analysis %d using %s (%s)", predictionAnalysisId, source.getSourceName(), source.getSourceKey()));


        Job generateAnalysisJob = generationUtils.buildJobForExecutionEngineBasedAnalysisTasklet(
                GENERATE_PREDICTION_ANALYSIS,
                source,
                builder,
                analysisFiles
        ).build();

        return jobService.runJob(generateAnalysisJob, builder.toJobParameters());
    }

    @Override
    protected String getExecutionScript() {

        return EXEC_SCRIPT;
    }

    @Override
    public List<PredictionGenerationEntity> getPredictionGenerations(Integer predictionAnalysisId) {

        return generationRepository
            .findByPredictionAnalysisId(predictionAnalysisId, DEFAULT_ENTITY_GRAPH)
            .stream()
            .filter(gen -> sourceAccessor.hasAccess(gen.getSource()))
            .collect(Collectors.toList());
    }

    @Override
    public PredictionGenerationEntity getGeneration(Long generationId) {

        return generationRepository.findOne(generationId, DEFAULT_ENTITY_GRAPH);
    }
    
    private PredictionAnalysis save(PredictionAnalysis analysis) {
        analysis = predictionAnalysisRepository.saveAndFlush(analysis);
        entityManager.refresh(analysis);
        analysis = getById(analysis.getId());
        return analysis;
    }

    @Override
    public String getJobName() {
        return GENERATE_PREDICTION_ANALYSIS;
    }

    @Override
    public String getExecutionFoldingKey() {
        return PREDICTION_ANALYSIS_ID;
    }
}
