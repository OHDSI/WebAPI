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
import org.apache.commons.lang.StringUtils;
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
import org.ohdsi.webapi.shiro.filters.ProcessResponseContentFilter;
import org.ohdsi.webapi.shiro.filters.SkipFurtherFilteringFilter;
import org.ohdsi.webapi.shiro.filters.UrlBasedAuthorizingFilter;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import waffle.shiro.negotiate.NegotiateAuthenticationStrategy;

import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_COHORT_CHARACTERIZATION;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_COHORT_DEFINITION;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_CONCEPT_SET;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_COPY_IR;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_COPY_PLP;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_IR;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_PLE;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_PLP;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_SOURCE;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_ESTIMATION;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_FEATURE_ANALYSIS;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_PATHWAY_ANALYSIS;
import static org.ohdsi.webapi.shiro.management.CreatePermTemplates.CREATE_PREDICTION;

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
  private final Map<String, javax.servlet.Filter> filters = new HashMap<>();

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

    this.pathwayAnalysisCreatorPermissionTemplate.put("pathway-analysis:%s:put", "Update Pathway Analysis with ID = %s");
    this.pathwayAnalysisCreatorPermissionTemplate.put("pathway-analysis:%s:sql:*:get", "Get analysis sql for Pathway Analysis with ID = %s");
    this.pathwayAnalysisCreatorPermissionTemplate.put("pathway-analysis:%s:generation:*:post", "Generate Pathway Analysis with ID = %s");
    this.pathwayAnalysisCreatorPermissionTemplate.put("pathway-analysis:%s:delete", "Delete Pathway Analysis with ID = %s");

    this.featureAnalysisPermissionTemplates.put("feature-analysis:%s:put", "Update Feature Analysis with ID = %s");
    this.featureAnalysisPermissionTemplates.put("feature-analysis:%s:delete", "Delete Feature Analysis with ID = %s");

    this.incidenceRatePermissionTemplates.put("ir:%s:get", "Read Incidence Rate with ID=%s");
    this.incidenceRatePermissionTemplates.put("ir:%s:execution:*:get", "Execute Incidence Rate job with ID=%s");
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
    
    fillFilters();
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
      .addProtectedRestPath("/conceptset", "jwtAuthc, authz, " + CREATE_CONCEPT_SET.getTemplateName())
      .addProtectedRestPath("/conceptset/*/items", "jwtAuthc, authz")
      .addProtectedRestPath("/conceptset/*", "jwtAuthc, authz, deletePermissionsOnDeleteConceptSet")

      // incidence rates
      .addProtectedRestPath("/ir", "jwtAuthc, authz, " + CREATE_IR.getTemplateName())
      .addProtectedRestPath("/ir/*/copy", CREATE_COPY_IR.getTemplateName())
      .addProtectedRestPath("/ir/*", "jwtAuthc, authz")
      .addProtectedRestPath("/ir/*/execute/*")

      // comparative cohort analysis (estimation)
      .addProtectedRestPath("/comparativecohortanalysis", CREATE_PLE.getTemplateName())
      .addProtectedRestPath("/comparativecohortanalysis/*", "deletePermissionsOnDeletePle")

      // new estimation
      .addProtectedRestPath("/estimation", CREATE_ESTIMATION.getTemplateName())
      .addProtectedRestPath("/estimation/*/copy", CREATE_ESTIMATION.getTemplateName())
      .addProtectedRestPath("/estimation/*", "deletePermissionsOnDeleteEstimation")
      .addProtectedRestPath("/estimation/*/export")
      .addProtectedRestPath("/estimation/*/download")

      // population level prediction
      .addProtectedRestPath("/plp", CREATE_PLP.getTemplateName())
      .addProtectedRestPath("/plp/*/copy", CREATE_COPY_PLP.getTemplateName())
      .addProtectedRestPath("/plp/*", "deletePermissionsOnDeletePlp")

      // new prediction
      .addProtectedRestPath("/prediction", CREATE_PREDICTION.getTemplateName())
      .addProtectedRestPath("/prediction/*/copy", CREATE_PREDICTION.getTemplateName())
      .addProtectedRestPath("/prediction/*", "deletePermissionsOnDeletePrediction")
      .addProtectedRestPath("/prediction/*/export")
      .addProtectedRestPath("/prediction/*/download")

      // cohort definition
      .addProtectedRestPath("/cohortdefinition", CREATE_COHORT_DEFINITION.getTemplateName())
      .addProtectedRestPath("/cohortdefinition/*/copy", CREATE_COHORT_DEFINITION.getTemplateName())
      .addProtectedRestPath("/cohortdefinition/*", "deletePermissionsOnDeleteCohortDefinition")
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
      .addProtectedRestPath("/source", CREATE_SOURCE.getTemplateName())
      .addProtectedRestPath("/source/*", "deletePermissionsOnDeleteSource")
      .addProtectedRestPath("/source/details/*")

      // cohort analysis
      .addProtectedRestPath("/cohortanalysis")
      .addProtectedRestPath("/cohortanalysis/*")

      // cohort results
      .addProtectedRestPath("/cohortresults/*")

      // cohort characterization
      .addProtectedRestPath("/cohort-characterization", CREATE_COHORT_CHARACTERIZATION.getTemplateName())
      .addProtectedRestPath("/cohort-characterization/import", CREATE_COHORT_CHARACTERIZATION.getTemplateName())
      .addProtectedRestPath("/cohort-characterization/*", "deletePermissionsOnDeleteCohortCharacterization")
      .addProtectedRestPath("/cohort-characterization/*/generation/*")
      .addProtectedRestPath("/cohort-characterization/*/generation")
      .addProtectedRestPath("/cohort-characterization/generation/*")
      .addProtectedRestPath("/cohort-characterization/generation/*/design")
      .addProtectedRestPath("/cohort-characterization/generation/*/result")
      .addProtectedRestPath("/cohort-characterization/*/export")

      // Pathways Analyses
      .addProtectedRestPath("/pathway-analysis", CREATE_PATHWAY_ANALYSIS.getTemplateName())
      .addProtectedRestPath("/pathway-analysis/import", CREATE_PATHWAY_ANALYSIS.getTemplateName())
      .addProtectedRestPath("/pathway-analysis/*", "deletePermissionsOnDeletePathwayAnalysis")
      .addProtectedRestPath("/pathway-analysis/*/sql/*")
      .addProtectedRestPath("/pathway-analysis/*/generation/*")
      .addProtectedRestPath("/pathway-analysis/*/generation")
      .addProtectedRestPath("/pathway-analysis/generation/*")
      .addProtectedRestPath("/pathway-analysis/generation/*/design")
      .addProtectedRestPath("/pathway-analysis/generation/*/result")
      .addProtectedRestPath("/pathway-analysis/*/export")

      // feature analyses
      .addProtectedRestPath("/feature-analysis", CREATE_FEATURE_ANALYSIS.getTemplateName())
      .addProtectedRestPath("/feature-analysis/*", "deletePermissionsOnDeleteFeatureAnalysis")

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
  public Map<String, javax.servlet.Filter> getFilters(){
    return new HashMap<>(filters);
  }
  
  private void fillFilters() {
    filters.put("noSessionCreation", new NoSessionCreationFilter());
    filters.put("forceSessionCreation", new ForceSessionCreationFilter());
    filters.put("authz", new UrlBasedAuthorizingFilter());
    filters.put(CREATE_COHORT_DEFINITION.getTemplateName(), this.getCreatePermissionsOnCreateCohortDefinitionFilter());
    filters.put(CREATE_COHORT_CHARACTERIZATION.getTemplateName(), new ProcessResponseContentFilterImpl(cohortCharacterizationCreatorPermissionTemplates));
    filters.put("deletePermissionsOnDeleteCohortCharacterization", this.getDeletePermissionsOnDeleteFilter(cohortCharacterizationCreatorPermissionTemplates));
    filters.put(CREATE_PATHWAY_ANALYSIS.getTemplateName(), new ProcessResponseContentFilterImpl(pathwayAnalysisCreatorPermissionTemplate));
    filters.put("deletePermissionsOnDeletePathwayAnalysis", this.getDeletePermissionsOnDeleteFilter(pathwayAnalysisCreatorPermissionTemplate));
    filters.put(CREATE_FEATURE_ANALYSIS.getTemplateName(), new ProcessResponseContentFilterImpl(featureAnalysisPermissionTemplates));
    filters.put("deletePermissionsOnDeleteFeatureAnalysis", this.getDeletePermissionsOnDeleteFilter(featureAnalysisPermissionTemplates));
    filters.put(CREATE_CONCEPT_SET.getTemplateName(), new ProcessResponseContentFilterImpl(conceptsetCreatorPermissionTemplates));
    filters.put("deletePermissionsOnDeleteCohortDefinition", this.getDeletePermissionsOnDeleteCohortDefinitionFilter());
    filters.put("deletePermissionsOnDeleteConceptSet", this.getDeletePermissionsOnDeleteConceptSetFilter());
    filters.put("deletePermissionsOnDeletePle", this.getDeletePermissionsOnDeleteFilter(plePermissionTemplates));
    filters.put("deletePermissionsOnDeletePlp", this.getDeletePermissionsOnDeleteFilter(plpPermissionTemplate));
    filters.put(CREATE_IR.getTemplateName(), new ProcessResponseContentFilterImpl(incidenceRatePermissionTemplates));
    filters.put(CREATE_COPY_IR.getTemplateName(), this.getCreatePermissionsOnCopyIncidenceRateFilter());
    filters.put(CREATE_PLE.getTemplateName(), new ProcessResponseContentFilterImpl(estimationPermissionTemplates));
    filters.put(CREATE_PLP.getTemplateName(), new ProcessResponseContentFilterImpl(plpPermissionTemplate));
    filters.put(CREATE_COPY_PLP.getTemplateName(), this.getCreatePermissionsOnCopyFilter(plpPermissionTemplate, ".*plp/.*/copy"));
    filters.put(CREATE_SOURCE.getTemplateName(), new ProcessResponseContentFilterImpl(dataSourcePermissionTemplates));
    filters.put("deletePermissionsOnDeleteSource", this.getDeletePermissionsOnDeleteFilter(dataSourcePermissionTemplates));
    filters.put("cors", new CorsFilter());
    filters.put("skipFurtherFiltersIfNotPost", this.getSkipFurtherFiltersIfNotPostFilter());
    filters.put("skipFurtherFiltersIfNotPut", this.getSkipFurtherFiltersIfNotPutFilter());
    filters.put("skipFurtherFiltersIfNotPutOrPost", this.getskipFurtherFiltersIfNotPutOrPostFilter());
    filters.put("skipFurtherFiltersIfNotPutOrDelete", this.getskipFurtherFiltersIfNotPutOrDeleteFilter());
    filters.put("ssl", this.getSslFilter());
    filters.put(CREATE_PREDICTION.getTemplateName(), new ProcessResponseContentFilterImpl(predictionPermissionTemplates));
    filters.put("deletePermissionsOnDeletePrediction", this.getDeletePermissionsOnDeleteFilter(predictionPermissionTemplates));
    filters.put(CREATE_ESTIMATION.getTemplateName(), new ProcessResponseContentFilterImpl(estimationPermissionTemplates));
    filters.put("deletePermissionsOnDeleteEstimation", this.getDeletePermissionsOnDeleteFilter(estimationPermissionTemplates));
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

  @PostConstruct
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
  
  private class ProcessResponseContentFilterImpl extends ProcessResponseContentFilter{
    private Map<String, String> template;
    
    private ProcessResponseContentFilterImpl(Map<String, String> template){
      this.template = new HashMap<>(template);
    }

    @Override
    protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
      return HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
    }

    @Override
    public void doProcessResponseContent(String id) throws Exception {
      RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
      authorizer.addPermissionsFromTemplate(currentUserPersonalRole, template, id);
    }
  }

  private Filter getCreatePermissionsOnCreateCohortDefinitionFilter() {
    return new ProcessResponseContentFilterImpl(cohortdefinitionCreatorPermissionTemplates) {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String path = httpRequest.getPathInfo().replaceAll("/+$", "");

        if (StringUtils.endsWithIgnoreCase(path, "copy")) {
          return HttpMethod.GET.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
        }
        else {
          return  HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
        }
      }
    };
  }

  private Filter getCreatePermissionsOnCopyIncidenceRateFilter() {
    return new ProcessResponseContentFilterImpl(incidenceRatePermissionTemplates) {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return WebUtils.toHttp(request).getRequestURI().matches(".*ir/.*/copy");
      }
    };
  }

  private Filter getCreatePermissionsOnCopyFilter(Map<String, String> template, String pathRegex) {
    return new ProcessResponseContentFilterImpl(template) {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return WebUtils.toHttp(request).getRequestURI().matches(pathRegex);
      }
    };
  }

  private Filter getDeletePermissionsOnDeleteFilter(Map<String, String> template) {
    return new AdviceFilter() {
      @Override
      protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        if (!HttpMethod.DELETE.equalsIgnoreCase(httpRequest.getMethod())) {
          return;
        }

        String id = httpRequest.getPathInfo()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .split("/")
                [1];
        authorizer.removePermissionsFromTemplate(template, id);
      }
    };
  }

  private Filter getDeletePermissionsOnDeleteCohortDefinitionFilter() {
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
                .split("/")
                [1];
        authorizer.removePermissionsFromTemplate(cohortdefinitionCreatorPermissionTemplates, id);
      }
    };
  }

  private Filter getDeletePermissionsOnDeleteIRFilter() {
    return new AdviceFilter() {
      @Override
      protected void postHandle(ServletRequest request, ServletResponse response) throws Exception {

        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        if (!HttpMethod.DELETE.equalsIgnoreCase(httpRequest.getMethod())){
          return;
        }
        String id = httpRequest.getPathInfo()
                .replaceAll("^/+", "")
                .replaceAll("/+$", "")
                .split("/")
                [1];
        authorizer.removePermissionsFromTemplate(incidenceRatePermissionTemplates, id);
      }
    };
  }

  private Filter getDeletePermissionsOnDeleteConceptSetFilter() {
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
                .split("/")
                [1];
        authorizer.removePermissionsFromTemplate(conceptsetCreatorPermissionTemplates, id);
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

  class FilterChainBuilder {

    private Map<String, String> filterChain = new LinkedHashMap<>();
    private String restFilters;
    private String authcFilter;
    private String authzFilter;
    private String filtersBeforeOAuth;
    private String filtersAfterOAuth;

    public FilterChainBuilder setRestFilters(String restFilters) {
      this.restFilters = restFilters;
      return this;
    }

    public FilterChainBuilder setOAuthFilters(String filtersBeforeOAuth, String filtersAfterOAuth) {
      this.filtersBeforeOAuth = filtersBeforeOAuth;
      this.filtersAfterOAuth = filtersAfterOAuth;
      return this;
    }

    public FilterChainBuilder setAuthcFilter(String authcFilter) {
      this.authcFilter = authcFilter;
      return this;
    }

    public FilterChainBuilder setAuthzFilter(String authzFilter) {
      this.authzFilter = authzFilter;
      return this;
    }

    public FilterChainBuilder addRestPath(String path, String filters) {
      return this.addPath(path, this.restFilters + ", " + filters);
    }

    public FilterChainBuilder addRestPath(String path) {
      return this.addPath(path, this.restFilters);
    }

    public FilterChainBuilder addOAuthPath(String path, String oauthFilter) {
      return this.addPath(path, filtersBeforeOAuth + ", " + oauthFilter + ", " + filtersAfterOAuth);
    }

    public FilterChainBuilder addProtectedRestPath(String path) {
      return this.addRestPath(path, this.authcFilter + ", " + this.authzFilter);
    }

    public FilterChainBuilder addProtectedRestPath(String path, String filters) {
      return this.addRestPath(path, authcFilter + ", " + authzFilter + ", " + filters);
    }

    public FilterChainBuilder addPath(String path, String filters) {
      path = path.replaceAll("/+$", "");
      this.filterChain.put(path, filters);

      // If path ends with non wildcard character, need to add two paths -
      // one without slash at the end and one with slash at the end, because
      // both URLs like www.domain.com/myapp/mypath and www.domain.com/myapp/mypath/
      // (note the slash at the end) are falling into the same method, but
      // for filter chain these are different paths
      if (!path.endsWith("*")) {
        this.filterChain.put(path + "/", filters);
      }

      return this;
    }

    public Map<String, String> build() {
      return filterChain;
    }
  }
}
