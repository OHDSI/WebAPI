package org.ohdsi.webapi.estimation;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.estimation.design.EstimationTypeEnum;
import org.ohdsi.analysis.estimation.design.NegativeControlTypeEnum;
import org.ohdsi.analysis.estimation.design.Settings;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpression.ConceptSetItem;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.common.DesignImportService;
import org.ohdsi.webapi.common.generation.AnalysisExecutionSupport;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.conceptset.ConceptSetCrossReferenceImpl;
import org.ohdsi.webapi.estimation.comparativecohortanalysis.specification.CohortMethodAnalysisImpl;
import org.ohdsi.webapi.estimation.comparativecohortanalysis.specification.ComparativeCohortAnalysisImpl;
import org.ohdsi.webapi.estimation.comparativecohortanalysis.specification.TargetComparatorOutcomesImpl;
import org.ohdsi.webapi.estimation.domain.EstimationGenerationEntity;
import org.ohdsi.webapi.estimation.repository.EstimationAnalysisGenerationRepository;
import org.ohdsi.webapi.estimation.repository.EstimationRepository;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;
import org.ohdsi.webapi.estimation.specification.NegativeControlImpl;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.featureextraction.specification.CovariateSettingsImpl;
import org.ohdsi.webapi.job.GeneratesNotification;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
import org.ohdsi.webapi.shiro.management.datasource.SourceAccessor;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.ExportUtil;
import org.ohdsi.webapi.util.EntityUtils;
import org.ohdsi.webapi.util.NameUtils;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.TempFileUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.InternalServerErrorException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.Constants.GENERATE_ESTIMATION_ANALYSIS;
import static org.ohdsi.webapi.Constants.Params.ESTIMATION_ANALYSIS_ID;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;

@Service
@Transactional
public class EstimationServiceImpl extends AnalysisExecutionSupport implements EstimationService, GeneratesNotification {
    
    private static final String CONCEPT_SET_XREF_KEY_TARGET_COMPARATOR_OUTCOME = "estimationAnalysisSettings.analysisSpecification.targetComparatorOutcomes";
    public static final String CONCEPT_SET_XREF_KEY_NEGATIVE_CONTROL_OUTCOMES = "negativeControlOutcomes";
    private static final String CONCEPT_SET_XREF_KEY_COHORT_METHOD_COVAR = "estimationAnalysisSettings.analysisSpecification.cohortMethodAnalysisList.getDbCohortMethodDataArgs.covariateSettings";
    private static final String CONCEPT_SET_XREF_KEY_POS_CONTROL_COVAR = "positiveControlSynthesisArgs.covariateSettings";
    private static final String CONCEPT_SET_XREF_KEY_INCLUDED_COVARIATE_CONCEPT_IDS = "includedCovariateConceptIds";
    private static final String CONCEPT_SET_XREF_KEY_EXCLUDED_COVARIATE_CONCEPT_IDS = "excludedCovariateConceptIds";

    private final String EXEC_SCRIPT = ResourceHelper.GetResourceAsString("/resources/estimation/r/runAnalysis.R");

    private final EntityGraph DEFAULT_ENTITY_GRAPH = EntityGraphUtils.fromAttributePaths("source", "analysisExecution.resultFiles");

    private final EntityGraph COMMONS_ENTITY_GRAPH = EntityUtils.fromAttributePaths(
            "createdBy",
            "modifiedBy"
    );

    @PersistenceContext
    protected EntityManager entityManager;
    
    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;

    @Autowired
    private ConceptSetService conceptSetService;
    
    @Autowired
    private VocabularyService vocabularyService;

    @Autowired
    private EstimationRepository estimationRepository;

    @Autowired
    private Environment env;

    @Autowired
    private GenerationUtils generationUtils;

    @Autowired
    private SourceService sourceService;

    @Autowired
    private JobService jobService;

    @Autowired
    private EstimationAnalysisGenerationRepository generationRepository;

    @Autowired
    private SourceAccessor sourceAccessor;

    @Autowired
    private DesignImportService designImportService;

    @Autowired
    private ConversionService conversionService;

    @Value("${organization.name}")
    private String organizationName;

    @Override
    public Iterable<Estimation> getAnalysisList() {

        return estimationRepository.findAll(COMMONS_ENTITY_GRAPH);
    }
    
    @Override
    public int getCountEstimationWithSameName(Integer id, String name) {
        return estimationRepository.getCountEstimationWithSameName(id, name);
    }
    
    @Override
    public Estimation getById(Integer id) {
        return estimationRepository.findOne(id, COMMONS_ENTITY_GRAPH);
    }

    @Override
    public void delete(final int id) {

        this.estimationRepository.delete(id);
    }

    @Override
    public Estimation createEstimation(Estimation est) throws Exception {
        Date currentTime = Calendar.getInstance().getTime();

        est.setCreatedBy(getCurrentUser());
        est.setCreatedDate(currentTime);
        // Fields with information about modifications have to be reseted
        est.setModifiedBy(null);
        est.setModifiedDate(null);

        est.setName(StringUtils.trim(est.getName()));

        return save(est);
    }

    @Override
    public Estimation updateEstimation(final int id, Estimation est) throws Exception {
        Estimation estFromDB = getById(id);
        Date currentTime = Calendar.getInstance().getTime();

        est.setModifiedBy(getCurrentUser());
        est.setModifiedDate(currentTime);
        // Prevent any updates to protected fields like created/createdBy
        est.setCreatedDate(estFromDB.getCreatedDate());
        est.setCreatedBy(estFromDB.getCreatedBy());

        est.setName(StringUtils.trim(est.getName()));

        return save(est);
    }

    private List<String> getNamesLike(String name) {
        return estimationRepository.findAllByNameStartsWith(name).stream().map(Estimation::getName).collect(Collectors.toList());
    }
    
    @Override
    public Estimation copy(final int id) throws Exception {

        Estimation est = estimationRepository.findOne(id);
        entityManager.detach(est); // Detach from the persistence context in order to save a copy
        est.setId(null);
        est.setName(getNameForCopy(est.getName()));
        return this.createEstimation(est);
    }

    @Override
    public Estimation getAnalysis(int id) {

        return estimationRepository.findOne(id, COMMONS_ENTITY_GRAPH);
    }

    @Override
    public EstimationAnalysisImpl getAnalysisExpression(int id) {
        return Utils.deserialize(estimationRepository.findOne(id, COMMONS_ENTITY_GRAPH).getSpecification(), EstimationAnalysisImpl.class);
    }

    @Override
    public EstimationAnalysisImpl exportAnalysis(Estimation est) {
        
        return exportAnalysis(est, sourceService.getPriorityVocabularySource().getSourceKey());
    }

    @Override
    public EstimationAnalysisImpl exportAnalysis(Estimation est, String sourceKey) {

        EstimationAnalysisImpl expression;
        try {
            expression = Utils.deserialize(est.getSpecification(), EstimationAnalysisImpl.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // Set the root properties
        expression.setId(est.getId());
        expression.setName(StringUtils.trim(est.getName()));
        expression.setDescription(est.getDescription());
        expression.setOrganizationName(this.organizationName);
        
        // Retrieve the cohort definition details
        List<AnalysisCohortDefinition> detailedList = new ArrayList<>();
        for (AnalysisCohortDefinition c : expression.getCohortDefinitions()) {
            CohortDefinition cd = cohortDefinitionRepository.findOneWithDetail(c.getId());
            detailedList.add(new AnalysisCohortDefinition(cd));
        }
        expression.setCohortDefinitions(detailedList);
        
        // Retrieve the concept set expressions
        List<AnalysisConceptSet> ecsList = new ArrayList<>();
        Map<Integer, List<Long>> conceptIdentifiers = new HashMap<>();
        Map<Integer, ConceptSetExpression> csExpressionList = new HashMap<>();
        for (AnalysisConceptSet pcs : expression.getConceptSets()) {
            pcs.expression = conceptSetService.getConceptSetExpression(pcs.id, sourceKey);
            csExpressionList.put(pcs.id, pcs.expression);
            ecsList.add(pcs);
            conceptIdentifiers.put(pcs.id, new ArrayList<>(vocabularyService.resolveConceptSetExpression(pcs.expression)));
        }
        expression.setConceptSets(ecsList);
        
        // Resolve all ConceptSetCrossReferences
        for (ConceptSetCrossReferenceImpl xref : expression.getConceptSetCrossReference()) {
            // TODO: Make this conditional on the expression.getEstimationAnalysisSettings().getEstimationType() vs
            // hard coded to always use a comparative cohort analysis once we have implemented the other
            // estimation types
            Settings settings = expression.getEstimationAnalysisSettings().getAnalysisSpecification();
            ComparativeCohortAnalysisImpl ccaSpec = (ComparativeCohortAnalysisImpl) settings;
            List<TargetComparatorOutcomesImpl> tcoList = ccaSpec.getTargetComparatorOutcomes();
            List<CohortMethodAnalysisImpl> ccaList = ccaSpec.getCohortMethodAnalysisList();
            
            if (xref.getTargetName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_TARGET_COMPARATOR_OUTCOME)) {
                TargetComparatorOutcomesImpl tco = tcoList.get(xref.getTargetIndex());
                List<Long> conceptIds = conceptIdentifiers.get(xref.getConceptSetId());
                if (xref.getPropertyName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_INCLUDED_COVARIATE_CONCEPT_IDS)) {
                    tco.setIncludedCovariateConceptIds(conceptIds);
                } else if (xref.getPropertyName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_EXCLUDED_COVARIATE_CONCEPT_IDS)) {
                    tco.setExcludedCovariateConceptIds(conceptIds);
                }
            } else if (xref.getTargetName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_NEGATIVE_CONTROL_OUTCOMES)) {
                // Fill in the negative controls for each T/C pair as specified
                TargetComparatorOutcomesImpl tco = tcoList.get(xref.getTargetIndex());
                ConceptSetExpression e = csExpressionList.get(xref.getConceptSetId());
                for(ConceptSetItem csi : e.items) {
                    NegativeControlImpl nc = new NegativeControlImpl();
                    nc.setTargetId(tco.getTargetId());
                    nc.setComparatorId(tco.getComparatorId());
                    nc.setOutcomeId(csi.concept.conceptId);
                    nc.setOutcomeName(csi.concept.conceptName);
                    nc.setType(NegativeControlTypeEnum.OUTCOME);
                    expression.addNegativeControlsItem(nc);
                }
            } else if (xref.getTargetName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_COHORT_METHOD_COVAR)) {
                CohortMethodAnalysisImpl cca = ccaList.get(xref.getTargetIndex());
                CovariateSettingsImpl dbCohortMethodCovarSettings = cca.getDbCohortMethodDataArgs().getCovariateSettings();
                List<Long> conceptIds = conceptIdentifiers.get(xref.getConceptSetId());
                if (xref.getPropertyName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_INCLUDED_COVARIATE_CONCEPT_IDS)) {
                    dbCohortMethodCovarSettings.setIncludedCovariateConceptIds(conceptIds);
                } else if (xref.getPropertyName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_EXCLUDED_COVARIATE_CONCEPT_IDS)) {
                    dbCohortMethodCovarSettings.setExcludedCovariateConceptIds(conceptIds);
                }
            } else if (xref.getTargetName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_POS_CONTROL_COVAR)) {
                if (xref.getPropertyName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_INCLUDED_COVARIATE_CONCEPT_IDS)) {
                    expression.getPositiveControlSynthesisArgs().getCovariateSettings().setIncludedCovariateConceptIds(conceptIdentifiers.get(xref.getConceptSetId()));
                } else if (xref.getPropertyName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_EXCLUDED_COVARIATE_CONCEPT_IDS)) {
                    expression.getPositiveControlSynthesisArgs().getCovariateSettings().setExcludedCovariateConceptIds(conceptIdentifiers.get(xref.getConceptSetId()));
                }
            }
        }

        ExportUtil.clearCreateAndUpdateInfo(expression);
        return expression;
    }

    @Override
    public Estimation importAnalysis(EstimationAnalysisImpl analysis) throws Exception {
        try {
            if (Objects.isNull(analysis.getEstimationAnalysisSettings())) {
                log.error("Failed to import Estimation. Invalid source JSON. EstimationAnalysisSettings is empty");
                throw new InternalServerErrorException();
            }
            if (analysis.getEstimationAnalysisSettings().getEstimationType() != EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS) {
                String estimationType = analysis.getEstimationAnalysisSettings().getEstimationType().name();
                throw new UnsupportedOperationException("Cannot import " + estimationType);
            }

            // Open up the analysis and get the relevant portions for import
            Settings settings = analysis.getEstimationAnalysisSettings().getAnalysisSpecification();
            ComparativeCohortAnalysisImpl ccaSpec = (ComparativeCohortAnalysisImpl) settings;
            List<TargetComparatorOutcomesImpl> tcoList = ccaSpec.getTargetComparatorOutcomes();
            List<CohortMethodAnalysisImpl> ccaList = ccaSpec.getCohortMethodAnalysisList();

            // Create all of the cohort definitions
            // and map the IDs from old -> new
            Map<Long, Long> cohortIds = new HashMap<>();
            analysis.getCohortDefinitions().forEach((analysisCohortDefinition) -> {
                Integer oldId = analysisCohortDefinition.getId();
                analysisCohortDefinition.setId(null);
                CohortDefinition cd = designImportService.persistCohortOrGetExisting(conversionService.convert(analysisCohortDefinition, CohortDefinition.class), true);
                cohortIds.put(Long.valueOf(oldId), Long.valueOf(cd.getId()));
                analysisCohortDefinition.setId(cd.getId());
                analysisCohortDefinition.setName(cd.getName());
                log.debug("cohort created: " + cd.getId());
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
                log.debug("concept set created: " + cs.getId());
            });

            // Replace all of the T/C/Os with the new IDs
            tcoList.forEach((tco) -> {
                // Get the new IDs
                Long newT = cohortIds.get(tco.getTargetId());
                Long newC = cohortIds.get(tco.getComparatorId());
                List<Long> newOs = new ArrayList<>();
                tco.getOutcomeIds().forEach((o) -> {
                    newOs.add(cohortIds.get(o));
                });
                // Set the TCO to use the new IDs
                tco.setTargetId(newT);
                tco.setComparatorId(newC);
                tco.setOutcomeIds(newOs);
                // Clear any included/excluded covarite concept ids
                tco.setExcludedCovariateConceptIds(new ArrayList<>());
                tco.setIncludedCovariateConceptIds(new ArrayList<>());
            });

            // Replace all of the concept sets
            analysis.getConceptSetCrossReference().forEach((ConceptSetCrossReferenceImpl xref) -> {
                Integer newConceptSetId = conceptSetIdMap.get(xref.getConceptSetId());
                xref.setConceptSetId(newConceptSetId);
            });

            // Clear all of the concept IDs from the covariate settings
            ccaList.forEach((cca) -> {
                cca.getDbCohortMethodDataArgs().getCovariateSettings().setIncludedCovariateConceptIds(new ArrayList<>());
                cca.getDbCohortMethodDataArgs().getCovariateSettings().setExcludedCovariateConceptIds(new ArrayList<>());
            });
            analysis.getPositiveControlSynthesisArgs().getCovariateSettings().setIncludedCovariateConceptIds(new ArrayList<>());
            analysis.getPositiveControlSynthesisArgs().getCovariateSettings().setExcludedCovariateConceptIds(new ArrayList<>());
            
            // Remove all of the negative controls as 
            // these are populated upon export
            analysis.setNegativeControls(new ArrayList<>());

            // Remove the ID
            analysis.setId(null);

            // Create the estimation
            Estimation est = new Estimation();
            est.setDescription(analysis.getDescription());
            est.setType(EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS);
            est.setSpecification(Utils.serialize(analysis));
            est.setName(NameUtils.getNameWithSuffix(analysis.getName(), this::getNamesLike));

            Estimation savedEstimation = this.createEstimation(est);
            return estimationRepository.findOne(savedEstimation.getId(), COMMONS_ENTITY_GRAPH);
        } catch (Exception e) {
            log.debug("Error while importing estimation analysis: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public String getNameForCopy(String dtoName) {
        return NameUtils.getNameForCopy(dtoName, this::getNamesLike, estimationRepository.findByName(dtoName));
    }

    @Override
    public void hydrateAnalysis(EstimationAnalysisImpl analysis, String packageName, OutputStream out) throws JsonProcessingException {

        if (packageName == null || !Utils.isAlphaNumeric(packageName)) {
            throw new IllegalArgumentException("The package name must be alphanumeric only.");
        }
        analysis.setPackageName(packageName);
        super.hydrateAnalysis(analysis, out);
    }

    @Override
    @DataSourceAccess
    public JobExecutionResource runGeneration(Estimation estimation, @SourceKey String sourceKey) throws IOException {

        final Source source = sourceService.findBySourceKey(sourceKey);
        final Integer analysisId = estimation.getId();

        String packageName = String.format("EstimationAnalysis.%s", SessionUtils.sessionId());
        String packageFilename = String.format("estimation_study_%d.zip", analysisId);
        List<AnalysisFile> analysisFiles = new ArrayList<>();
        AnalysisFile analysisFile = new AnalysisFile();
        analysisFile.setFileName(packageFilename);
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            EstimationAnalysisImpl analysis = exportAnalysis(estimation, sourceKey);
            hydrateAnalysis(analysis, packageName, out);
            analysisFile.setContents(out.toByteArray());
        }
        analysisFiles.add(analysisFile);
        analysisFiles.add(prepareAnalysisExecution(packageName, packageFilename, analysisId));

        JobParametersBuilder builder = prepareJobParametersBuilder(source, analysisId, packageName, packageFilename)
                .addString(ESTIMATION_ANALYSIS_ID, analysisId.toString())
                .addString(JOB_NAME, String.format("Generating Estimation Analysis %d using %s (%s)", analysisId, source.getSourceName(), source.getSourceKey()));

        Job generateAnalysisJob = generationUtils.buildJobForExecutionEngineBasedAnalysisTasklet(
                GENERATE_ESTIMATION_ANALYSIS,
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
    public List<EstimationGenerationEntity> getEstimationGenerations(Integer estimationAnalysisId) {

        return generationRepository
            .findByEstimationAnalysisId(estimationAnalysisId, DEFAULT_ENTITY_GRAPH)
            .stream()
            .filter(gen -> sourceAccessor.hasAccess(gen.getSource()))
            .collect(Collectors.toList());
    }

    @Override
    public EstimationGenerationEntity getGeneration(Long generationId) {

        return generationRepository.findOne(generationId, DEFAULT_ENTITY_GRAPH);
    }
    
    private Estimation save(Estimation analysis) {
        analysis = estimationRepository.saveAndFlush(analysis);
        entityManager.refresh(analysis);
        analysis = getById(analysis.getId());
        return analysis;
    }

    @Override
    public String getJobName() {
        return GENERATE_ESTIMATION_ANALYSIS;
    }

    @Override
    public String getExecutionFoldingKey() {
        return ESTIMATION_ANALYSIS_ID;
    }
}
