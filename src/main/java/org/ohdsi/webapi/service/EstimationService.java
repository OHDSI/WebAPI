package org.ohdsi.webapi.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.ohdsi.analysis.estimation.design.NegativeControlTypeEnum;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpression.ConceptSetItem;
import org.ohdsi.hydra.Hydra;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.conceptset.ConceptSetCrossReferenceImpl;
import org.ohdsi.webapi.estimation.Estimation;
import org.ohdsi.webapi.estimation.EstimationListItem;
import org.ohdsi.webapi.estimation.EstimationRepository;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.specification.*;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;

@Controller
@Path("/estimation/")
public class EstimationService extends AbstractDaoService {
    
    private static final String CONCEPT_SET_XREF_KEY_TARGET_COMPARATOR_OUTCOME = "estimationAnalysisSettings.analysisSpecification.targetComparatorOutcomes";
    private static final String CONCEPT_SET_XREF_KEY_NEGATIVE_CONTROL_OUTCOMES = "negativeControlOutcomes";
    private static final String CONCEPT_SET_XREF_KEY_COHORT_METHOD_COVAR = "estimationAnalysisSettings.analysisSpecification.cohortMethodAnalysisList.getDbCohortMethodDataArgs.covariateSettings";
    private static final String CONCEPT_SET_XREF_KEY_POS_CONTROL_COVAR = "positiveControlSynthesisArgs.covariateSettings";
    
    @Autowired
    private Security security;
    
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

    @Autowired
    private Environment env;
    
    @Value("${organization.name}")
    private String organizationName;
    
    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EstimationListItem> getAnalysisList() {
        return getTransactionTemplate().execute(transactionStatus ->
            StreamSupport.stream(estimationRepository.findAll().spliterator(), false)
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
            }).collect(Collectors.toList()));        
    }
    
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public void delete(@PathParam("id") final int id) {
        this.estimationRepository.delete(id);
    }
    
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public EstimationDTO createEstimation(Estimation est) {
        return getTransactionTemplate().execute(transactionStatus -> {
            Date currentTime = Calendar.getInstance().getTime();

            UserEntity user = userRepository.findByLogin(security.getSubject());
            est.setCreatedBy(user);
            est.setCreatedDate(currentTime);

            Estimation estWithId = this.estimationRepository.save(est);

            return conversionService.convert(estWithId, EstimationDTO.class);
        });
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public EstimationDTO updateEstimation(@PathParam("id") final int id, Estimation est) {
        return getTransactionTemplate().execute(transactionStatus -> {
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
        });
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/copy")
    @Transactional
    public EstimationDTO copy(@PathParam("id") final int id) {
            Estimation est = this.estimationRepository.findOne(id);
            entityManager.detach(est); // Detach from the persistance context in order to save a copy
            est.setId(null);
            est.setName("COPY OF: " + est.getName());
            return this.createEstimation(est);
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public EstimationDTO getAnalysis(@PathParam("id") int id) {
        return getTransactionTemplate().execute(transactionStatus -> {
            Estimation est = this.estimationRepository.findOne(id);
            return conversionService.convert(est, EstimationDTO.class);
        });
    }
    
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
        ArrayList<EstimationCohortDefinition> detailedList = new ArrayList<>();
        for (EstimationCohortDefinition c : expression.getCohortDefinitions()) {
            CohortDefinition cd = cohortDefinitionRepository.findOneWithDetail(c.getId());
            detailedList.add(new EstimationCohortDefinition(cd));
        }
        expression.setCohortDefinitions(detailedList);
        
        // Retrieve the concept set expressions
        ArrayList<EstimationConceptSet> ecsList = new ArrayList<>();
        HashMap<Integer, ArrayList<Long>> conceptIdentifiers = new HashMap<>();
        HashMap<Integer, ConceptSetExpression> csExpressionList = new HashMap<>();
        for (EstimationConceptSet pcs : expression.getConceptSets()) {
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
                    NegativeControlImpl nc = new NegativeControlImpl();
                    nc.setTargetId(Long.valueOf((int) tco.get("targetId")));
                    nc.setComparatorId(Long.valueOf((int) tco.get("comparatorId")));
                    nc.setOutcomeId(csi.concept.conceptId);
                    nc.setOutcomeName(csi.concept.conceptName);
                    nc.setType(NegativeControlTypeEnum.OUTCOME);
                    expression.addNegativeControlsItem(nc);
                }
            } else if (xref.getTargetName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_COHORT_METHOD_COVAR)) {
                LinkedHashMap cca = ccaList.get(xref.getTargetIndex());
                LinkedHashMap dbCohortMethod = (LinkedHashMap) cca.get("getDbCohortMethodDataArgs");
                LinkedHashMap dbCohortMethodCovarSettings = (LinkedHashMap) dbCohortMethod.get("covariateSettings");
                dbCohortMethodCovarSettings.put(xref.getPropertyName(), conceptIdentifiers.get(xref.getConceptSetId()));
            } else if (xref.getTargetName().equalsIgnoreCase(CONCEPT_SET_XREF_KEY_POS_CONTROL_COVAR)) {
                if (xref.getPropertyName().equalsIgnoreCase("includedCovariateConceptIds")) {
                    expression.getPositiveControlSynthesisArgs().getCovariateSettings().setIncludedCovariateConceptIds(conceptIdentifiers.get(xref.getConceptSetId()));
                } else if (xref.getPropertyName().equalsIgnoreCase("excludedCovariateConceptIds")) {
                    expression.getPositiveControlSynthesisArgs().getCovariateSettings().setExcludedCovariateConceptIds(conceptIdentifiers.get(xref.getConceptSetId()));
                }
            }
        }
        
        return expression;
    }
    
    
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
        String studySpecs = Utils.serialize(analysis);
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
