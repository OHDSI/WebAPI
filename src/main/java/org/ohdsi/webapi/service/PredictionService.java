package org.ohdsi.webapi.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ohdsi.analysis.Utils;
import org.ohdsi.hydra.Hydra;
import org.ohdsi.webapi.Constants;
import static org.ohdsi.webapi.Constants.Templates.ENTITY_COPY_PREFIX;
import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.analysis.importer.AnalysisCohortDefinitionImportService;
import org.ohdsi.webapi.analysis.importer.AnalysisConceptSetImportService;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.conceptset.ConceptSetCrossReferenceImpl;
import org.ohdsi.webapi.featureextraction.specification.CovariateSettingsImpl;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.PredictionListItem;
import org.ohdsi.webapi.prediction.PredictionAnalysisRepository;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.specification.*;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.UserUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author asena5
 */
@RestController
@Path("/prediction/")
@Transactional
public class PredictionService  extends AbstractDaoService {

    @Autowired
    private PredictionAnalysisRepository predictionAnalysisRepository;

    /**
     *
     */
    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private ConceptSetService conceptSetService;
    
    @Autowired
    private VocabularyService vocabularyService;
    
    @Autowired
    private GenericConversionService conversionService;
    
    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;
    
    @Autowired
    private Environment env;

    private static final Logger log = LoggerFactory.getLogger(PredictionService.class);    
    
    /**
     *
     * @return
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PredictionListItem> getAnalysisList() {

        return StreamSupport
                .stream(predictionAnalysisRepository.findAll().spliterator(), false)
                .map(pred -> { 
                    PredictionListItem item = new PredictionListItem();
                    item.analysisId = pred.getId();
                    item.name = pred.getName();
                    item.description = pred.getDescription();
                    item.createdBy = UserUtils.nullSafeLogin(pred.getCreatedBy());
                    item.createdDate = pred.getCreatedDate();
                    item.modifiedBy = UserUtils.nullSafeLogin(pred.getModifiedBy());
                    item.modifiedDate = pred.getModifiedDate();
                    return item;
                }).collect(Collectors.toList());
    }
    
    /**
     *
     * @param id
     */
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public void delete(@PathParam("id") final int id) {
        this.predictionAnalysisRepository.delete(id);
    }
    
    /**
     *
     * @param pred
     * @return
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PredictionAnalysisDTO createAnalysis(PredictionAnalysis pred) { 
        Date currentTime = Calendar.getInstance().getTime();
        pred.setCreatedBy(getCurrentUser());
        pred.setCreatedDate(currentTime);
    
        PredictionAnalysis predWithId = this.predictionAnalysisRepository.save(pred);
        return conversionService.convert(predWithId, PredictionAnalysisDTO.class);        
    }

    /**
     *
     * @param id
     * @param pred
     * @return
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PredictionAnalysisDTO updateAnalysis(@PathParam("id") final int id, PredictionAnalysis pred) {
        PredictionAnalysis predFromDB = predictionAnalysisRepository.findOne(id);
        Date currentTime = Calendar.getInstance().getTime();

        pred.setModifiedBy(getCurrentUser());
        pred.setModifiedDate(currentTime);
        // Prevent any updates to protected fields like created/createdBy
        pred.setCreatedDate(predFromDB.getCreatedDate());
        pred.setCreatedBy(predFromDB.getCreatedBy());

        PredictionAnalysis updatedPred = this.predictionAnalysisRepository.save(pred);

        return conversionService.convert(updatedPred, PredictionAnalysisDTO.class);
    }
    
    /**
     *
     * @param id
     * @return
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/copy")
    public PredictionAnalysisDTO copy(@PathParam("id") final int id) {
        PredictionAnalysis analysis = this.predictionAnalysisRepository.findOne(id);
        entityManager.detach(analysis); // Detach from the persistence context in order to save a copy
        analysis.setId(null);
        analysis.setName(String.format(Constants.Templates.ENTITY_COPY_PREFIX, analysis.getName()));
        return this.createAnalysis(analysis);
    }
    
    /**
     *
     * @param id
     * @return
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PredictionAnalysisDTO getAnalysis(@PathParam("id") int id) {
        return getTransactionTemplate().execute(transactionStatus -> {
            PredictionAnalysis analysis = this.predictionAnalysisRepository.findOne(id);
            ExceptionUtils.throwNotFoundExceptionIfNull(analysis, String.format("There is no prediction analysis with id = %d.", id));
            return conversionService.convert(analysis, PredictionAnalysisDTO.class);
        });
    }    
    
    /**
     *
     * @param id
     * @return
     */
    @GET
    @Path("{id}/export")
    @Produces(MediaType.APPLICATION_JSON)
    public PatientLevelPredictionAnalysisImpl exportAnalysis(@PathParam("id") int id) {
        PredictionAnalysis pred = predictionAnalysisRepository.findOne(id);
        PatientLevelPredictionAnalysisImpl expression = new PatientLevelPredictionAnalysisImpl();
        try {
            expression = Utils.deserialize(pred.getSpecification(), PatientLevelPredictionAnalysisImpl.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // Set the root properties
        expression.setId(pred.getId());
        expression.setName(pred.getName());
        expression.setDescription(pred.getDescription());
        expression.setOrganizationName(env.getRequiredProperty("organization.name"));
        
        // Retrieve the cohort definition details
        List<AnalysisCohortDefinition> detailedList = new ArrayList<>();
        for (AnalysisCohortDefinition c : expression.getCohortDefinitions()) {
            CohortDefinition cd = cohortDefinitionRepository.findOneWithDetail(c.getId());
            detailedList.add(new AnalysisCohortDefinition(cd));
        }
        expression.setCohortDefinitions(detailedList);
        
        // Retrieve the concept set expressions
        List<AnalysisConceptSet> pcsList = new ArrayList<>();
        Map<Integer, List<Long>> conceptIdentifiers = new HashMap<>();
        for (AnalysisConceptSet pcs : expression.getConceptSets()) {
            pcs.expression = conceptSetService.getConceptSetExpression(pcs.id);
            pcsList.add(pcs);
            conceptIdentifiers.put(pcs.id, new ArrayList(vocabularyService.resolveConceptSetExpression(pcs.expression)));
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
        
        return expression;
    }

    @POST
    @Path("import")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public PredictionAnalysisDTO importAnalysis(PatientLevelPredictionAnalysisImpl analysis) {
        try {
            // Create all of the cohort definitions 
            // and map the IDs from old -> new
            List<BigDecimal> newTargetIds = new ArrayList<>();
            List<BigDecimal> newOutcomeIds = new ArrayList<>();
            AnalysisCohortDefinitionImportService cohortImportService = new AnalysisCohortDefinitionImportService(security, userRepository, cohortDefinitionRepository);
            analysis.getCohortDefinitions().forEach((analysisCohortDefinition) -> {
                BigDecimal oldId = new BigDecimal(analysisCohortDefinition.getId());
                CohortDefinition cd = cohortImportService.persistCohort(analysisCohortDefinition);
                if (analysis.getTargetIds().contains(oldId)) {
                    newTargetIds.add(new BigDecimal(cd.getId()));
                }
                if (analysis.getOutcomeIds().contains(oldId)) {
                    newOutcomeIds.add(new BigDecimal(cd.getId()));
                }
                analysisCohortDefinition.setId(cd.getId());
            });
            
            // Create all of the concept sets and map
            // the IDs from old -> new
            Map<Integer, Integer> conceptSetIdMap = new HashMap<>();
            AnalysisConceptSetImportService conceptSetImportService = new AnalysisConceptSetImportService(conceptSetService);
            analysis.getConceptSets().forEach((pcs) -> { 
               int oldId = pcs.id;
               ConceptSetDTO cs = conceptSetImportService.persistConceptSet(pcs);
               pcs.id = cs.getId();
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
            pa.setName(String.format(ENTITY_COPY_PREFIX, analysis.getName()));
            pa.setSpecification(Utils.serialize(analysis));
            
            return this.createAnalysis(pa);
        } catch (RuntimeException e) {
            log.debug("Error whie importing prediction analysis: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     *
     * @param id
     * @param packageName
     * @return
     * @throws IOException
     */
    @GET
    @Path("{id}/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPackage(@PathParam("id") int id, @QueryParam("packageName") String packageName) throws IOException {
        if (packageName == null) {
            packageName = "prediction" + String.valueOf(id);
        }
        if (!Utils.isAlphaNumeric(packageName)) {
            throw new IllegalArgumentException("The package name must be alphanumeric only.");
        }
        PatientLevelPredictionAnalysisImpl plpa = this.exportAnalysis(id);
        plpa.setPackageName(packageName);
        String studySpecs = Utils.serialize(plpa, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Hydra h = new Hydra(studySpecs);
        h.hydrate(baos);
        
        
        Response response = Response
                .ok(baos)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", String.format("attachment; filename=\"prediction_study_%d_export.zip\"", id))
                .build();

        return response;     
    }
}
