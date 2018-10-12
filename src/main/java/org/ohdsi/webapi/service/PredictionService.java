package org.ohdsi.webapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.Hibernate;
import org.ohdsi.analysis.Utils;
import org.ohdsi.hydra.Hydra;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.PredictionListItem;
import org.ohdsi.webapi.prediction.PredictionAnalysisRepository;
import org.ohdsi.webapi.prediction.dto.PredictionAnalysisDTO;
import org.ohdsi.webapi.prediction.specification.*;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Transactional
@Path("/prediction/")
public class PredictionService  extends AbstractDaoService {
    @Autowired
    private Security security;

    @Autowired
    private PredictionAnalysisRepository predictionAnalysisRepository;

    @PersistenceContext
    protected EntityManager entityManager;

    @Autowired
    private ConceptSetService conceptSetService;
    
    @Autowired
    private VocabularyService vocabularyService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private GenericConversionService conversionService;
    
    @Autowired
    private CohortDefinitionRepository cohortDefinitionRepository;
    
    @Autowired
    private Environment env;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public List<PredictionListItem> getAnalysisList() {

      return getTransactionTemplate().execute(transactionStatus ->
        StreamSupport.stream(predictionAnalysisRepository.findAll().spliterator(), false)
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
        }).collect(Collectors.toList()));
    }
    
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public void delete(@PathParam("id") final int id) {
        this.predictionAnalysisRepository.delete(id);
    }
    
    @POST
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PredictionAnalysisDTO createAnalysis(PredictionAnalysis pred) {
        return getTransactionTemplate().execute(transactionStatus -> {
            Date currentTime = Calendar.getInstance().getTime();

            pred.setCreatedBy(getCurrentUser());
            pred.setCreatedDate(currentTime);

            PredictionAnalysis predWithId = this.predictionAnalysisRepository.save(pred);

            return conversionService.convert(predWithId, PredictionAnalysisDTO.class);
        });
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PredictionAnalysisDTO updateAnalysis(@PathParam("id") final int id, PredictionAnalysis pred) {
        return getTransactionTemplate().execute(transactionStatus -> {
            PredictionAnalysis predFromDB = predictionAnalysisRepository.findOne(id);
            Date currentTime = Calendar.getInstance().getTime();

            pred.setModifiedBy(getCurrentUser());
            pred.setModifiedDate(currentTime);
            // Prevent any updates to protected fields like created/createdBy
            pred.setCreatedDate(predFromDB.getCreatedDate());
            pred.setCreatedBy(predFromDB.getCreatedBy());

            PredictionAnalysis updatedPred = this.predictionAnalysisRepository.save(pred);

            return conversionService.convert(updatedPred, PredictionAnalysisDTO.class);
        });
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/copy")
    @Transactional
    public PredictionAnalysisDTO copy(@PathParam("id") final int id) {
            PredictionAnalysis analysis = this.predictionAnalysisRepository.findOne(id);
            entityManager.detach(analysis); // Detach from the persistance context in order to save a copy
            analysis.setId(null);
            analysis.setName("COPY OF: " + analysis.getName());
            return this.createAnalysis(analysis);
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PredictionAnalysisDTO getAnalysis(@PathParam("id") int id) {
        return getTransactionTemplate().execute(transactionStatus -> {
            PredictionAnalysis analysis = this.predictionAnalysisRepository.findOne(id);
            return conversionService.convert(analysis, PredictionAnalysisDTO.class);
        });
    }    
    
    @GET
    @Path("{id}/export")
    @Produces(MediaType.APPLICATION_JSON)
    public PatientLevelPredictionAnalysis exportAnalysis(@PathParam("id") int id) {
        PredictionAnalysis pred = predictionAnalysisRepository.findOne(id);
        ObjectMapper mapper = new ObjectMapper();
        PatientLevelPredictionAnalysis expression = new PatientLevelPredictionAnalysis();
        try {
            expression = mapper.readValue(pred.getSpecification(), PatientLevelPredictionAnalysis.class);
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
            conceptIdentifiers.put(pcs.id, new ArrayList(vocabularyService.resolveConceptSetExpression(pcs.expression)));
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
    
    @GET
    @Path("{id}/download")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadPackage(@PathParam("id") int id) throws IOException {
        PatientLevelPredictionAnalysis plpa = this.exportAnalysis(id);
        // Cannot use Utils.serialize(analysis) since it removes
        // properties with null values which are required in the
        // specification
        //String studySpecs = Utils.serialize(analysis);        
        String studySpecs = this.seralizeAnalysis(plpa);
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
    
    // NOTE: This should be replaced with SSA.serialize once issue
    // noted in the download function is addressed.
    private String seralizeAnalysis(PatientLevelPredictionAnalysis predictionAnalysis) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(
                MapperFeature.AUTO_DETECT_CREATORS,
                MapperFeature.AUTO_DETECT_GETTERS,
                MapperFeature.AUTO_DETECT_IS_GETTERS
        );

        objectMapper.disable(
                SerializationFeature.FAIL_ON_EMPTY_BEANS
        );

        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        //objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        return objectMapper.writeValueAsString(predictionAnalysis);
    }
    
}
