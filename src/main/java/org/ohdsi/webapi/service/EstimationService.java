package org.ohdsi.webapi.service;

import org.ohdsi.webapi.estimation.comparativecohortanalysis.specification.TargetComparatorOutcomesImpl;
import org.ohdsi.webapi.estimation.comparativecohortanalysis.specification.ComparativeCohortAnalysisImpl;
import org.ohdsi.webapi.estimation.comparativecohortanalysis.specification.CohortMethodAnalysisImpl;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
import org.ohdsi.analysis.estimation.design.EstimationTypeEnum;
import org.ohdsi.analysis.estimation.design.NegativeControlTypeEnum;
import org.ohdsi.analysis.estimation.design.Settings;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpression.ConceptSetItem;
import org.ohdsi.hydra.Hydra;
import static org.ohdsi.webapi.Constants.Templates.ENTITY_COPY_PREFIX;
import org.ohdsi.webapi.analysis.importer.AnalysisConceptSetImportService;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.conceptset.ConceptSetCrossReferenceImpl;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.EstimationListItem;
import org.ohdsi.webapi.estimation.EstimationRepository;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.specification.*;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;
import org.ohdsi.webapi.featureextraction.specification.CovariateSettingsImpl;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.util.ExceptionUtils;
import org.ohdsi.webapi.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.web.bind.annotation.RestController;

import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.analysis.AnalysisConceptSet;
import org.ohdsi.webapi.analysis.importer.AnalysisCohortDefinitionImportService;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;

@RestController
@Path("/estimation/")
@Transactional
public class EstimationService extends AbstractDaoService {
    
    private static final String CONCEPT_SET_XREF_KEY_TARGET_COMPARATOR_OUTCOME = "estimationAnalysisSettings.analysisSpecification.targetComparatorOutcomes";
    private static final String CONCEPT_SET_XREF_KEY_NEGATIVE_CONTROL_OUTCOMES = "negativeControlOutcomes";
    private static final String CONCEPT_SET_XREF_KEY_COHORT_METHOD_COVAR = "estimationAnalysisSettings.analysisSpecification.cohortMethodAnalysisList.getDbCohortMethodDataArgs.covariateSettings";
    private static final String CONCEPT_SET_XREF_KEY_POS_CONTROL_COVAR = "positiveControlSynthesisArgs.covariateSettings";
    private static final String CONCEPT_SET_XREF_KEY_INCLUDED_COVARIATE_CONCEPT_IDS = "includedCovariateConceptIds";
    private static final String CONCEPT_SET_XREF_KEY_EXCLUDED_COVARIATE_CONCEPT_IDS = "excludedCovariateConceptIds";
    
    @Autowired
    private Security security;
    
    /**
     *
     */
    @PersistenceContext
    protected EntityManager entityManager;
    
    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;

    @Autowired
    private ConceptSetService conceptSetService;
    
    @Autowired
    private VocabularyService vocabularyService;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GenericConversionService conversionService;
    
    @Autowired
    private EstimationRepository estimationRepository;
    
    @Value("${organization.name}")
    private String organizationName;
    
    /**
     *
     * @return
     */
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EstimationListItem> getAnalysisList() {
        return StreamSupport.stream(estimationRepository.findAll().spliterator(), false)
            .map(est -> {
              EstimationListItem item = new EstimationListItem();
              item.estimationId = est.getId();
              item.name = est.getName();
              item.type = est.getType();
              item.description = est.getDescription();
              item.createdBy = UserUtils.nullSafeLogin(est.getCreatedBy());
              item.createdDate = est.getCreatedDate();
              item.modifiedBy = UserUtils.nullSafeLogin(est.getModifiedBy());
              item.modifiedDate = est.getModifiedDate();
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
        this.estimationRepository.delete(id);
    }
    
    /**
     *
     * @param est
     * @return
     */
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public EstimationDTO createEstimation(Estimation est) {
        Date currentTime = Calendar.getInstance().getTime();

        UserEntity user = userRepository.findByLogin(security.getSubject());
        est.setCreatedBy(user);
        est.setCreatedDate(currentTime);

        Estimation estWithId = this.estimationRepository.save(est);
        return conversionService.convert(estWithId, EstimationDTO.class);
    }

    /**
     *
     * @param id
     * @param est
     * @return
     */
    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public EstimationDTO updateEstimation(@PathParam("id") final int id, Estimation est) {
        Estimation estFromDB = estimationRepository.findOne(id);
        Date currentTime = Calendar.getInstance().getTime();

        UserEntity user = userRepository.findByLogin(security.getSubject());
        est.setModifiedBy(user);
        est.setModifiedDate(currentTime);
        // Prevent any updates to protected fields like created/createdBy
        est.setCreatedDate(estFromDB.getCreatedDate());
        est.setCreatedBy(estFromDB.getCreatedBy());

        Estimation updatedEst = this.estimationRepository.save(est);

        return conversionService.convert(updatedEst, EstimationDTO.class);
    }
    
    /**
     * Create a copy of the estimation using the ID
     * @param id The identifier for the estimation specification
     * @return The new estimation copy
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/copy")
    public EstimationDTO copy(@PathParam("id") final int id) {
        Estimation est = this.estimationRepository.findOne(id);
        entityManager.detach(est); // Detach from the persistence context in order to save a copy
        est.setId(null);
        est.setName(String.format(ENTITY_COPY_PREFIX, est.getName()));
        return this.createEstimation(est);
    }
    
    /**
     * Retrieve the estimation specification
     * @param id The ID of the estimation to retrieve
     * @return The estimation specification
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public EstimationDTO getAnalysis(@PathParam("id") int id) {
        return getTransactionTemplate().execute(transactionStatus -> {
            Estimation est = this.estimationRepository.findOne(id);
            ExceptionUtils.throwNotFoundExceptionIfNull(est, String.format("There is no estimation with id = %d.", id));
            return conversionService.convert(est, EstimationDTO.class);
        });
    }
    
    /**
     * Export the full estimation specification for exchange between environments
     * @param id The ID of the estimation
     * @return The full estimation specification
     */
    @GET
    @Path("{id}/export")
    @Produces(MediaType.APPLICATION_JSON)
    public EstimationAnalysisImpl exportAnalysis(@PathParam("id") int id) {
        Estimation est = estimationRepository.findOne(id);
        EstimationAnalysisImpl expression = new EstimationAnalysisImpl();
        try {
            expression = Utils.deserialize(est.getSpecification(), EstimationAnalysisImpl.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        // Set the root properties
        expression.setId(est.getId());
        expression.setName(est.getName());
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
            pcs.expression = conceptSetService.getConceptSetExpression(pcs.id);
            csExpressionList.put(pcs.id, pcs.expression);
            ecsList.add(pcs);
            conceptIdentifiers.put(pcs.id, new ArrayList(vocabularyService.resolveConceptSetExpression(pcs.expression)));
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
        
        return expression;
    }
    
    /**
     * Import the full estimation specification
     * @param analysis The full estimation specification
     * @return The newly imported estimation
     */
    @POST
    @Path("import")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public EstimationDTO importAnalysis(EstimationAnalysisImpl analysis) {
        try {
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
            AnalysisCohortDefinitionImportService cohortImportService = new AnalysisCohortDefinitionImportService(security, userRepository, cohortDefinitionRepository);
            analysis.getCohortDefinitions().forEach((analysisCohortDefinition) -> {
                CohortDefinition cd = cohortImportService.persistCohort(analysisCohortDefinition);
                cohortIds.put(Long.valueOf(analysisCohortDefinition.getId()), Long.valueOf(cd.getId()));
                analysisCohortDefinition.setId(cd.getId());
                log.debug("cohort created: " + cd.getId());
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
            
            // Replace all of the negative controls with the new IDs
            analysis.getNegativeControls().forEach((nc) -> {
                Long newT = cohortIds.get(nc.getTargetId());
                Long newC = cohortIds.get(nc.getComparatorId());
                nc.setTargetId(newT);
                nc.setComparatorId(newC);
                // No need to set the outcomes since these are concept IDs
                // and will transfer in fine
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
            
            // Remove the ID
            analysis.setId(null);
            
            // Create the estimation 
            Estimation est = new Estimation();
            est.setDescription(analysis.getDescription());
            est.setName(String.format(ENTITY_COPY_PREFIX, analysis.getName()));
            est.setType(EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS);
            est.setSpecification(Utils.serialize(analysis));
            
            return this.createEstimation(est);
        } catch (RuntimeException e) {
            log.debug("Error whie importing estimation analysis: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Download an R package to execute the estimation study
     * @param id The id for the estimation study
     * @param packageName The R package name for the study
     * @return Binary zip file containing the full R package
     * @throws IOException
     */
    @GET
    @Path("{id}/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response download(@PathParam("id") int id, @QueryParam("packageName") String packageName) throws IOException {
        if (packageName == null) {
            packageName = "estimation" + String.valueOf(id);
        }
        if (!Utils.isAlphaNumeric(packageName)) {
            throw new IllegalArgumentException("The package name must be alphanumeric only.");
        }
        
        EstimationAnalysisImpl analysis = this.exportAnalysis(id);
        analysis.setPackageName(packageName);
        String studySpecs = Utils.serialize(analysis, true);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        Hydra h = new Hydra(studySpecs);
        h.hydrate(baos);

        
        Response response = Response
                .ok(baos)
                .type(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", String.format("attachment; filename=\"estimation_%d.zip\"", id))
                .build();
        
        return response;
    }
}
