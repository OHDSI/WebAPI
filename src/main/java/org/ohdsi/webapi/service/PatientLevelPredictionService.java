/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
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
import org.ohdsi.webapi.shiro.management.Security;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 *
 * @author asena5
 */
@Component
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

	@GET
	@Path("/")
	@Produces(MediaType.APPLICATION_JSON)
	public List<PatientLevelPredictionListItem> getAnalysisList() {
		ArrayList<PatientLevelPredictionListItem> result = new ArrayList<>();
		List<Object[]> defs = entityManager.createQuery("SELECT plp.analysisId, plp.name, plp.modelType, plp.createdBy, plp.createdDate, plp.modifiedBy, plp.modifiedDate FROM PatientLevelPredictionAnalysis plp").getResultList();
		for (Object[] d : defs) {
			PatientLevelPredictionListItem item = new PatientLevelPredictionListItem();
			item.analysisId = (Integer) d[0];
			item.name = (String) d[1];
			item.modelType = (String) d[2];
			item.createdBy = (String) d[3];
			item.createdDate = (Date) d[4];
			item.modifiedBy = (String) d[5];
			item.modifiedDate = (Date) d[6];
			result.add(item);
		}
		return result;
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
	public PatientLevelPredictionAnalysis createAnalysis(PatientLevelPredictionAnalysis plpa) {
		Date currentTime = Calendar.getInstance().getTime();

		plpa.setCreatedBy(security.getSubject());
		plpa.setCreatedDate(currentTime);

		PatientLevelPredictionAnalysis plpaWithId = this.patientLevelPredictionAnalysisRepository.save(plpa);

		return plpaWithId;
	}

	@PUT
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public PatientLevelPredictionAnalysis updateAnalysis(@PathParam("id") final int id, PatientLevelPredictionAnalysis plpa) {
		PatientLevelPredictionAnalysis plpaFromDB = this.getAnalysis(id);
		Date currentTime = Calendar.getInstance().getTime();

		plpa.setModifiedBy(security.getSubject());
		plpa.setModifiedDate(currentTime);
		// Prevent any updates to protected fields like created/createdBy
		plpa.setCreatedDate(plpaFromDB.getCreatedDate());
		plpa.setCreatedBy(plpaFromDB.getCreatedBy());

		PatientLevelPredictionAnalysis updatedPlpa = this.patientLevelPredictionAnalysisRepository.save(plpa);

		return updatedPlpa;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{id}/copy")
	@Transactional
	public PatientLevelPredictionAnalysis copy(@PathParam("id") final int id) {
		PatientLevelPredictionAnalysis analysis = this.patientLevelPredictionAnalysisRepository.findOne(id);
		entityManager.detach(analysis); // Detach from the persistance context in order to save a copy
		analysis.setAnalysisId(null);
		analysis.setName("COPY OF: " + analysis.getName());
		PatientLevelPredictionAnalysisInfo info = new PatientLevelPredictionAnalysisInfo(this.createAnalysis(analysis));
		return info;
	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public PatientLevelPredictionAnalysisInfo getAnalysis(@PathParam("id") int id) {
		PatientLevelPredictionAnalysis analysis = this.patientLevelPredictionAnalysisRepository.findOne(id);
		PatientLevelPredictionAnalysisInfo info = new PatientLevelPredictionAnalysisInfo(analysis);

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
	}

}
