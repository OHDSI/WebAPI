package org.ohdsi.webapi.events;

import org.springframework.context.ApplicationEvent;

public class DeleteEventMessageFactory {

    public static ApplicationEvent getDeletionEvent (Object source, EntityName entityName, Integer id){

        switch (entityName){
            case COHORT_CHARACTERIZATION: return new DeleteCcEvent(source, id);
            case FEATURE_ANALYSIS: return new DeleteFeatureAnalysisEvent(source, id);
            case PATHWAY_ANALYSIS: return new DeletePathwayAnalysisEvent(source, id);
            case COHORT: return new DeleteCohortEvent(source, id);
            case COMPARATIVE_COHORT_ANALYSIS: return new DeleteComparativeCohAnalysisEvent(source, id);
            case CONCEPT_SET: return new DeleteConceptSetEvent(source, id);
            case ESTIMATION: return new DeleteEstimationEvent(source, id);
            case INCIDENCE_RATE: return new DeleteIREvent(source, id);
            case PATIENT_LEVEL_PREDICTION: return new DeletePLPEvent(source, id);
            case PREDICTION: return new DeletePredictionEvent(source, id);
            case SOURCE: return new DeleteSourceEvent(source, id);
            default: throw new IllegalArgumentException("Unknown value: " + entityName.getName());
        }
    }
}
