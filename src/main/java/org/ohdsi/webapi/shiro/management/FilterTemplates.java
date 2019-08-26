package org.ohdsi.webapi.shiro.management;

public enum FilterTemplates {

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
    NO_CACHE("noCache"),
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
    HANDLE_CAS("handleCas"),
    RUN_AS("runAs");
    
    private String templateName;
    
    FilterTemplates(String templateName){
        this.templateName = templateName;
    }
    
    public String getTemplateName() {
        return templateName;
    }
}
