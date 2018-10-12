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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.Authenticator;
import org.apache.shiro.authc.pam.ModularRealmAuthenticator;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.web.filter.authz.SslFilter;
import org.apache.shiro.web.filter.session.NoSessionCreationFilter;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.OidcConfCreator;
import org.ohdsi.webapi.shiro.*;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;
import org.ohdsi.webapi.shiro.filters.CorsFilter;
import org.ohdsi.webapi.shiro.filters.ForceSessionCreationFilter;
import org.ohdsi.webapi.shiro.filters.ProcessResponseContentFilter;
import org.ohdsi.webapi.shiro.filters.SkipFurtherFilteringFilter;
import org.ohdsi.webapi.shiro.filters.UrlBasedAuthorizingFilter;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import waffle.shiro.negotiate.NegotiateAuthenticationStrategy;

/**
 *
 * @author gennadiy.anisimov
 */
public abstract class AtlasSecurity extends Security {
  public static final String TOKEN_ATTRIBUTE = "TOKEN";
  public static final String AUTH_FILTER_ATTRIBUTE = "AuthenticatingFilter";
  public static final String PERMISSIONS_ATTRIBUTE = "PERMISSIONS";
  private final Log log = LogFactory.getLog(getClass());

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
      .addProtectedRestPath("/conceptset", "jwtAuthc, authz, createPermissionsOnCreateConceptSet")
      .addProtectedRestPath("/conceptset/*/items", "jwtAuthc, authz")
      .addProtectedRestPath("/conceptset/*", "jwtAuthc, authz, deletePermissionsOnDeleteConceptSet")

      // incidence rates
      .addProtectedRestPath("/ir", "jwtAuthc, authz, createPermissionsOnCreateIR")
      .addProtectedRestPath("/ir/*/copy", "createPermissionsOnCopyIR")
      .addProtectedRestPath("/ir/*", "jwtAuthc, authz")
      .addProtectedRestPath("/ir/*/execute/*")

      // comparative cohort analysis (estimation)
      .addProtectedRestPath("/comparativecohortanalysis", "createPermissionsOnCreatePle")
      .addProtectedRestPath("/comparativecohortanalysis/*", "deletePermissionsOnDeletePle")

      // new estimation
      .addProtectedRestPath("/estimation", "createPermissionsOnCreateEstimation")
      .addProtectedRestPath("/estimation/*/copy", "createPermissionsOnCreateEstimation")
      .addProtectedRestPath("/estimation/*", "deletePermissionsOnDeleteEstimation")
      .addProtectedRestPath("/estimation/*/export")
      .addProtectedRestPath("/estimation/*/download")

      // population level prediction
      .addProtectedRestPath("/plp", "createPermissionsOnCreatePlp")
      .addProtectedRestPath("/plp/*/copy", "createPermissionsOnCopyPlp")
      .addProtectedRestPath("/plp/*", "deletePermissionsOnDeletePlp")

      // new prediction
      .addProtectedRestPath("/prediction", "createPermissionsOnCreatePrediction")
      .addProtectedRestPath("/prediction/*/copy", "createPermissionsOnCreatePrediction")
      .addProtectedRestPath("/prediction/*", "deletePermissionsOnDeletePrediction")
      .addProtectedRestPath("/prediction/*/export")
      .addProtectedRestPath("/prediction/*/download")

      // cohort definition
      .addProtectedRestPath("/cohortdefinition", "createPermissionsOnCreateCohortDefinition")
      .addProtectedRestPath("/cohortdefinition/*/copy", "createPermissionsOnCreateCohortDefinition")
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
      .addProtectedRestPath("/source", "createPermissionsOnCreateSource")
      .addProtectedRestPath("/source/*", "deletePermissionsOnDeleteSource")
      .addProtectedRestPath("/source/details/*")

      // cohort analysis
      .addProtectedRestPath("/cohortanalysis")
      .addProtectedRestPath("/cohortanalysis/*")

      // cohort results
      .addProtectedRestPath("/cohortresults/*")

      // cohort characterization
      .addProtectedRestPath("/cohort-characterization", "createPermissionsOnCreateCohortCharacterization")
      .addProtectedRestPath("/cohort-characterization/import", "createPermissionsOnCreateCohortCharacterization")
      .addProtectedRestPath("/cohort-characterization/*", "deletePermissionsOnDeleteCohortCharacterization")
      .addProtectedRestPath("/cohort-characterization/*/generation/*")
      .addProtectedRestPath("/cohort-characterization/*/generation")
      .addProtectedRestPath("/cohort-characterization/generation/*")
      .addProtectedRestPath("/cohort-characterization/generation/*/design")
      .addProtectedRestPath("/cohort-characterization/generation/*/result")
      .addProtectedRestPath("/cohort-characterization/*/export")

      // Pathways Analyses
      .addProtectedRestPath("/pathway-analysis", "createPermissionsOnCreatePathwayAnalysis")
      .addProtectedRestPath("/pathway-analysis/import", "createPermissionsOnCreatePathwayAnalysis")
      .addProtectedRestPath("/pathway-analysis/*", "deletePermissionsOnDeletePathwayAnalysis")
      .addProtectedRestPath("/pathway-analysis/*/sql/*")
      .addProtectedRestPath("/pathway-analysis/*/generation/*")
      .addProtectedRestPath("/pathway-analysis/*/generation")
      .addProtectedRestPath("/pathway-analysis/generation/*")
      .addProtectedRestPath("/pathway-analysis/generation/*/design")
      .addProtectedRestPath("/pathway-analysis/generation/*/result")
      .addProtectedRestPath("/pathway-analysis/*/export")

      // feature analyses
      .addProtectedRestPath("/feature-analysis", "createPermissionsOnCreateFeatureAnalysis")
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
      .addProtectedRestPath("/*/person/*");
  }

  @Override
  public Map<String, Filter> getFilters() {
    Map<String, javax.servlet.Filter> filters = new HashMap<>();

    filters.put("noSessionCreation", new NoSessionCreationFilter());
    filters.put("forceSessionCreation", new ForceSessionCreationFilter());
    filters.put("authz", new UrlBasedAuthorizingFilter());
    filters.put("createPermissionsOnCreateCohortDefinition", this.getCreatePermissionsOnCreateCohortDefinitionFilter());
    filters.put("createPermissionsOnCreateCohortCharacterization", this.getCreatePermissionsOnCreateCohortCharacterizationFilter());
    filters.put("deletePermissionsOnDeleteCohortCharacterization", this.getDeletePermissionsOnDeleteFilter(cohortCharacterizationCreatorPermissionTemplates));
    filters.put("createPermissionsOnCreatePathwayAnalysis", this.getCreatePermissionsOnCreatePathwayAnalysisFilter());
    filters.put("deletePermissionsOnDeletePathwayAnalysis", this.getDeletePermissionsOnDeleteFilter(pathwayAnalysisCreatorPermissionTemplate));
    filters.put("createPermissionsOnCreateFeatureAnalysis", this.getCreatePermissionsOnCreateFilter(featureAnalysisPermissionTemplates, "id"));
    filters.put("deletePermissionsOnDeleteFeatureAnalysis", this.getDeletePermissionsOnDeleteFilter(featureAnalysisPermissionTemplates));
    filters.put("createPermissionsOnCreateConceptSet", this.getCreatePermissionsOnCreateConceptSetFilter());
    filters.put("deletePermissionsOnDeleteCohortDefinition", this.getDeletePermissionsOnDeleteCohortDefinitionFilter());
    filters.put("deletePermissionsOnDeleteConceptSet", this.getDeletePermissionsOnDeleteConceptSetFilter());
    filters.put("deletePermissionsOnDeletePle", this.getDeletePermissionsOnDeleteFilter(plePermissionTemplates));
    filters.put("deletePermissionsOnDeletePlp", this.getDeletePermissionsOnDeleteFilter(plpPermissionTemplate));
    filters.put("createPermissionsOnCreateIR", this.getCreatePermissionsOnCreateIncidenceRateFilter());
    filters.put("createPermissionsOnCopyIR", this.getCreatePermissionsOnCopyIncidenceRateFilter());
    filters.put("createPermissionsOnCreatePle", this.getCreatePermissionsOnCreateFilter(estimationPermissionTemplates, "analysisId"));
    filters.put("createPermissionsOnCreatePlp", this.getCreatePermissionsOnCreateFilter(plpPermissionTemplate, "analysisId"));
    filters.put("createPermissionsOnCopyPlp", this.getCreatePermissionsOnCopyFilter(plpPermissionTemplate, ".*plp/.*/copy", "analysisId"));
    filters.put("createPermissionsOnCreateSource", this.getCreatePermissionsOnCreateFilter(dataSourcePermissionTemplates, "sourceKey"));
    filters.put("deletePermissionsOnDeleteSource", this.getDeletePermissionsOnDeleteFilter(dataSourcePermissionTemplates));
    filters.put("cors", new CorsFilter());
    filters.put("skipFurtherFiltersIfNotPost", this.getSkipFurtherFiltersIfNotPostFilter());
    filters.put("skipFurtherFiltersIfNotPut", this.getSkipFurtherFiltersIfNotPutFilter());
    filters.put("skipFurtherFiltersIfNotPutOrPost", this.getskipFurtherFiltersIfNotPutOrPostFilter());
    filters.put("skipFurtherFiltersIfNotPutOrDelete", this.getskipFurtherFiltersIfNotPutOrDeleteFilter());
    filters.put("ssl", this.getSslFilter());

    filters.put("createPermissionsOnCreatePrediction", this.getCreatePermissionsOnCreateFilter(predictionPermissionTemplates, "id"));
    filters.put("deletePermissionsOnDeletePrediction", this.getDeletePermissionsOnDeleteFilter(predictionPermissionTemplates));
    filters.put("createPermissionsOnCreateEstimation", this.getCreatePermissionsOnCreateFilter(estimationPermissionTemplates, "id"));
    filters.put("deletePermissionsOnDeleteEstimation", this.getDeletePermissionsOnDeleteFilter(estimationPermissionTemplates));

    return filters;
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

    RoleEntity role = this.authorizer.addRole(roleName);
    this.authorizer.addPermissionsFromTemplate(role, this.sourcePermissionTemplates, sourceKey);
  }

  @Override
  public void removeSourceRole(String sourceKey) throws Exception {
    final String roleName = getSourceRoleName(sourceKey);
    if (this.authorizer.roleExists(roleName)) {
      RoleEntity role = this.authorizer.getRoleByName(roleName);
      this.authorizer.removePermissionsFromTemplate(this.sourcePermissionTemplates, sourceKey);
      Set<UserEntity> roleUsers = this.authorizer.getRoleUsers(role.getId());
      for(UserEntity user : roleUsers) {
        this.authorizer.removeUserFromRole(roleName, user.getLogin());
      }
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
      log.error(e);
    }
  }

  private Filter getCreatePermissionsOnCreateCohortDefinitionFilter() {
    return new ProcessResponseContentFilter() {
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

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, cohortdefinitionCreatorPermissionTemplates, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCreateCohortCharacterizationFilter() {
    return  new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, cohortCharacterizationCreatorPermissionTemplates, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCreatePathwayAnalysisFilter() {
    return  new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, pathwayAnalysisCreatorPermissionTemplate, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCreateIncidenceRateFilter() {
    return  new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, incidenceRatePermissionTemplates, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCreateFilter(Map<String, String> template, String idField) {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, idField);
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, template, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCopyIncidenceRateFilter() {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return WebUtils.toHttp(request).getRequestURI().matches(".*ir/.*/copy");
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, incidenceRatePermissionTemplates, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCopyFilter(Map<String, String> template, String pathRegex, String idField) {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {

        return WebUtils.toHttp(request).getRequestURI().matches(pathRegex);
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, idField);
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, template, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCreateConceptSetFilter() {
    return  new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        return  HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String id = this.parseJsonField(content, "id");
        RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
        authorizer.addPermissionsFromTemplate(currentUserPersonalRole, conceptsetCreatorPermissionTemplates, id);
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
