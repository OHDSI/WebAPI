package org.ohdsi.webapi.shiro.management;

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
import javax.ws.rs.HttpMethod;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.filter.authz.SslFilter;
import org.apache.shiro.web.filter.session.NoSessionCreationFilter;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.OidcConfCreator;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.PermissionManager;
import org.ohdsi.webapi.shiro.filters.CorsFilter;
import org.ohdsi.webapi.shiro.filters.ForceSessionCreationFilter;
import org.ohdsi.webapi.shiro.filters.ProcessResponseContentFilterImpl;
import org.ohdsi.webapi.shiro.filters.SkipFurtherFilteringFilter;
import org.ohdsi.webapi.shiro.filters.UrlBasedAuthorizingFilter;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import waffle.shiro.negotiate.NegotiateAuthenticationStrategy;

import static org.ohdsi.webapi.shiro.management.FilterTemplates.AUTHZ;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CORS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COHORT_CHARACTERIZATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COHORT_DEFINITION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_CONCEPT_SET;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COPY_COHORT_DEFINITION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COPY_IR;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COPY_PLP;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_IR;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_PLE;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_PLP;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_SOURCE;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_ESTIMATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_FEATURE_ANALYSIS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_PATHWAY_ANALYSIS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_PREDICTION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.DELETE_COHORT_CHARACTERIZATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.DELETE_COHORT_DEFINITION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.DELETE_CONCEPT_SET;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.DELETE_ESTIMATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.DELETE_FEATURE_ANALYSIS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.DELETE_PATHWAY_ANALYSIS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.DELETE_PLE;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.DELETE_PLP;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.DELETE_PREDICTION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.DELETE_SOURCE;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.FORCE_SESSION_CREATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.JWT_AUTHC;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.NO_SESSION_CREATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SKIP_IF_NOT_POST;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SKIP_IF_NOT_PUT;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SKIP_IF_NOT_PUT_OR_DELETE;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SKIP_IF_NOT_PUT_OR_POST;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.SSL;

/**
 *
 * @author gennadiy.anisimov
 */
public abstract class AtlasSecurity extends Security {
  public static final String TOKEN_ATTRIBUTE = "TOKEN";
  public static final String AUTH_FILTER_ATTRIBUTE = "AuthenticatingFilter";
  public static final String PERMISSIONS_ATTRIBUTE = "PERMISSIONS";
  private final Logger log = LoggerFactory.getLogger(getClass());

  @Autowired
  protected PermissionManager authorizer;

  @Autowired
  SourceRepository sourceRepository;

  @Autowired
  private ApplicationEventPublisher eventPublisher;
  
  @Autowired
  OidcConfCreator oidcConfCreator;

  @Value("${server.port}")
  private int sslPort;

  @Value("${security.ssl.enabled}")
  private boolean sslEnabled;

  protected final Set<String> defaultRoles = new LinkedHashSet<>();

  private final Map<String, String> cohortdefinitionCreatorPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> cohortCharacterizationCreatorPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> pathwayAnalysisCreatorPermissionTemplate = new LinkedHashMap<>();
  private final Map<String, String> conceptsetCreatorPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> sourcePermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> incidenceRatePermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> plePermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> plpPermissionTemplate = new LinkedHashMap<>();
  private final Map<String, String> estimationPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> predictionPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> dataSourcePermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> featureAnalysisPermissionTemplates = new LinkedHashMap<>();
  private final Map<FilterTemplates, Filter> filters = new HashMap<>();

  public AtlasSecurity() {
    this.defaultRoles.add("public");

    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:put", "Update Cohort Definition with ID = %s");
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:delete", "Delete Cohort Definition with ID = %s");

    this.conceptsetCreatorPermissionTemplates.put("conceptset:%s:put", "Update Concept Set with ID = %s");
    this.conceptsetCreatorPermissionTemplates.put("conceptset:%s:items:put", "Update Items of Concept Set with ID = %s");
    this.conceptsetCreatorPermissionTemplates.put("conceptset:%s:delete", "Delete Concept Set with ID = %s");

    this.sourcePermissionTemplates.put("cohortdefinition:*:report:%s:get", "Get Inclusion Rule Report for Source with SourceKey = %s");
    this.sourcePermissionTemplates.put("cohortdefinition:*:generate:%s:get", "Generate Cohort on Source with SourceKey = %s");
    this.sourcePermissionTemplates.put(SOURCE_ACCESS_PERMISSION, "Access to Source with SourceKey = %s");

    this.cohortCharacterizationCreatorPermissionTemplates.put("cohort-characterization:%s:put", "Update Cohort Characterization with ID = %s");
    this.cohortCharacterizationCreatorPermissionTemplates.put("cohort-characterization:%s:delete", "Delete Cohort Characterization with ID = %s");
    this.cohortCharacterizationCreatorPermissionTemplates.put("cohort-characterization:%s:generation:*:post", "Generate Cohort Characterization with ID = %s");
    this.cohortCharacterizationCreatorPermissionTemplates.put("cohort-characterization:%s:generation:*:delete", "Cancel Generation of Cohort Characterization with ID = %s");

    this.pathwayAnalysisCreatorPermissionTemplate.put("pathway-analysis:%s:put", "Update Pathway Analysis with ID = %s");
    this.pathwayAnalysisCreatorPermissionTemplate.put("pathway-analysis:%s:sql:*:get", "Get analysis sql for Pathway Analysis with ID = %s");
    this.pathwayAnalysisCreatorPermissionTemplate.put("pathway-analysis:%s:generation:*:post", "Generate Pathway Analysis with ID = %s");
    this.pathwayAnalysisCreatorPermissionTemplate.put("pathway-analysis:%s:generation:*:delete", "Cancel Generation of Pathway Analysis with ID = %s");
    this.pathwayAnalysisCreatorPermissionTemplate.put("pathway-analysis:%s:delete", "Delete Pathway Analysis with ID = %s");

    this.featureAnalysisPermissionTemplates.put("feature-analysis:%s:put", "Update Feature Analysis with ID = %s");
    this.featureAnalysisPermissionTemplates.put("feature-analysis:%s:delete", "Delete Feature Analysis with ID = %s");

    this.incidenceRatePermissionTemplates.put("ir:%s:get", "Read Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:execution:*:get", "Execute Incidence Rate job with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:execution:*:delete", "Cancel Execution of Incidence Rate job with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:info:get", "Read Incidence Rate info with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:report:*:get", "Report Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:copy:get", "Copy Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:export:get", "Export Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:put", "Edit Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:delete", "Delete Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:info:*:delete", "Delete Incidence Rate info with ID=%s");

    // TODO: to be removed together with old PLE controller
    this.plePermissionTemplates.put("comparativecohortanalysis:%s:put", "Edit Estimation with ID=%s");
    this.plePermissionTemplates.put("comparativecohortanalysis:%s:delete", "Delete Estimation with ID=%s");

    // TODO: to be removed together with old PLP controller
    this.plpPermissionTemplate.put("plp:%s:put", "Edit Population Level Prediction with ID=%s");
    this.plpPermissionTemplate.put("plp:%s:delete", "Delete Population Level Prediction with ID=%s");
    this.plpPermissionTemplate.put("plp:%s:get", "Read Population Level Prediction with ID=%s");
    this.plpPermissionTemplate.put("plp:%s:copy:get", "Copy Population Level Prediction with ID=%s");

    this.dataSourcePermissionTemplates.put("source:%s:put", "Edit Source with sourceKey=%s");
    this.dataSourcePermissionTemplates.put("source:%s:get", "Read Source with sourceKey=%s");
    this.dataSourcePermissionTemplates.put("source:%s:delete", "Delete Source with sourceKey=%s");

    this.estimationPermissionTemplates.put("estimation:%s:put", "Edit Estimation with ID=%s");
    this.estimationPermissionTemplates.put("estimation:%s:delete", "Delete Estimation with ID=%s");

    this.predictionPermissionTemplates.put("prediction:%s:put", "Edit Estimation with ID=%s");
    this.predictionPermissionTemplates.put("prediction:%s:delete", "Delete Estimation with ID=%s");
  }

  @PostConstruct
  private void init() {
    fillFilters();
    initRolesForSources();
  }

  private void initRolesForSources() {
    try {
      for (Source source : sourceRepository.findAll()) {
        this.addSourceRole(source.getSourceKey());
      }
    }
    catch (Exception e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public Map<String, String> getFilterChain() {

      return getFilterChainBuilder().build();
  }

  protected abstract FilterChainBuilder getFilterChainBuilder();

  protected FilterChainBuilder setupProtectedPaths(FilterChainBuilder filterChainBuilder) {

    return filterChainBuilder

            // permissions
            .addProtectedRestPath("/user/**")
            .addProtectedRestPath("/role/**")
            .addProtectedRestPath("/permission/**")

            // concept set
            .addProtectedRestPath("/conceptset", JWT_AUTHC, AUTHZ,CREATE_CONCEPT_SET)
            .addProtectedRestPath("/conceptset/*/items", JWT_AUTHC, AUTHZ)
            .addProtectedRestPath("/conceptset/*", JWT_AUTHC, AUTHZ, DELETE_CONCEPT_SET)

            // incidence rates
            .addProtectedRestPath("/ir", JWT_AUTHC, AUTHZ, CREATE_IR)
            .addProtectedRestPath("/ir/*/copy", CREATE_COPY_IR)
            .addProtectedRestPath("/ir/*", JWT_AUTHC, AUTHZ)
            .addProtectedRestPath("/ir/*/execute/*")

            // comparative cohort analysis (estimation)
            .addProtectedRestPath("/comparativecohortanalysis", CREATE_PLE)
            .addProtectedRestPath("/comparativecohortanalysis/*", DELETE_PLE)

            // new estimation
            .addProtectedRestPath("/estimation", CREATE_ESTIMATION)
            .addProtectedRestPath("/estimation/*/copy", CREATE_ESTIMATION)
            .addProtectedRestPath("/estimation/*", DELETE_ESTIMATION)
            .addProtectedRestPath("/estimation/*/export")
            .addProtectedRestPath("/estimation/*/download")

            // population level prediction
            .addProtectedRestPath("/plp", CREATE_PLP)
            .addProtectedRestPath("/plp/*/copy", CREATE_COPY_PLP)
            .addProtectedRestPath("/plp/*", DELETE_PLP)

            // new prediction
            .addProtectedRestPath("/prediction", CREATE_PREDICTION)
            .addProtectedRestPath("/prediction/*/copy", CREATE_PREDICTION)
            .addProtectedRestPath("/prediction/*", DELETE_PREDICTION)
            .addProtectedRestPath("/prediction/*/export")
            .addProtectedRestPath("/prediction/*/download")

            // cohort definition
            .addProtectedRestPath("/cohortdefinition", CREATE_COHORT_DEFINITION)
            .addProtectedRestPath("/cohortdefinition/*/copy", CREATE_COPY_COHORT_DEFINITION)
            .addProtectedRestPath("/cohortdefinition/*", DELETE_COHORT_DEFINITION)
            .addProtectedRestPath("/cohortdefinition/*/info")
            .addProtectedRestPath("/cohortdefinition/sql")
            .addProtectedRestPath("/cohortdefinition/*/generate/*")
            .addProtectedRestPath("/cohortdefinition/*/report/*")
            .addProtectedRestPath("/*/cohortresults/*/breakdown")
            .addProtectedRestPath("/job/execution")
            .addProtectedRestPath("/job")

            // configuration
            .addProtectedRestPath("/source/refresh")
            .addProtectedRestPath("/source/priorityVocabulary")
            .addRestPath("/source/sources")
            .addProtectedRestPath("/source/connection/*")
            .addProtectedRestPath("/source", CREATE_SOURCE)
            .addProtectedRestPath("/source/*", DELETE_SOURCE)
            .addProtectedRestPath("/source/*/daimons/*/set-priority")
            .addProtectedRestPath("/source/details/*")

            // cohort analysis
            .addProtectedRestPath("/cohortanalysis")
            .addProtectedRestPath("/cohortanalysis/*")

            // cohort results
            .addProtectedRestPath("/cohortresults/*")

            // cohort characterization
            .addProtectedRestPath("/cohort-characterization", CREATE_COHORT_CHARACTERIZATION)
            .addProtectedRestPath("/cohort-characterization/import", CREATE_COHORT_CHARACTERIZATION)
            .addProtectedRestPath("/cohort-characterization/*", DELETE_COHORT_CHARACTERIZATION)
            .addProtectedRestPath("/cohort-characterization/*/generation/*")
            .addProtectedRestPath("/cohort-characterization/*/generation")
            .addProtectedRestPath("/cohort-characterization/generation/*")
            .addProtectedRestPath("/cohort-characterization/generation/*/design")
            .addProtectedRestPath("/cohort-characterization/generation/*/result")
            .addProtectedRestPath("/cohort-characterization/*/export")

            // Pathways Analyses
            .addProtectedRestPath("/pathway-analysis", CREATE_PATHWAY_ANALYSIS)
            .addProtectedRestPath("/pathway-analysis/import", CREATE_PATHWAY_ANALYSIS)
            .addProtectedRestPath("/pathway-analysis/*", DELETE_PATHWAY_ANALYSIS)
            .addProtectedRestPath("/pathway-analysis/*/sql/*")
            .addProtectedRestPath("/pathway-analysis/*/generation/*")
            .addProtectedRestPath("/pathway-analysis/*/generation")
            .addProtectedRestPath("/pathway-analysis/generation/*")
            .addProtectedRestPath("/pathway-analysis/generation/*/design")
            .addProtectedRestPath("/pathway-analysis/generation/*/result")
            .addProtectedRestPath("/pathway-analysis/*/export")

            // feature analyses
            .addProtectedRestPath("/feature-analysis", CREATE_FEATURE_ANALYSIS)
            .addProtectedRestPath("/feature-analysis/*", DELETE_FEATURE_ANALYSIS)

            // evidence
            .addProtectedRestPath("/evidence/*")
            .addProtectedRestPath("/evidence/*/negativecontrols")

            // execution service
            .addProtectedRestPath("/executionservice/*")
            .addProtectedRestPath("/executionservice/execution/run")

            // feasibility
            .addProtectedRestPath("/feasibility")
            .addProtectedRestPath("/feasibility/*")

            // featureextraction
            .addProtectedRestPath("/featureextraction/*")

            // vocabulary services
            .addProtectedRestPath("/vocabulary/*")

            // data sources
            .addProtectedRestPath("/cdmresults/*")

            // profiles
            .addProtectedRestPath("/*/person/*")

            // notifications
            .addProtectedRestPath("/notifications/viewed")
            .addProtectedRestPath("/notifications");
  }

  @Override
  public Map<FilterTemplates, Filter> getFilters(){
    return new HashMap<>(filters);
  }
  
  private void fillFilters() {
    
    filters.put(NO_SESSION_CREATION, new NoSessionCreationFilter());
    filters.put(FORCE_SESSION_CREATION, new ForceSessionCreationFilter());
    filters.put(AUTHZ, new UrlBasedAuthorizingFilter());
    filters.put(CORS, new CorsFilter());
    filters.put(SKIP_IF_NOT_POST, this.getSkipFurtherFiltersIfNotPostFilter());
    filters.put(SKIP_IF_NOT_PUT, this.getSkipFurtherFiltersIfNotPutFilter());
    filters.put(SKIP_IF_NOT_PUT_OR_POST, this.getskipFurtherFiltersIfNotPutOrPostFilter());
    filters.put(SKIP_IF_NOT_PUT_OR_DELETE, this.getskipFurtherFiltersIfNotPutOrDeleteFilter());
    filters.put(SSL, this.getSslFilter());
    
    filters.put(DELETE_COHORT_CHARACTERIZATION, this.getDeletePermissionsOnDeleteFilter(cohortCharacterizationCreatorPermissionTemplates));
    filters.put(DELETE_PATHWAY_ANALYSIS, this.getDeletePermissionsOnDeleteFilter(pathwayAnalysisCreatorPermissionTemplate));
    filters.put(DELETE_FEATURE_ANALYSIS, this.getDeletePermissionsOnDeleteFilter(featureAnalysisPermissionTemplates));
    filters.put(DELETE_COHORT_DEFINITION, this.getDeletePermissionsOnDeleteFilter(cohortdefinitionCreatorPermissionTemplates));
    filters.put(DELETE_CONCEPT_SET, this.getDeletePermissionsOnDeleteFilter(conceptsetCreatorPermissionTemplates));
    filters.put(DELETE_PLE, this.getDeletePermissionsOnDeleteFilter(plePermissionTemplates));
    filters.put(DELETE_PLP, this.getDeletePermissionsOnDeleteFilter(plpPermissionTemplate));
    filters.put(DELETE_SOURCE, this.getDeletePermissionsOnDeleteFilter(dataSourcePermissionTemplates));
    filters.put(DELETE_PREDICTION, this.getDeletePermissionsOnDeleteFilter(predictionPermissionTemplates));
    filters.put(DELETE_ESTIMATION, this.getDeletePermissionsOnDeleteFilter(estimationPermissionTemplates));
    
    addProcessEntityFilter(CREATE_COPY_COHORT_DEFINITION, cohortdefinitionCreatorPermissionTemplates);
    addProcessEntityFilter(CREATE_COPY_IR, incidenceRatePermissionTemplates);
    addProcessEntityFilter(CREATE_COPY_PLP, plpPermissionTemplate);
    addProcessEntityFilter(CREATE_COHORT_DEFINITION, cohortdefinitionCreatorPermissionTemplates);
    addProcessEntityFilter(CREATE_COHORT_CHARACTERIZATION, cohortCharacterizationCreatorPermissionTemplates);
    addProcessEntityFilter(CREATE_PATHWAY_ANALYSIS, pathwayAnalysisCreatorPermissionTemplate);
    addProcessEntityFilter(CREATE_FEATURE_ANALYSIS, featureAnalysisPermissionTemplates);
    addProcessEntityFilter(CREATE_CONCEPT_SET, conceptsetCreatorPermissionTemplates);
    addProcessEntityFilter(CREATE_IR, incidenceRatePermissionTemplates);    
    addProcessEntityFilter(CREATE_PLE, estimationPermissionTemplates);
    addProcessEntityFilter(CREATE_PLP, plpPermissionTemplate);
    addProcessEntityFilter(CREATE_SOURCE, dataSourcePermissionTemplates);
    addProcessEntityFilter(CREATE_PREDICTION, predictionPermissionTemplates);
    addProcessEntityFilter(CREATE_ESTIMATION, estimationPermissionTemplates);    
  }

  private void addProcessEntityFilter(FilterTemplates template, Map<String, String> permissionTemplates){
    filters.put(template, new ProcessResponseContentFilterImpl(permissionTemplates, template.getEntityName(), authorizer, eventPublisher, template.getHttpMethod()));
  }

  @Override
  public Set<Realm> getRealms() {

    return new LinkedHashSet<>();
  }

  @Override
  public Authenticator getAuthenticator() {
    ModularRealmAuthenticator authenticator = new ModularRealmAuthenticator();
    authenticator.setAuthenticationStrategy(new NegotiateAuthenticationStrategy());

    return authenticator;
  }

  private String getSourceRoleName(String sourceKey) {
    return String.format("Source user (%s)", sourceKey);
  }

  @Override
  public void addSourceRole(String sourceKey) throws Exception {
    final String roleName = getSourceRoleName(sourceKey);
    if (this.authorizer.roleExists(roleName)) {
      return;
    }

    RoleEntity role = this.authorizer.addRole(roleName, true);
    this.authorizer.addPermissionsFromTemplate(role, this.sourcePermissionTemplates, sourceKey);
  }

  @Override
  public void removeSourceRole(String sourceKey) throws Exception {
    final String roleName = getSourceRoleName(sourceKey);
    if (this.authorizer.roleExists(roleName)) {
      RoleEntity role = this.authorizer.getRoleByName(roleName);
      this.authorizer.removePermissionsFromTemplate(this.sourcePermissionTemplates, sourceKey);
      this.authorizer.removePermissionsFromTemplate(this.dataSourcePermissionTemplates, sourceKey);
      this.authorizer.removeRole(role.getId());
    }
  }

  private Filter getDeletePermissionsOnDeleteFilter(Map<String, String> template) {
    return new AdviceFilter() {
      @Override
      protected void postHandle(ServletRequest request, ServletResponse response) {

        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        if (!HttpMethod.DELETE.equalsIgnoreCase(httpRequest.getMethod())) {
          return;
        }

        String id = httpRequest.getPathInfo()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .split("/")[1];
        authorizer.removePermissionsFromTemplate(template, id);
      }
    };
  }

  private Filter getSkipFurtherFiltersIfNotPostFilter() {
    return new SkipFurtherFilteringFilter() {
      @Override
      protected boolean shouldSkip(ServletRequest request, ServletResponse response) {
        return !HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }
    };
  }

  private Filter getSkipFurtherFiltersIfNotPutFilter() {
    return new SkipFurtherFilteringFilter() {
      @Override
      protected boolean shouldSkip(ServletRequest request, ServletResponse response) {
        return !HttpMethod.PUT.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }
    };
  }

  private Filter getskipFurtherFiltersIfNotPutOrPostFilter() {
    return new SkipFurtherFilteringFilter() {
      @Override
      protected boolean shouldSkip(ServletRequest request, ServletResponse response) {
        String httpMethod = WebUtils.toHttp(request).getMethod();
        return !(HttpMethod.PUT.equalsIgnoreCase(httpMethod) || HttpMethod.POST.equalsIgnoreCase(httpMethod));
      }
    };
  }

  private Filter getskipFurtherFiltersIfNotPutOrDeleteFilter() {
    return new SkipFurtherFilteringFilter() {
      @Override
      protected boolean shouldSkip(ServletRequest request, ServletResponse response) {
        String httpMethod = WebUtils.toHttp(request).getMethod();
        return !(HttpMethod.PUT.equalsIgnoreCase(httpMethod) || HttpMethod.DELETE.equalsIgnoreCase(httpMethod));
      }
    };
  }

  private Filter getSslFilter() {
    SslFilter sslFilter = new SslFilter();
    sslFilter.setPort(sslPort);
    sslFilter.setEnabled(sslEnabled);
    return sslFilter;
  }

  @Override
  public String getSubject() {
    if (SecurityUtils.getSubject().isAuthenticated())
      return authorizer.getSubjectName();
    else
      return "anonymous";
  }
}