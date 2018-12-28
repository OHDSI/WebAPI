package org.ohdsi.webapi.shiro.management;

public enum FilterTemplates {
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
    CREATE_ESTIMATION("createPermissionsOnCreateEstimation"),
    
    DELETE_COHORT_CHARACTERIZATION("deletePermissionsOnDeleteCohortCharacterization"),
    DELETE_PATHWAY_ANALYSIS("deletePermissionsOnDeletePathwayAnalysis"),
    DELETE_FEATURE_ANALYSIS("deletePermissionsOnDeleteFeatureAnalysis"),
    DELETE_COHORT_DEFINITION("deletePermissionsOnDeleteCohortDefinition"),
    DELETE_CONCEPT_SET("deletePermissionsOnDeleteConceptSet"),
    DELETE_PLE("deletePermissionsOnDeletePle"),
    DELETE_PLP("deletePermissionsOnDeletePlp"),
    DELETE_SOURCE("deletePermissionsOnDeleteSource"),
    DELETE_PREDICTION("deletePermissionsOnDeletePrediction"),
    DELETE_ESTIMATION("deletePermissionsOnDeleteEstimation"),
    
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
    FilterTemplates(String templateName){
        this.templateName = templateName;
    }
    
    public String getTemplateName() {
        return templateName;
    }
}
