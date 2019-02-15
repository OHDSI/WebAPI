package org.ohdsi.webapi.prediction;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraph;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.hydra.Hydra;
import org.ohdsi.webapi.Constants;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.common.generation.AnalysisExecutionSupport;
import org.ohdsi.webapi.common.generation.GenerationUtils;
import org.ohdsi.webapi.executionengine.entity.AnalysisFile;
import org.ohdsi.webapi.prediction.domain.PredictionGenerationEntity;
import org.ohdsi.webapi.prediction.repository.PredictionAnalysisGenerationRepository;
import org.ohdsi.webapi.prediction.specification.ConceptSetCrossReference;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysis;
import org.ohdsi.webapi.prediction.specification.PredictionCohortDefinition;
import org.ohdsi.webapi.prediction.specification.PredictionConceptSet;
import org.ohdsi.webapi.service.*;
import org.ohdsi.webapi.shiro.annotations.DataSourceAccess;
import org.ohdsi.webapi.shiro.annotations.SourceKey;
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

import static org.ohdsi.webapi.Constants.GENERATE_PREDICTION_ANALYSIS;
import static org.ohdsi.webapi.Constants.Params.*;

@Service
@Transactional
public class PredictionServiceImpl extends AnalysisExecutionSupport implements PredictionService {

    private static final EntityGraph DEFAULT_ENTITY_GRAPH = EntityGraphUtils.fromAttributePaths("source");

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

    private final String EXEC_SCRIPT = ResourceHelper.GetResourceAsString("/resources/prediction/r/runAnalysis.R");

    @Override
    public Iterable<PredictionAnalysis> getAnalysisList() {

        return predictionAnalysisRepository.findAll(COMMONS_ENTITY_GRAPH);
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
    
        return this.predictionAnalysisRepository.save(pred);
    }

    @Override
    public PredictionAnalysis updateAnalysis(final int id, PredictionAnalysis pred) {
        PredictionAnalysis predFromDB = predictionAnalysisRepository.findOne(id);
        Date currentTime = Calendar.getInstance().getTime();

        pred.setModifiedBy(getCurrentUser());
        pred.setModifiedDate(currentTime);
        // Prevent any updates to protected fields like created/createdBy
        pred.setCreatedDate(predFromDB.getCreatedDate());
        pred.setCreatedBy(predFromDB.getCreatedBy());

        return this.predictionAnalysisRepository.save(pred);
    }
    
    @Override
    public PredictionAnalysis copy(final int id) {
        PredictionAnalysis analysis = this.predictionAnalysisRepository.findOne(id);
        entityManager.detach(analysis); // Detach from the persistence context in order to save a copy
        analysis.setId(null);
        analysis.setName(String.format(Constants.Templates.ENTITY_COPY_PREFIX, analysis.getName()));
        return this.createAnalysis(analysis);
    }
    
    @Override
    public PredictionAnalysis getAnalysis(int id) {

        return this.predictionAnalysisRepository.findOne(id, COMMONS_ENTITY_GRAPH);
    }
    
    @Override
    public PatientLevelPredictionAnalysis exportAnalysis(int id) {
        PredictionAnalysis pred = predictionAnalysisRepository.findOne(id);
        ObjectMapper mapper = new ObjectMapper();
        PatientLevelPredictionAnalysis expression;
        try {
            expression = mapper.readValue(pred.getSpecification(), org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysis.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // Set the root properties
        expression.setId(pred.getId());
        expression.setName(pred.getName());
        expression.setDescription(pred.getDescription());
        expression.setOrganizationName(env.getRequiredProperty("organization.name"));
        
        // Retrieve the cohort definition details
        ArrayList<PredictionCohortDefinition> detailedList = new ArrayList<>();
        for (PredictionCohortDefinition c : expression.getCohortDefinitions()) {
            System.out.println(c.getId());
            CohortDefinition cd = cohortDefinitionRepository.findOneWithDetail(c.getId());
            detailedList.add(new PredictionCohortDefinition(cd));
        }
        expression.setCohortDefinitions(detailedList);
        
        // Retrieve the concept set expressions
        ArrayList<PredictionConceptSet> pcsList = new ArrayList<>();
        HashMap<Integer, ArrayList<Long>> conceptIdentifiers = new HashMap<Integer, ArrayList<Long>>();
        for (PredictionConceptSet pcs : expression.getConceptSets()) {
            System.out.println(pcs.id);
            pcs.expression = conceptSetService.getConceptSetExpression(pcs.id);
            pcsList.add(pcs);
            conceptIdentifiers.put(pcs.id, new ArrayList<>(vocabularyService.resolveConceptSetExpression(pcs.expression)));
        }
        expression.setConceptSets(pcsList);
        
        // Resolve all ConceptSetCrossReferences
        for (ConceptSetCrossReference xref : expression.getConceptSetCrossReference()) {
            if (xref.getTargetName().equalsIgnoreCase("covariateSettings")) {
                if (xref.getPropertyName().equalsIgnoreCase("includedCovariateConceptIds")) {
                    expression.getCovariateSettings().get(xref.getTargetIndex()).includedCovariateConceptIds(conceptIdentifiers.get(xref.getConceptSetId()));
                } else if (xref.getPropertyName().equalsIgnoreCase("excludedCovariateConceptIds")) {
                    expression.getCovariateSettings().get(xref.getTargetIndex()).excludedCovariateConceptIds(conceptIdentifiers.get(xref.getConceptSetId()));
                }
            }
        }
        
        return expression;
    }
    
    public void hydrateAnalysis(org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysis plpa, OutputStream out) throws JsonProcessingException {
        // Cannot use Utils.serialize(analysis) since it removes
        // properties with null values which are required in the
        // specification
        //String studySpecs = Utils.serialize(analysis);
        String studySpecs = generationUtils.serializeAnalysis(plpa);

        Hydra h = new Hydra(studySpecs);
        h.hydrate(out);
    }
    
    @Override
    @DataSourceAccess
    public void runGeneration(final PredictionAnalysis predictionAnalysis,
                              @SourceKey final String sourceKey) throws IOException {

        final Source source = sourceService.findBySourceKey(sourceKey);
        final Integer predictionAnalysisId = predictionAnalysis.getId();

        String packageName = String.format("PredictionAnalysis.%s", SessionUtils.sessionId());
        String packageFilename = String.format("prediction_study_%d.zip", predictionAnalysisId);
        List<AnalysisFile> analysisFiles = new ArrayList<>();
        AnalysisFile analysisFile = new AnalysisFile();
        analysisFile.setFileName(packageFilename);
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
          PatientLevelPredictionAnalysis analysis = exportAnalysis(predictionAnalysisId);
          analysis.setPackageName(packageName);
          hydrateAnalysis(analysis, out);
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

        jobService.runJob(generateAnalysisJob, builder.toJobParameters());
    }

    @Override
    protected String getExecutionScript() {

        return EXEC_SCRIPT;
    }

    @Override
    public List<PredictionGenerationEntity> getPredictionGenerations(Integer predictionAnalysisId) {

        return generationRepository.findByPredictionAnalysisId(predictionAnalysisId, DEFAULT_ENTITY_GRAPH);
    }

    @Override
    public PredictionGenerationEntity getGeneration(Long generationId) {

        return generationRepository.findOne(generationId, DEFAULT_ENTITY_GRAPH);
    }
}
