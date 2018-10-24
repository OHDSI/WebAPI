package com.jnj.honeur.webapi.shiro.management;

import com.jnj.honeur.webapi.cas.filter.CASSessionFilter;
import com.jnj.honeur.webapi.shiro.filters.HoneurInvalidateAccessTokenFilter;
import com.jnj.honeur.webapi.shiro.filters.HoneurJwtAuthFilter;
import com.jnj.honeur.webapi.shiro.filters.HoneurUpdateAccessTokenFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.web.servlet.AdviceFilter;
import org.apache.shiro.web.util.WebUtils;
import org.ohdsi.webapi.shiro.Entities.RoleEntity;
import org.ohdsi.webapi.shiro.filters.*;
import org.ohdsi.webapi.shiro.management.AtlasRegularSecurity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.HttpMethod;
import java.util.*;

@Component
@ConditionalOnProperty(name = "security.provider", havingValue = "AtlasCustomSecurity")
@DependsOn("flyway")
public class AtlasCustomSecurity extends AtlasRegularSecurity {

  private final Log logger = LogFactory.getLog(getClass());

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Value("${security.token.expiration}")
  private int tokenExpirationIntervalInSeconds;

  @Value("${webapi.central}")
  private boolean central;

  @Value("${security.cas.tgc.domain}")
  private String casTgcDomain;

  private final Map<String, String> cohortdefinitionCreatorPermissionTemplates = new LinkedHashMap<>();
  private final Map<String, String> cohortdefinitionExporterPermissionTemplatesToDelete = new LinkedHashMap<>();
  private final Map<String, String> cohortdefinitionImporterPermissionTemplates = new LinkedHashMap<>();

  public AtlasCustomSecurity() {
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:get", "View Cohort Definition with ID = %s");
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:%s:export:get", "Export Cohort Definition with ID = %s");
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:hss:list:%s:get", "List Cohort Definition Generation Results in Amazon for Cohort Definition with ID = %s");
    this.cohortdefinitionCreatorPermissionTemplates.put("cohortdefinition:hss:%s:select:*:post", "Import Cohort Definition Generation Results for Cohort Definition with ID = %s");

    this.cohortdefinitionExporterPermissionTemplatesToDelete
            .put("cohortdefinition:%s:put", "Update Cohort Definition with ID = %s");
    this.cohortdefinitionExporterPermissionTemplatesToDelete
            .put("cohortdefinition:%s:delete", "Delete Cohort Definition with ID = %s");
    this.cohortdefinitionExporterPermissionTemplatesToDelete
            .put("cohortdefinition:%s:export:get", "Export Cohort Definition with ID = %s");

//    this.cohortdefinitionImporterPermissionTemplates.put("cohortdefinition:%s:generate:*:get", "Generate Cohort Definition generation results for defintion with ID = %s");
    this.cohortdefinitionImporterPermissionTemplates.put("cohortdefinition:%s:delete", "Delete Cohort Definition with ID = %s");
    this.cohortdefinitionImporterPermissionTemplates.put("cohortdefinition:%s:get", "View Cohort Definition with ID = %s");
//    this.cohortdefinitionImporterPermissionTemplates.put("cohortdefinition:%s:export:*:get", "Export Cohort Definition generation results for defintion with ID = %s");
    this.cohortdefinitionImporterPermissionTemplates.put("cohortdefinition:%s:info:get", "Get Cohort Definition Info for cohort definition with ID = %s");

  }

  @PostConstruct
  private void addHoneurLocalRoleIfRemote(){
    if(!central){
      this.defaultRoles.add("HONEUR-local");
    }
  }

  @Override
  protected FilterChainBuilder getFilterChainBuilder() {

    FilterChainBuilder filterChainBuilder = super.getFilterChainBuilder()
            .addProtectedRestPath("/cohortdefinition/hss/select", "createPermissionsOnImportCohortDefinition")
            .addProtectedRestPath("/cohortdefinition/*", "deletePermissionsOnDeleteCohortDefinition");

    return filterChainBuilder;
  }

    @Override
  public Map<String, Filter> getFilters() {
    Map<String, Filter> filters = super.getFilters();

    filters.put("jwtAuthc", new HoneurJwtAuthFilter());
    filters.put("updateToken", new HoneurUpdateAccessTokenFilter(this.authorizer, this.defaultRoles, this.tokenExpirationIntervalInSeconds));
    filters.put("invalidateToken", new HoneurInvalidateAccessTokenFilter());
    filters.put("createPermissionsOnImportCohortDefinition", this.getCreatePermissionsOnImportCohortDefinitionFilter());
    filters.put("deletePermissionsOnExportCohortDefinition", this.getDeletePermissionsOnExportCohortDefinitionFilter());
    filters.put("createPermissionsOnCreateCohortDefinition", this.getCreatePermissionsOnCreateCohortDefinitionFilter());
    filters.put("deletePermissionsOnDeleteCohortDefinition", this.getDeletePermissionsOnDeleteCohortDefinitionFilter());
    filters.put("casSessionFilter", new CASSessionFilter(true, casTgcDomain));

    return filters;
  }

  private Filter getCreatePermissionsOnImportCohortDefinitionFilter() {
      return new ProcessResponseContentFilter() {
          @Override
          protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
              HttpServletRequest httpRequest = WebUtils.toHttp(request);
              String path = httpRequest.getPathInfo().replaceAll("/+$", "");

              if (StringUtils.endsWithIgnoreCase(path, "hss/select")) {
                  return HttpMethod.POST.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
              }
              return false;
          }

          @Override
          protected void doProcessResponseContent(String content) throws Exception {
              String id = this.parseJsonField(content, "id");
              RoleEntity currentUserPersonalRole = authorizer.getCurrentUserPersonalRole();
              authorizer.addPermissionsFromTemplate(currentUserPersonalRole, cohortdefinitionImporterPermissionTemplates,
                      id);
          }
      };
  }

  private Filter getDeletePermissionsOnExportCohortDefinitionFilter() {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String path = httpRequest.getPathInfo().replaceAll("/+$", "");

        if (StringUtils.endsWithIgnoreCase(path, "export")) {
          return HttpMethod.GET.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
        } else {
          return  false;
        }
      }

      @Override
      protected void doProcessResponseContent(String content) throws Exception {
        String previousVersion = this.parseJsonField(content, "previousVersion");
        String id = this.parseJsonField(previousVersion, "id");
        authorizer.removePermissionsFromTemplate(cohortdefinitionExporterPermissionTemplatesToDelete, id);
      }
    };
  }

  private Filter getCreatePermissionsOnCreateCohortDefinitionFilter() {
    return new ProcessResponseContentFilter() {
      @Override
      protected boolean shouldProcess(ServletRequest request, ServletResponse response) {
        HttpServletRequest httpRequest = WebUtils.toHttp(request);
        String path = httpRequest.getPathInfo().replaceAll("/+$", "");

        if (StringUtils.endsWithIgnoreCase(path, "copy")) {
          return HttpMethod.GET.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
        } else if (StringUtils.endsWithIgnoreCase(path, "export")) {
          return HttpMethod.GET.equalsIgnoreCase(WebUtils.toHttp(request).getMethod());
        } else {
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
        authorizer.removePermissionsFromTemplate(cohortdefinitionImporterPermissionTemplates, id);
      }
    };
  }
}
