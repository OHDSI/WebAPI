package org.ohdsi.webapi.shiro.management;

import org.ohdsi.webapi.shiro.PermissionManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.ohdsi.webapi.shiro.management.FilterTemplates.AUTHZ;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COHORT_CHARACTERIZATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COHORT_DEFINITION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_CONCEPT_SET;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COPY_IR;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_COPY_PLP;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_ESTIMATION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_FEATURE_ANALYSIS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_IR;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_PATHWAY_ANALYSIS;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_PLE;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_PLP;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_PREDICTION;
import static org.ohdsi.webapi.shiro.management.FilterTemplates.CREATE_SOURCE;
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
import static org.ohdsi.webapi.shiro.management.FilterTemplates.JWT_AUTHC;

@Component
public class AtlasSecurityHelper {

    public FilterChainBuilder setupProtectedPaths(FilterChainBuilder filterChainBuilder) {

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
                .addProtectedRestPath("/cohortdefinition/*/copy", CREATE_COHORT_DEFINITION)
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

    public ResponseContentFilterBuilder createResponseContentBuilder(FilterTemplates template, Map<String, String> permissionTemplates,
                                                                     PermissionManager authorizer, ApplicationEventPublisher eventPublisher){
        return ResponseContentFilterBuilder.builder()
                .withPermissionManager(authorizer)
                .withEventPublisher(eventPublisher)
                .withTemplate(permissionTemplates)
                .withEntityName(template.getEntityName());
    }
}