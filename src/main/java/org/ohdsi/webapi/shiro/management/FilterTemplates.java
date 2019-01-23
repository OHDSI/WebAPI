package org.ohdsi.webapi.shiro.management;

import org.ohdsi.webapi.events.EntityName;

import static org.ohdsi.webapi.events.EntityName.COHORT;
import static org.ohdsi.webapi.events.EntityName.COHORT_CHARACTERIZATION;
import static org.ohdsi.webapi.events.EntityName.CONCEPT_SET;
import static org.ohdsi.webapi.events.EntityName.ESTIMATION;
import static org.ohdsi.webapi.events.EntityName.FEATURE_ANALYSIS;
import static org.ohdsi.webapi.events.EntityName.INCIDENCE_RATE;
import static org.ohdsi.webapi.events.EntityName.PATHWAY_ANALYSIS;
import static org.ohdsi.webapi.events.EntityName.PATIENT_LEVEL_PREDICTION;
import static org.ohdsi.webapi.events.EntityName.PREDICTION;
import static org.ohdsi.webapi.events.EntityName.SOURCE;

public enum FilterTemplates {
    CREATE_COHORT_DEFINITION("createPermissionsOnCreateCohortDefinition", COHORT), 
    CREATE_COHORT_CHARACTERIZATION("createPermissionsOnCreateCohortCharacterization", COHORT_CHARACTERIZATION),
    CREATE_PATHWAY_ANALYSIS("createPermissionsOnCreatePathwayAnalysis", PATHWAY_ANALYSIS),
    CREATE_FEATURE_ANALYSIS("createPermissionsOnCreateFeatureAnalysis", FEATURE_ANALYSIS),
    CREATE_CONCEPT_SET("createPermissionsOnCreateConceptSet", CONCEPT_SET),
    CREATE_IR("createPermissionsOnCreateIR", INCIDENCE_RATE),
    CREATE_COPY_IR("createPermissionsOnCopyIR", INCIDENCE_RATE),
    CREATE_PLE("createPermissionsOnCreatePle", ESTIMATION),
    CREATE_PLP("createPermissionsOnCreatePlp", PATIENT_LEVEL_PREDICTION),
    CREATE_COPY_PLP("createPermissionsOnCopyPlp", PATIENT_LEVEL_PREDICTION),
    CREATE_SOURCE("createPermissionsOnCreateSource", SOURCE),
    CREATE_PREDICTION("createPermissionsOnCreatePrediction", PREDICTION),
    CREATE_ESTIMATION("createPermissionsOnCreateEstimation", ESTIMATION),
    
    DELETE_COHORT_CHARACTERIZATION("deletePermissionsOnDeleteCohortCharacterization", COHORT_CHARACTERIZATION),
    DELETE_PATHWAY_ANALYSIS("deletePermissionsOnDeletePathwayAnalysis", PATHWAY_ANALYSIS),
    DELETE_FEATURE_ANALYSIS("deletePermissionsOnDeleteFeatureAnalysis", FEATURE_ANALYSIS),
    DELETE_COHORT_DEFINITION("deletePermissionsOnDeleteCohortDefinition", COHORT),
    DELETE_CONCEPT_SET("deletePermissionsOnDeleteConceptSet", CONCEPT_SET),
    DELETE_PLE("deletePermissionsOnDeletePle", ESTIMATION),
    DELETE_PLP("deletePermissionsOnDeletePlp", PATIENT_LEVEL_PREDICTION),
    DELETE_SOURCE("deletePermissionsOnDeleteSource", SOURCE),
    DELETE_PREDICTION("deletePermissionsOnDeletePrediction", PREDICTION),
    DELETE_ESTIMATION("deletePermissionsOnDeleteEstimation", ESTIMATION),
    
    SKIP_IF_NOT_POST("skipFurtherFiltersIfNotPost"),
    SKIP_IF_NOT_PUT("skipFurtherFiltersIfNotPut"),
    SKIP_IF_NOT_PUT_OR_POST("skipFurtherFiltersIfNotPutOrPost"),
    SKIP_IF_NOT_PUT_OR_DELETE("skipFurtherFiltersIfNotPutOrDelete"),


    SEND_TOKEN_IN_URL("sendTokenInUrl"),
    SEND_TOKEN_IN_HEADER("sendTokenInHeader"),
    SEND_TOKEN_IN_REDIRECT("sendTokenInRedirect"),

    JWT_AUTHC("jwtAuthc"),
    NEGOTIATE_AUTHC("negotiateAuthc"),
    GOOGLE_AUTHC("googleAuthc"),
    FACEBOOK_AUTHC("facebookAuthc"),
    CAS_AUTHC("casAuthc"),
    

    NO_SESSION_CREATION("noSessionCreation"),
    FORCE_SESSION_CREATION("forceSessionCreation"),
    AUTHZ("authz"),
    CORS("cors"),
    SSL("ssl"),
    HIDE_RESOURCE("hideResource"),
    LOGOUT("logout"),
    UPDATE_TOKEN("updateToken"),
    JDBC_FILTER("jdbcFilter"),
    KERBEROS_FILTER("kerberosFilter"),
    LDAP_FILTER("ldapFilter"),
    AD_FILTER("adFilter"),
    OIDC_AUTH("oidcAuth"),
    OAUTH_CALLBACK("oauthCallback"),
    HANDLE_UNSUCCESSFUL_OAUTH("handleUnsuccessfullOAuth"),
    HANDLE_CAS("handleCas");
    
    private String templateName;
    private EntityName entityName;
    FilterTemplates(String templateName){
        this(templateName, null);
    }
    
    FilterTemplates(String templateName, EntityName entityName){
        this.templateName = templateName;
        this.entityName = entityName;
    }
    
    public String getTemplateName() {
        return templateName;
    }

    public EntityName getEntityName() {
        return entityName;
    }
}
