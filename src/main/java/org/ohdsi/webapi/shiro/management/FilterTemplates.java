package org.ohdsi.webapi.shiro.management;

import static javax.ws.rs.HttpMethod.GET;
import static javax.ws.rs.HttpMethod.POST;
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

import org.ohdsi.webapi.events.EntityName;

public enum FilterTemplates {
    CREATE_COHORT_DEFINITION("createPermissionsOnCreateCohortDefinition", COHORT, POST),
    CREATE_COPY_COHORT_DEFINITION("createPermissionsOnCopyCohortDefinition", COHORT, GET),
    CREATE_COHORT_CHARACTERIZATION("createPermissionsOnCreateCohortCharacterization", COHORT_CHARACTERIZATION, POST),
    CREATE_PATHWAY_ANALYSIS("createPermissionsOnCreatePathwayAnalysis", PATHWAY_ANALYSIS, POST),
    CREATE_FEATURE_ANALYSIS("createPermissionsOnCreateFeatureAnalysis", FEATURE_ANALYSIS, POST),
    CREATE_CONCEPT_SET("createPermissionsOnCreateConceptSet", CONCEPT_SET, POST),
    CREATE_IR("createPermissionsOnCreateIR", INCIDENCE_RATE, POST),
    CREATE_COPY_IR("createPermissionsOnCopyIR", INCIDENCE_RATE, GET),
    CREATE_SOURCE("createPermissionsOnCreateSource", SOURCE, POST),
    CREATE_PREDICTION("createPermissionsOnCreatePrediction", PREDICTION, POST),
    CREATE_ESTIMATION("createPermissionsOnCreateEstimation", ESTIMATION, POST),
    COPY_PREDICTION("createPermissionsOnCopyPrediction", PREDICTION, GET),
    COPY_ESTIMATION("createPermissionsOnCopyEstimation", ESTIMATION, GET),
    
    //old PLE & PLP
    CREATE_PLE("createPermissionsOnCreatePle", ESTIMATION, POST),
    CREATE_PLP("createPermissionsOnCreatePlp", PATIENT_LEVEL_PREDICTION, POST),
    CREATE_COPY_PLP("createPermissionsOnCopyPlp", PATIENT_LEVEL_PREDICTION, GET),
    
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
    GITHUB_AUTHC("githubAuthc"),
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
    private String httpMethod;
    
    FilterTemplates(String templateName){
        this(templateName, null, null);
    }

    FilterTemplates(String templateName, EntityName entityName){
        this(templateName, entityName, null);
    }
    
    FilterTemplates(String templateName, EntityName entityName, String httpMethod){
        this.templateName = templateName;
        this.entityName = entityName;
        this.httpMethod = httpMethod;
    }
    
    public String getTemplateName() {
        return templateName;
    }

    public EntityName getEntityName() {
        return entityName;
    }

    public String getHttpMethod() {
        return httpMethod;
    }
}
