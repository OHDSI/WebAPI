package org.ohdsi.webapi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionDetails;
import org.ohdsi.webapi.prediction.PredictionAnalysis;
import org.ohdsi.webapi.prediction.PredictionListItem;
import org.ohdsi.webapi.prediction.PredictionAnalysisRepository;
import org.ohdsi.webapi.prediction.specification.*;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
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
    private UserRepository userRepository;
    
    @Autowired
    private GenericConversionService conversionService;
    
    @Autowired
    private CohortDefinitionService cohortDefinitionService;
    

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
    public PredictionAnalysis createAnalysis(PredictionAnalysis pred) {
        return getTransactionTemplate().execute(transactionStatus -> {
            Date currentTime = Calendar.getInstance().getTime();

            UserEntity user = userRepository.findByLogin(security.getSubject());
            pred.setCreatedBy(user);
            pred.setCreatedDate(currentTime);

            PredictionAnalysis predWithId = this.predictionAnalysisRepository.save(pred);

            return conversionService.convert(predWithId, PredictionAnalysis.class);
        });
    }

    @PUT
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public PredictionAnalysis updateAnalysis(@PathParam("id") final int id, PredictionAnalysis pred) {
        return getTransactionTemplate().execute(transactionStatus -> {
            PredictionAnalysis predFromDB = predictionAnalysisRepository.findOne(id);
            Date currentTime = Calendar.getInstance().getTime();

            UserEntity user = userRepository.findByLogin(security.getSubject());
            pred.setModifiedBy(user);
            pred.setModifiedDate(currentTime);
            // Prevent any updates to protected fields like created/createdBy
            pred.setCreatedDate(predFromDB.getCreatedDate());
            pred.setCreatedBy(predFromDB.getCreatedBy());

            PredictionAnalysis updatedPred = this.predictionAnalysisRepository.save(pred);

            return conversionService.convert(updatedPred, PredictionAnalysis.class);
        });
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}/copy")
    @Transactional
    public PredictionAnalysis copy(@PathParam("id") final int id) {
            PredictionAnalysis analysis = this.predictionAnalysisRepository.findOne(id);
            entityManager.detach(analysis); // Detach from the persistance context in order to save a copy
            analysis.setId(null);
            analysis.setName("COPY OF: " + analysis.getName());
            return this.createAnalysis(analysis);
    }
    
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public PredictionAnalysis getAnalysis(@PathParam("id") int id) {
        return getTransactionTemplate().execute(transactionStatus -> {
            PredictionAnalysis analysis = this.predictionAnalysisRepository.findOne(id);
            return analysis;
        });
    }    
    
    @GET
    @Path("{id}/export")
    @Produces(MediaType.APPLICATION_JSON)
    public PatientLevelPredictionAnalysis exportAnalysis(@PathParam("id") int id) {
        PredictionAnalysis pred = this.getAnalysis(id);
        ObjectMapper mapper = new ObjectMapper();
        PatientLevelPredictionAnalysis expression = new PatientLevelPredictionAnalysis();
        try {
            expression = mapper.readValue(pred.getSpecification(), PatientLevelPredictionAnalysis.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        ArrayList<CohortDefinitionService.CohortDefinitionDTO> detailedList = new ArrayList<CohortDefinitionService.CohortDefinitionDTO>();
        for (CohortDefinitionService.CohortDefinitionDTO c : expression.getCohortDefinitions()) {
            System.out.println(c.id);
            CohortDefinitionService.CohortDefinitionDTO cd = cohortDefinitionService.getCohortDefinition(c.id);
            detailedList.add(cd);
        }
        expression.setCohortDefinitions(detailedList);
        
        return expression;
    }
}
