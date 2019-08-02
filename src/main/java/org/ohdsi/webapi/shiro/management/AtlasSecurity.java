package org.ohdsi.webapi.shiro.management;

import java.util.HashMap;
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
import org.ohdsi.webapi.cohortcharacterization.CcImportEvent;
import org.ohdsi.webapi.security.model.EntityPermissionSchemaResolver;
import org.ohdsi.webapi.security.model.EntityType;
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
import org.springframework.context.event.EventListener;
import waffle.shiro.negotiate.NegotiateAuthenticationStrategy;

import static org.ohdsi.webapi.shiro.management.FilterTemplates.AUTHZ;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.COPY_ESTIMATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.COPY_PREDICTION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CORS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COHORT_CHARACTERIZATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COHORT_DEFINITION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_CONCEPT_SET;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COPY_COHORT_DEFINITION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COPY_IR;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_IR;
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

  private final Map<String, String> cohortdefinitionCreatorPermissionTemplates;
  private final Map<String, String> cohortCharacterizationCreatorPermissionTemplates;
  private final Map<String, String> pathwayAnalysisCreatorPermissionTemplate;
  private final Map<String, String> conceptsetCreatorPermissionTemplates;
  private final Map<String, String> sourcePermissionTemplates;
  private final Map<String, String> incidenceRatePermissionTemplates;
  private final Map<String, String> estimationPermissionTemplates;
  private final Map<String, String> predictionPermissionTemplates;
  private final Map<String, String> dataSourcePermissionTemplates;
  private final Map<String, String> featureAnalysisPermissionTemplates;
  private final Map<FilterTemplates, Filter> filters = new HashMap<>();

  public AtlasSecurity(EntityPermissionSchemaResolver permissionSchemaResolver) {
    this.defaultRoles.add("public");

    cohortdefinitionCreatorPermissionTemplates = permissionSchemaResolver.getForType(EntityType.COHORT_DEFINITION).getWritePermissions();
    cohortCharacterizationCreatorPermissionTemplates = permissionSchemaResolver.getForType(EntityType.COHORT_CHARACTERIZATION).getWritePermissions();
    pathwayAnalysisCreatorPermissionTemplate = permissionSchemaResolver.getForType(EntityType.PATHWAY_ANALYSIS).getWritePermissions();
    conceptsetCreatorPermissionTemplates = permissionSchemaResolver.getForType(EntityType.CONCEPT_SET).getWritePermissions();
    sourcePermissionTemplates = permissionSchemaResolver.getForType(EntityType.CONCEPT_SET).getReadPermissions();
    incidenceRatePermissionTemplates = permissionSchemaResolver.getForType(EntityType.INCIDENCE_RATE).getWritePermissions();
    estimationPermissionTemplates = permissionSchemaResolver.getForType(EntityType.ESTIMATION).getWritePermissions();
    predictionPermissionTemplates = permissionSchemaResolver.getForType(EntityType.PREDICTION).getWritePermissions();
    dataSourcePermissionTemplates = permissionSchemaResolver.getForType(EntityType.CONCEPT_SET).getWritePermissions();
    featureAnalysisPermissionTemplates = permissionSchemaResolver.getForType(EntityType.FE_ANALYSIS).getWritePermissions();
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

            // concept set
            .addProtectedRestPath("/conceptset", CREATE_CONCEPT_SET)
            .addProtectedRestPath("/conceptset/*", DELETE_CONCEPT_SET)

            // incidence rates
            .addProtectedRestPath("/ir", CREATE_IR)
            .addProtectedRestPath("/ir/design", CREATE_IR)
            .addProtectedRestPath("/ir/*/copy", CREATE_COPY_IR)

            // new estimation
            .addProtectedRestPath("/estimation", CREATE_ESTIMATION)
            .addProtectedRestPath("/estimation/import", CREATE_ESTIMATION)
            .addProtectedRestPath("/estimation/*/copy", COPY_ESTIMATION)
            .addProtectedRestPath("/estimation/*", DELETE_ESTIMATION)

            // new prediction
            .addProtectedRestPath("/prediction", CREATE_PREDICTION)
            .addProtectedRestPath("/prediction/import", CREATE_PREDICTION)
            .addProtectedRestPath("/prediction/*/copy", COPY_PREDICTION)
            .addProtectedRestPath("/prediction/*", DELETE_PREDICTION)

            // cohort definition
            .addProtectedRestPath("/cohortdefinition", CREATE_COHORT_DEFINITION)
            .addProtectedRestPath("/cohortdefinition/*/copy", CREATE_COPY_COHORT_DEFINITION)
            .addProtectedRestPath("/cohortdefinition/*", DELETE_COHORT_DEFINITION)

            // configuration
            .addProtectedRestPath("/source", CREATE_SOURCE)
            .addProtectedRestPath("/source/*", DELETE_SOURCE)

            // cohort characterization
            .addProtectedRestPath("/cohort-characterization", CREATE_COHORT_CHARACTERIZATION)
            .addProtectedRestPath("/cohort-characterization/import", CREATE_COHORT_CHARACTERIZATION)
            .addProtectedRestPath("/cohort-characterization/*", CREATE_COHORT_CHARACTERIZATION, DELETE_COHORT_CHARACTERIZATION)

            // Pathways Analyses
            .addProtectedRestPath("/pathway-analysis", CREATE_PATHWAY_ANALYSIS)
            .addProtectedRestPath("/pathway-analysis/import", CREATE_PATHWAY_ANALYSIS)
            .addProtectedRestPath("/pathway-analysis/*", CREATE_PATHWAY_ANALYSIS, DELETE_PATHWAY_ANALYSIS)

            // feature analyses
            .addProtectedRestPath("/feature-analysis", CREATE_FEATURE_ANALYSIS)
            .addProtectedRestPath("/feature-analysis/*", DELETE_FEATURE_ANALYSIS)

            // version info
            .addRestPath("/info")
            // DDL service
            .addRestPath("/ddl/results")
            .addRestPath("/ddl/cemresults")

            //executionservice callbacks
            .addRestPath("/executionservice/callbacks/**")

            .addRestPath("/permission/access/**/*", JWT_AUTHC) // Authorization check is done inside controller

            .addProtectedRestPath("/**/*");
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
    filters.put(DELETE_SOURCE, this.getDeletePermissionsOnDeleteFilter(dataSourcePermissionTemplates));
    filters.put(DELETE_PREDICTION, this.getDeletePermissionsOnDeleteFilter(predictionPermissionTemplates));
    filters.put(DELETE_ESTIMATION, this.getDeletePermissionsOnDeleteFilter(estimationPermissionTemplates));

    addProcessEntityFilter(CREATE_COPY_COHORT_DEFINITION, cohortdefinitionCreatorPermissionTemplates);
    addProcessEntityFilter(CREATE_COPY_IR, incidenceRatePermissionTemplates);
    addProcessEntityFilter(CREATE_COHORT_DEFINITION, cohortdefinitionCreatorPermissionTemplates);
    addProcessEntityFilter(CREATE_COHORT_CHARACTERIZATION, cohortCharacterizationCreatorPermissionTemplates);
    addProcessEntityFilter(CREATE_PATHWAY_ANALYSIS, pathwayAnalysisCreatorPermissionTemplate);
    addProcessEntityFilter(CREATE_FEATURE_ANALYSIS, featureAnalysisPermissionTemplates);
    addProcessEntityFilter(CREATE_CONCEPT_SET, conceptsetCreatorPermissionTemplates);
    addProcessEntityFilter(CREATE_IR, incidenceRatePermissionTemplates);
    addProcessEntityFilter(CREATE_SOURCE, dataSourcePermissionTemplates, "sourceId");
    addProcessEntityFilter(CREATE_PREDICTION, predictionPermissionTemplates);
    addProcessEntityFilter(CREATE_ESTIMATION, estimationPermissionTemplates);
    addProcessEntityFilter(COPY_PREDICTION, predictionPermissionTemplates);
    addProcessEntityFilter(COPY_ESTIMATION, estimationPermissionTemplates);
  }

  private void addProcessEntityFilter(FilterTemplates template, Map<String, String> permissionTemplates){
    filters.put(template, new ProcessResponseContentFilterImpl(permissionTemplates, template.getEntityName(), authorizer, eventPublisher, template.getHttpMethod()));
  }
  private void addProcessEntityFilter(FilterTemplates template, Map<String, String> permissionTemplates, String identityField){
    filters.put(template, new ProcessResponseContentFilterImpl(permissionTemplates, template.getEntityName(), authorizer, eventPublisher, template.getHttpMethod(), identityField));
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

  // Since we need to create permissions only for certain analyses, we cannot go with `addProcessEntityFilter`
  @EventListener
  public void onCcImport(CcImportEvent event) throws Exception {
      for (Integer id : event.getSavedAnalysesIds()) {
          authorizer.addPermissionsFromTemplate(featureAnalysisPermissionTemplates, id.toString());
      }
  }
}
