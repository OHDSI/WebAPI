package org.ohdsi.webapi.estimation;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpression.ConceptSetItem;
import org.ohdsi.hydra.Hydra;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.common.generation.AnalysisExecutionSupport;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.estimation.domain.EstimationGenerationEntity;
import org.ohdsi.webapi.estimation.repository.EstimationAnalysisGenerationRepository;
import org.ohdsi.webapi.estimation.specification.*;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.service.SourceService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.util.EntityUtils;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import static org.ohdsi.webapi.Constants.GENERATE_ESTIMATION_ANALYSIS;
import static org.ohdsi.webapi.Constants.Params.ESTIMATION_ANALYSIS_ID;
import static org.ohdsi.webapi.Constants.Params.JOB_NAME;
import static org.ohdsi.webapi.Constants.Templates.ENTITY_COPY_PREFIX;

@Service
@Transactional
public class EstimationServiceImpl extends AnalysisExecutionSupport implements EstimationService {
    
    private static final String CONCEPT_SET_XREF_KEY_TARGET_COMPARATOR_OUTCOME = "estimationAnalysisSettings.analysisSpecification.targetComparatorOutcomes";
    private static final String CONCEPT_SET_XREF_KEY_NEGATIVE_CONTROL_OUTCOMES = "negativeControlOutcomes";
    private static final String CONCEPT_SET_XREF_KEY_COHORT_METHOD_COVAR = "estimationAnalysisSettings.analysisSpecification.cohortMethodAnalysisList.getDbCohortMethodDataArgs.covariateSettings";
    private static final String CONCEPT_SET_XREF_KEY_POS_CONTROL_COVAR = "positiveControlSynthesisArgs.covariateSettings";

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
    private PermissionManager authorizer;

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

    @Override
    public Iterable<Estimation> getAnalysisList() {

        return estimationRepository.findAll(COMMONS_ENTITY_GRAPH);
    }

    @Override
    public void delete(final int id) {

        this.estimationRepository.delete(id);
    }

    @Override
    public Estimation createEstimation(Estimation est) throws Exception {

        Date currentTime = Calendar.getInstance().getTime();

        UserEntity user = authorizer.getCurrentUser();
        est.setCreatedBy(user);
        est.setCreatedDate(currentTime);

        return this.estimationRepository.save(est);
    }

    @Override
    public Estimation updateEstimation(final int id, Estimation est) throws Exception {

        Estimation estFromDB = estimationRepository.findOne(id);
        Date currentTime = Calendar.getInstance().getTime();

        UserEntity user = authorizer.getCurrentUser();
        est.setModifiedBy(user);
        est.setModifiedDate(currentTime);
        // Prevent any updates to protected fields like created/createdBy
        est.setCreatedDate(estFromDB.getCreatedDate());
        est.setCreatedBy(estFromDB.getCreatedBy());

        return this.estimationRepository.save(est);
    }

    @Override
    public Estimation copy(final int id) throws Exception {

        Estimation est = this.estimationRepository.findOne(id);
        entityManager.detach(est); // Detach from the persistence context in order to save a copy
        est.setId(null);
        est.setName(String.format(ENTITY_COPY_PREFIX, est.getName()));
        return this.createEstimation(est);
    }

    @Override
    public Estimation getAnalysis(int id) {

        return estimationRepository.findOne(id, COMMONS_ENTITY_GRAPH);
    }

    @Override
    public EstimationAnalysis exportAnalysis(Estimation est) {

        ObjectMapper mapper = new ObjectMapper();
        EstimationAnalysis expression;
        try {
            expression = mapper.readValue(est.getSpecification(), EstimationAnalysis.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // Set the root properties
        expression.setId(est.getId());
        expression.setName(est.getName());
        expression.setDescription(est.getDescription());
        expression.setOrganizationName(env.getRequiredProperty("organization.name"));
        
        // Retrieve the cohort definition details
        ArrayList<EstimationCohortDefinition> detailedList = new ArrayList<>();
        for (EstimationCohortDefinition c : expression.getCohortDefinitions()) {
            System.out.println(c.getId());
            CohortDefinition cd = cohortDefinitionRepository.findOneWithDetail(c.getId());
            detailedList.add(new EstimationCohortDefinition(cd));
        }
        expression.setCohortDefinitions(detailedList);
        
        // Retrieve the concept set expressions
        ArrayList<EstimationConceptSet> ecsList = new ArrayList<>();
        HashMap<Integer, ArrayList<Long>> conceptIdentifiers = new HashMap<>();
        HashMap<Integer, ConceptSetExpression> csExpressionList = new HashMap<>();
        for (EstimationConceptSet pcs : expression.getConceptSets()) {
            System.out.println(pcs.id);
            pcs.expression = conceptSetService.getConceptSetExpression(pcs.id);
            csExpressionList.put(pcs.id, pcs.expression);
            ecsList.add(pcs);
            conceptIdentifiers.put(pcs.id, new ArrayList<>(vocabularyService.resolveConceptSetExpression(pcs.expression)));
        }
        expression.setConceptSets(ecsList);
        
        // Resolve all ConceptSetCrossReferences
        for (ConceptSetCrossReference xref : expression.getConceptSetCrossReference()) {
            // TODO: Make this conditional on the expression.getEstimationAnalysisSettings().getEstimationType() vs
            // hard coded to always use a comparative cohort analysis once we have implemented the other
            // estimation types
            ArrayList<LinkedHashMap> tcoList = (ArrayList<LinkedHashMap>) ((LinkedHashMap) expression.getEstimationAnalysisSettings().getAnalysisSpecification()).get("targetComparatorOutcomes");
            ArrayList<LinkedHashMap> ccaList = (ArrayList<LinkedHashMap>) ((LinkedHashMap) expression.getEstimationAnalysisSettings().getAnalysisSpecification()).get("cohortMethodAnalysisList");
            
            if (xref.getTargetName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_TARGET_COMPARATOR_OUTCOME)) {
                LinkedHashMap tco = tcoList.get(xref.getTargetIndex());
                tco.put(xref.getPropertyName(), conceptIdentifiers.get(xref.getConceptSetId()));
            } else if (xref.getTargetName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_NEGATIVE_CONTROL_OUTCOMES)) {
                // Fill in the negative controls for each T/C pair as specified
                LinkedHashMap tco = tcoList.get(xref.getTargetIndex());
                ConceptSetExpression e = csExpressionList.get(xref.getConceptSetId());
                for(ConceptSetItem csi : e.items) {
                    NegativeControl nc = new NegativeControl();
                    nc.setTargetId(Long.valueOf((int) tco.get("targetId")));
                    nc.setComparatorId(Long.valueOf((int) tco.get("comparatorId")));
                    nc.setOutcomeId(csi.concept.conceptId);
                    nc.setOutcomeName(csi.concept.conceptName);
                    nc.setType(NegativeControl.TypeEnum.OUTCOME);
                    expression.addNegativeControlsItem(nc);
                }
            } else if (xref.getTargetName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_COHORT_METHOD_COVAR)) {
                LinkedHashMap cca = ccaList.get(xref.getTargetIndex());
                LinkedHashMap dbCohortMethod = (LinkedHashMap) cca.get("getDbCohortMethodDataArgs");
                LinkedHashMap dbCohortMethodCovarSettings = (LinkedHashMap) dbCohortMethod.get("covariateSettings");
                dbCohortMethodCovarSettings.put(xref.getPropertyName(), conceptIdentifiers.get(xref.getConceptSetId()));
            } else if (xref.getTargetName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_POS_CONTROL_COVAR)) {
                if (xref.getPropertyName().equalsIgnoreCase("includedCovariateConceptIds")) {
                    expression.getPositiveControlSynthesisArgs().getCovariateSettings().includedCovariateConceptIds(conceptIdentifiers.get(xref.getConceptSetId()));
                } else if (xref.getPropertyName().equalsIgnoreCase("excludedCovariateConceptIds")) {
                    expression.getPositiveControlSynthesisArgs().getCovariateSettings().excludedCovariateConceptIds(conceptIdentifiers.get(xref.getConceptSetId()));
                }
            }
        }
        
        return expression;
    }

    public void hydrateAnalysis(EstimationAnalysis analysis, OutputStream out) throws JsonProcessingException {
        // Cannot use Utils.serialize(analysis) since it removes
        // properties with null values which are required in the
        // specification
        //String studySpecs = Utils.serialize(analysis);
        String studySpecs = generationUtils.serializeAnalysis(analysis);
        Hydra h = new Hydra(studySpecs);
        h.hydrate(out);
    }

    @Override
    public void runGeneration(Estimation estimation, String sourceKey) throws IOException {

        final Source source = sourceService.findBySourceKey(sourceKey);
        final Integer analysisId = estimation.getId();

        String packageName = String.format("EstimationAnalysis.%s", SessionUtils.sessionId());
        String packageFilename = String.format("estimation_study_%d.zip", analysisId);
        List<AnalysisFile> analysisFiles = new ArrayList<>();
        AnalysisFile analysisFile = new AnalysisFile();
        analysisFile.setFileName(packageFilename);
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            EstimationAnalysis analysis = exportAnalysis(estimation);
            analysis.setPackageName(packageName);
            hydrateAnalysis(analysis, out);
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

        jobService.runJob(generateAnalysisJob, builder.toJobParameters());
    }

    @Override
    protected String getExecutionScript() {

        return EXEC_SCRIPT;
    }

    @Override
    public List<EstimationGenerationEntity> getEstimationGenerations(Integer estimationAnalysisId) {

        return generationRepository.findByEstimationAnalysisId(estimationAnalysisId, DEFAULT_ENTITY_GRAPH);
    }

    @Override
    public EstimationGenerationEntity getGeneration(Long generationId) {

        return generationRepository.findOne(generationId, DEFAULT_ENTITY_GRAPH);
    }
}
