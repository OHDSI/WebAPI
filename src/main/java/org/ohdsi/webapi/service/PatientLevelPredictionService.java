/*
 * PLEASE NOTE: PatientLevelPredictionService will be deprecated in 
 * the next major release of WebAPI so please do not extend the functionality
 * in this service. Instead please review the new PredictionService.java
 */
package org.ohdsi.webapi.service;

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
import org.ohdsi.webapi.prediction.PatientLevelPredictionAnalysis;
import org.ohdsi.webapi.prediction.PatientLevelPredictionAnalysisInfo;
import org.ohdsi.webapi.prediction.PatientLevelPredictionAnalysisRepository;
import org.ohdsi.webapi.prediction.PatientLevelPredictionListItem;
import org.ohdsi.webapi.service.CohortDefinitionService.CohortDefinitionDTO;
import org.ohdsi.webapi.service.dto.PatientLevelPredictionAnalysisDTO;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.Entities.UserRepository;
import org.ohdsi.webapi.shiro.management.Security;
import org.ohdsi.webapi.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.stereotype.Component;

/**
 *
 * @author asena5
 */
@Component
@Transactional
@Path("/plp/")
public class PatientLevelPredictionService extends AbstractDaoService {

	@Autowired
	private Security security;

	@Autowired
	private PatientLevelPredictionAnalysisRepository patientLevelPredictionAnalysisRepository;

	@PersistenceContext
	protected EntityManager entityManager;

	@Autowired
	private CohortDefinitionService cohortDefinitionService;

	@Autowired
	private ConceptSetService conceptSetService;

	@Autowired
	private VocabularyService vocabularyService;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private GenericConversionService conversionService;

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PatientLevelPredictionListItem> getAnalysisList() {

	  return getTransactionTemplate().execute(transactionStatus ->
            StreamSupport.stream(patientLevelPredictionAnalysisRepository.findAll().spliterator(), false)
            .map(plp -> {
              PatientLevelPredictionListItem item = new PatientLevelPredictionListItem();
              item.analysisId = plp.getAnalysisId();
              item.name = plp.getName();
              item.modelType = plp.getModelType();
              item.createdBy = UserUtils.nullSafeLogin(plp.getCreatedBy());
              item.createdDate = plp.getCreatedDate();
              item.modifiedBy = UserUtils.nullSafeLogin(plp.getModifiedBy());
              item.modifiedDate = plp.getModifiedDate();
              return item;
            }).collect(Collectors.toList()));
	}

	/**
	 * Deletes the specified patient level prediction analysis
	 *
	 * @param id - the Patient Level Prediction Analysis ID to delete
	 */
	@DELETE
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}")
	public void delete(@PathParam("id") final int id) {
		this.patientLevelPredictionAnalysisRepository.delete(id);
	}

	@POST
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public PatientLevelPredictionAnalysisDTO createAnalysis(PatientLevelPredictionAnalysis plpa) {

	  return getTransactionTemplate().execute(transactionStatus -> {
      Date currentTime = Calendar.getInstance().getTime();

      UserEntity user = userRepository.findByLogin(security.getSubject());
      plpa.setCreatedBy(user);
      plpa.setCreatedDate(currentTime);

      PatientLevelPredictionAnalysis plpaWithId = this.patientLevelPredictionAnalysisRepository.save(plpa);

      return conversionService.convert(plpaWithId, PatientLevelPredictionAnalysisDTO.class);
    });
	}

	@PUT
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public PatientLevelPredictionAnalysisDTO updateAnalysis(@PathParam("id") final int id, PatientLevelPredictionAnalysis plpa) {

	  return getTransactionTemplate().execute(transactionStatus -> {
      PatientLevelPredictionAnalysis plpaFromDB = patientLevelPredictionAnalysisRepository.findOne(id);
      Date currentTime = Calendar.getInstance().getTime();

      UserEntity user = userRepository.findByLogin(security.getSubject());
      plpa.setModifiedBy(user);
      plpa.setModifiedDate(currentTime);
      // Prevent any updates to protected fields like created/createdBy
      plpa.setCreatedDate(plpaFromDB.getCreatedDate());
      plpa.setCreatedBy(plpaFromDB.getCreatedBy());

      PatientLevelPredictionAnalysis updatedPlpa = this.patientLevelPredictionAnalysisRepository.save(plpa);

      return conversionService.convert(updatedPlpa, PatientLevelPredictionAnalysisDTO.class);
    });
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/copy")
	@Transactional
	public PatientLevelPredictionAnalysisDTO copy(@PathParam("id") final int id) {
		PatientLevelPredictionAnalysis analysis = this.patientLevelPredictionAnalysisRepository.findOne(id);
		entityManager.detach(analysis); // Detach from the persistance context in order to save a copy
		analysis.setAnalysisId(null);
		analysis.setName("COPY OF: " + analysis.getName());
		return this.createAnalysis(analysis);
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public PatientLevelPredictionAnalysisInfo getAnalysis(@PathParam("id") int id) {

	  return getTransactionTemplate().execute(transactionStatus -> {
      PatientLevelPredictionAnalysis analysis = this.patientLevelPredictionAnalysisRepository.findOne(id);
      PatientLevelPredictionAnalysisInfo info = conversionService.convert(analysis, PatientLevelPredictionAnalysisInfo.class);

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
      if (analysis.getCvInclusionId() > 0) {
        try {
          info.setCvInclusionCaption(conceptSetService.getConceptSet(analysis.getCvInclusionId()).getName());
          info.setCvInclusionConceptSet(conceptSetService.getConceptSetExpression(analysis.getCvInclusionId()));
          info.setCvInclusionConceptSetSql(vocabularyService.getConceptSetExpressionSQL(info.getCvInclusionConceptSet()));
        } catch (Exception e) {
          log.debug("Concept set id = " + info.getCvInclusionId() + " no longer exists");
        }
      }
      if (analysis.getCvExclusionId() > 0) {
        try {
          info.setCvExclusionCaption(conceptSetService.getConceptSet(analysis.getCvExclusionId()).getName());
          info.setCvExclusionConceptSet(conceptSetService.getConceptSetExpression(analysis.getCvExclusionId()));
          info.setCvExclusionConceptSetSql(vocabularyService.getConceptSetExpressionSQL(info.getCvExclusionConceptSet()));
        } catch (Exception e) {
          log.debug("Concept set id = " + info.getCvExclusionId() + " no longer exists");
        }
      }

      return info;
    });
	}

}
