package org.ohdsi.webapi.shiro.management;

public enum CreatePermTemplates {
    CREATE_COHORT_DEFINITION("createPermissionsOnCreateCohortDefinition"), 
    CREATE_COHORT_CHARACTERIZATION("createPermissionsOnCreateCohortCharacterization"),
    CREATE_PATHWAY_ANALYSIS("createPermissionsOnCreatePathwayAnalysis"),
    CREATE_FEATURE_ANALYSIS("createPermissionsOnCreateFeatureAnalysis"),
    CREATE_CONCEPT_SET("createPermissionsOnCreateConceptSet"),
    CREATE_IR("createPermissionsOnCreateIR"),
    CREATE_COPY_IR("createPermissionsOnCopyIR"),
    CREATE_PLE("createPermissionsOnCreatePle"),
    CREATE_PLP("createPermissionsOnCreatePlp"),
    CREATE_COPY_PLP("createPermissionsOnCopyPlp"),
    CREATE_SOURCE("createPermissionsOnCreateSource"),
    CREATE_PREDICTION("createPermissionsOnCreatePrediction"),
    CREATE_ESTIMATION("createPermissionsOnCreateEstimation");
    
    private String templateName;
    CreatePermTemplates(String templateName){
        this. templateName = templateName;
    }

    public String getTemplateName() {
        return templateName;
    }
}
