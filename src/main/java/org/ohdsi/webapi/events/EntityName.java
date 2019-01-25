package org.ohdsi.webapi.events;

public enum EntityName {
    COHORT_CHARACTERIZATION("cohort characterization"),
    FEATURE_ANALYSIS("feature analysis"),
    PATHWAY_ANALYSIS("pathway analysis"),
    COHORT("cohort"),
    COMPARATIVE_COHORT_ANALYSIS("comparative cohort analysis"),
    CONCEPT_SET("concept set"),
    ESTIMATION("estimation"),
    INCIDENCE_RATE("incidence rate"),
    PATIENT_LEVEL_PREDICTION("patient level prediction"),
    PREDICTION("prediction"),
    SOURCE("source");
    private String name;

    EntityName(String entityName){
        this.name = entityName;
    }

    public String getName() {
        return name;
    }
}
