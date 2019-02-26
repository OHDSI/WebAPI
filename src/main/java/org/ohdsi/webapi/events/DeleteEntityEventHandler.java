package org.ohdsi.webapi.events;

import org.ohdsi.webapi.cohortcharacterization.CcService;
import org.ohdsi.webapi.estimation.EstimationService;
import org.ohdsi.webapi.feanalysis.FeAnalysisService;
import org.ohdsi.webapi.pathway.PathwayService;
import org.ohdsi.webapi.prediction.PredictionService;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.IRAnalysisResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DeleteEntityEventHandler {

    @Autowired
    private CcService ccService;
    @Autowired
    private FeAnalysisService feAnalysisService;
    @Autowired
    private PathwayService pathwayService;
    @Autowired
    private CohortDefinitionService cohortDefinitionService;
    @Autowired
    private ConceptSetService conceptSetService;
    @Autowired
    private EstimationService estimationService;
    @Autowired
    private IRAnalysisResource irAnalysisResource;
    @Autowired
    private PredictionService predictionService;

    @EventListener
    public void delete(DeleteEntityEvent event) {

        int id = event.getId();

        switch (event.getEntityName()) {
            case COHORT_CHARACTERIZATION:
                ccService.deleteCc((long) id);
                break;
            case FEATURE_ANALYSIS:
                feAnalysisService.deleteAnalysis(event.getId());
                break;
            case PATHWAY_ANALYSIS:
                pathwayService.delete(id);
                break;
            case COHORT:
                cohortDefinitionService.delete(id);
                break;
            case CONCEPT_SET:
                conceptSetService.deleteConceptSet(id);
                break;
            case ESTIMATION:
                estimationService.delete(id);
                break;
            case INCIDENCE_RATE:
                irAnalysisResource.delete(id);
                break;
            case PREDICTION:
                predictionService.delete(id);
                break;
            default:
                throw new IllegalArgumentException("Unknown value: " + event.getEntityName().getName());

        }
    }
}
