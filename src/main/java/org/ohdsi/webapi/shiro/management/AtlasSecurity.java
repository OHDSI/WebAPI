package org.ohdsi.webapi.shiro.management;

import io.buji.pac4j.filter.CallbackFilter;
import io.buji.pac4j.filter.SecurityFilter;
import io.buji.pac4j.realm.Pac4jRealm;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.session.Session;
import org.apache.shiro.web.filter.authc.AnonymousFilter;
import org.apache.shiro.web.filter.authz.SslFilter;
import org.apache.shiro.web.filter.session.NoSessionCreationFilter;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.InvalidateAccessTokenFilter;
import org.ohdsi.webapi.shiro.JwtAuthRealm;
import org.ohdsi.webapi.shiro.JwtAuthenticatingFilter;
import org.ohdsi.webapi.shiro.LogoutFilter;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.ProcessResponseContentFilter;
import org.ohdsi.webapi.shiro.SendTokenInHeaderFilter;
import org.ohdsi.webapi.shiro.SendTokenInUrlFilter;
import org.ohdsi.webapi.shiro.SkipFurtherFilteringFilter;
import org.ohdsi.webapi.shiro.UpdateAccessTokenFilter;
import org.ohdsi.webapi.shiro.UrlBasedAuthorizingFilter;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.pac4j.core.client.Clients;
import org.pac4j.core.config.Config;
import org.pac4j.oauth.client.Google2Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import waffle.shiro.negotiate.NegotiateAuthenticationFilter;
import waffle.shiro.negotiate.NegotiateAuthenticationRealm;
import waffle.shiro.negotiate.NegotiateAuthenticationStrategy;

/**
 *
 * @author gennadiy.anisimov
 */
@Component
public class AtlasSecurity extends Security {

  private final Log log = LogFactory.getLog(getClass());
  
  @Autowired
  private PermissionManager authorizer;

  @Autowired
  SourceRepository sourceRepository;

  @Value("${security.token.expiration}")
  private int tokenExpirationIntervalInSeconds;

  @Value("${security.allowOrigin}")
  private String allowOrigin;

  @Value("${security.ssl.port}")
  private int sslPort;

  @Value("${security.ssl.enabled}")
  private boolean sslEnabled;

  @Value("${security.oauth.callback.ui}")
  private String oauthUiCallback;

  @Value("${security.oauth.callback.api}")
  private String oauthApiCallback;

  @Value("${security.oauth.google.apiKey}")
  private String googleApiKey;

  @Value("${security.oauth.google.apiSecret}")
  private String googleApiSecret;

  private final Set<String> defaultRoles = new LinkedHashSet<>();

  private final Map<String, String> cohortdefinitionCreatorPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> conceptsetCreatorPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> sourcePermissionTemplates = new LinkedHashMap<>();

  public AtlasSecurity() {
    this.defaultRoles.add("public");

    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:put", "Update Cohort Definition with ID = %s");
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:delete", "Delete Cohort Definition with ID = %s");

    this.conceptsetCreatorPermissionTemplates.put("conceptset:%s:post", "Update Concept Set with ID = %s");
    this.conceptsetCreatorPermissionTemplates.put("conceptset:%s:items:post", "Update Items of Concept Set with ID = %s");
    this.conceptsetCreatorPermissionTemplates.put("conceptset:%s:delete:post", "Delete Concept Set with ID = %s");

    this.sourcePermissionTemplates.put("cohortdefinition:*:report:%s:get", "Get Inclusion Rule Report for Source with SourceKey = %s");
    this.sourcePermissionTemplates.put("cohortdefinition:*:generate:%s:get", "Generate Cohort on Source with SourceKey = %s");
  }

  @Override
  public Map<String, String> getFilterChain() {
    Map<String, String> filterChain = new LinkedHashMap<>();

    // protected resources
    //
    // user
    filterChain.put("/user/login", "ssl, noSessionCreation, cors, negotiateAuthc, updateToken, sendTokenInHeader");
    filterChain.put("/user/refresh", "ssl, noSessionCreation, cors, jwtAuthc, updateToken, sendTokenInHeader");
    filterChain.put("/user/logout", "ssl, noSessionCreation, cors, invalidateToken, logout");

    filterChain.put("/user/oauth/google", "ssl, cors, forceSessionCreation, googleAuthc, updateToken, sendTokenInUrl");
    filterChain.put("/user/oauth/callback", "ssl, oauthCallback");


    filterChain.put("/user/**", "noSessionCreation, cors, jwtAuthc, authz");
    filterChain.put("/role/**", "noSessionCreation, cors, jwtAuthc, authz");
    filterChain.put("/permission/**", "noSessionCreation, cors, jwtAuthc, authz");

    // concept set
    filterChain.put(
      "/conceptset",
      "ssl, noSessionCreation, cors, processOnlyPutAndPostRequests, jwtAuthc, authz, createPermissionsOnCreateConceptSet"); // only PUT and POST methods are protected
    filterChain.put(
      "/conceptset/",
      "ssl, noSessionCreation, cors, processOnlyPutAndPostRequests, jwtAuthc, authz, createPermissionsOnCreateConceptSet"); // only PUT and POST methods are protected
    filterChain.put("/conceptset/*/items", "ssl, noSessionCreation, cors, processOnlyPostRequests, jwtAuthc, authz"); // only POST method is protected
    filterChain.put("/conceptset/*/items/", "ssl, noSessionCreation, cors, processOnlyPostRequests, jwtAuthc, authz"); // only POST method is protected
    filterChain.put("/conceptset/*", "ssl, noSessionCreation, cors, processOnlyPostRequests, jwtAuthc, authz"); // only POST method is protected
    filterChain.put("/conceptset/*/delete", "ssl, noSessionCreation, cors, jwtAuthc, authz, deletePermissionsOnDeleteConceptSet");
    filterChain.put("/conceptset/*/delete/", "ssl, noSessionCreation, cors, jwtAuthc, authz, deletePermissionsOnDeleteConceptSet");

    // cohort definition
    filterChain.put("/cohortdefinition", "ssl, noSessionCreation, cors, jwtAuthc, authz, createPermissionsOnCreateCohortDefinition");
    filterChain.put("/cohortdefinition/", "ssl, noSessionCreation, cors, jwtAuthc, authz, createPermissionsOnCreateCohortDefinition");
    filterChain.put("/cohortdefinition/*/copy", "ssl, noSessionCreation, cors, jwtAuthc, authz, createPermissionsOnCreateCohortDefinition");
    filterChain.put("/cohortdefinition/*/copy/", "ssl, noSessionCreation, cors, jwtAuthc, authz, createPermissionsOnCreateCohortDefinition");
    filterChain.put("/cohortdefinition/*", "ssl, noSessionCreation, cors, jwtAuthc, authz, deletePermissionsOnDeleteCohortDefinition");
    filterChain.put("/cohortdefinition/*/info", "ssl, noSessionCreation, cors, jwtAuthc, authz");
    filterChain.put("/cohortdefinition/*/info/", "ssl, noSessionCreation, cors, jwtAuthc, authz");
    filterChain.put("/cohortdefinition/sql", "ssl, noSessionCreation, cors, jwtAuthc, authz");
    filterChain.put("/cohortdefinition/sql/", "ssl, noSessionCreation, cors, jwtAuthc, authz");
    filterChain.put("/cohortdefinition/*/generate/*", "ssl, noSessionCreation, cors, jwtAuthc, authz");
    filterChain.put("/cohortdefinition/*/report/*", "ssl, noSessionCreation, cors, jwtAuthc, authz");
    filterChain.put("/*/cohortresults/*/breakdown", "ssl, noSessionCreation, cors, jwtAuthc, authz");
    filterChain.put("/*/cohortresults/*/breakdown/", "ssl, noSessionCreation, cors, jwtAuthc, authz");

    // job
    filterChain.put("/job/execution", "ssl, noSessionCreation, cors, jwtAuthc, authz");
    filterChain.put("/job/execution/", "ssl, noSessionCreation, cors, jwtAuthc, authz");

    // allowed resources
    //
    filterChain.put("/**", "ssl, noSessionCreation, cors");

    return filterChain;
  }

  @Override
  public Map<String, Filter> getFilters() {
    Map<String, javax.servlet.Filter> filters = new HashMap<>();

    filters.put("logout", new LogoutFilter());
    filters.put("noSessionCreation", new NoSessionCreationFilter());
    filters.put("anon", new AnonymousFilter());
    filters.put("jwtAuthc", new JwtAuthenticatingFilter());
    filters.put("negotiateAuthc", new NegotiateAuthenticationFilter());
    filters.put("updateToken", new UpdateAccessTokenFilter(this.authorizer, this.defaultRoles, this.tokenExpirationIntervalInSeconds));
    filters.put("invalidateToken", new InvalidateAccessTokenFilter());
    filters.put("authz", new UrlBasedAuthorizingFilter(this.authorizer));
    filters.put("createPermissionsOnCreateCohortDefinition", new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String path = httpRequest.getPathInfo().replaceAll("/+$", "");

        if (StringUtils.endsWithIgnoreCase(path, "copy")) {
          return HttpMethod.GET.equalsIgnoreCase(this.getHttpMethod(request));
        } 
        else {
          return  HttpMethod.PUT.equalsIgnoreCase(this.getHttpMethod(request));
        }
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, cohortdefinitionCreatorPermissionTemplates, id);
      }
    });
    filters.put("createPermissionsOnCreateConceptSet", new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        return  HttpMethod.PUT.equalsIgnoreCase(this.getHttpMethod(request));
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, conceptsetCreatorPermissionTemplates, id);
      }
    });
    filters.put("deletePermissionsOnDeleteCohortDefinition", new AdviceFilter() {
      @Override
      protected void postHandle(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        if (!HttpMethod.DELETE.equalsIgnoreCase(httpRequest.getMethod())) {
          return;
        }

        String id = httpRequest.getPathInfo()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .split("/")
                [1];
        authorizer.removePermissionsFromTemplate(cohortdefinitionCreatorPermissionTemplates, id);
      }
    });
    filters.put("deletePermissionsOnDeleteConceptSet", new AdviceFilter() {
      @Override
      protected void postHandle(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        if (!HttpMethod.POST.equalsIgnoreCase(httpRequest.getMethod())) {
          return;
        }

        String id = httpRequest.getPathInfo()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .split("/")
                [1];
        authorizer.removePermissionsFromTemplate(conceptsetCreatorPermissionTemplates, id);
      }
    });
    filters.put("stopProcessing", new AdviceFilter() {
      @Override
      protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        return false;
      }
    });
    filters.put("cors", new AdviceFilter() {
      @Override
      protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        
        // check if it's CORS request
        //
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String origin = httpRequest.getHeader("Origin");
        if (origin == null) {
          return true;
        }

        // set headers
        //
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setHeader("Access-Control-Allow-Origin", allowOrigin);
        httpResponse.setHeader("Access-Control-Allow-Credentials", "true");

        // stop processing if it's preflight request
        //
        String requestMethod = httpRequest.getHeader("Access-Control-Request-Method");
        String method = httpRequest.getMethod();
        if (requestMethod != null && "OPTIONS".equalsIgnoreCase(method)) {
          httpResponse.setHeader("Access-Control-Allow-Headers", "origin, content-type, accept, authorization");
          httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
          httpResponse.setHeader("Access-Control-Max-Age", "1209600");
          httpResponse.setStatus(HttpServletResponse.SC_OK);

          return false;
        }

        // continue processing request
        //
        httpResponse.setHeader("Access-Control-Expose-Headers", "Bearer");
        return true;
      }
    });
    filters.put("processOnlyPostRequests", new SkipFurtherFilteringFilter() {
      @Override
      protected boolean shouldSkip(ServletRequest request, ServletResponse response) {
        return !HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }
    });
    filters.put("processOnlyPutAndPostRequests", new SkipFurtherFilteringFilter() {
      @Override
      protected boolean shouldSkip(ServletRequest request, ServletResponse response) {
        String httpMethod = WebUtils.toHttp(request).getMethod();
        return !(HttpMethod.PUT.equalsIgnoreCase(httpMethod) || HttpMethod.POST.equalsIgnoreCase(httpMethod));
      }
    });
    filters.put("sendTokenInUrl", new SendTokenInUrlFilter(this.oauthUiCallback));
    filters.put("sendTokenInHeader", new SendTokenInHeaderFilter());

    Config cfg = new Config();
    Clients clients = new Clients();
    Google2Client googleOauthClient = new Google2Client(this.googleApiKey, this.googleApiSecret);
    clients.setClients(googleOauthClient);
    clients.setCallbackUrl(this.oauthApiCallback);
    cfg.setClients(clients);
    SecurityFilter googleOauthFilter = new SecurityFilter();
    googleOauthFilter.setConfig(cfg);
    googleOauthFilter.setClients("Google2Client");
    filters.put("googleAuthc", googleOauthFilter);

    CallbackFilter callbackFilter = new CallbackFilter();
    callbackFilter.setConfig(cfg);
    filters.put("oauthCallback", callbackFilter);

    filters.put("forceSessionCreation", new AdviceFilter() {
      @Override
      protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        Session session = SecurityUtils.getSubject().getSession(true);
        if (session == null) {
          throw new Exception("Can't create web session");
        }

        return true;
      }
    });

    SslFilter sslFilter = new SslFilter();
    sslFilter.setPort(sslPort);
    sslFilter.setEnabled(sslEnabled);
    filters.put("ssl", sslFilter);
    
    return filters;
  }

  @Override
  public Set<Realm> getRealms() {
    Set<Realm> realms = new LinkedHashSet<>();

    realms.add(new JwtAuthRealm(this.authorizer));
    realms.add(new NegotiateAuthenticationRealm());
    realms.add(new Pac4jRealm());
    
    return realms;
  }

  @Override
  public Authenticator getAuthenticator() {
    ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
    authenticator.setAuthenticationStrategy(new NegotiateAuthenticationStrategy());
    
    return authenticator;
  }

  private void addSourceRole(String sourceKey) throws Exception {
    String roleName = String.format("Source user (%s)", sourceKey);
    if (this.authorizer.roleExists(roleName)) {
      return;
    }

    RoleEntity role = this.authorizer.addRole(roleName);
    this.authorizer.addPermissionsFromTemplate(role, this.sourcePermissionTemplates, sourceKey);
  }

  @PostConstruct
  private void initRolesForSources() {
     for (Source source : sourceRepository.findAll()) {
       try {
         this.addSourceRole(source.getSourceKey());
       }
       catch (Exception e) {
         log.error(e);
       }
     }
  }
}
