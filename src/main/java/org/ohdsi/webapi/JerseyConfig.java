package org.ohdsi.webapi;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.server.spi.internal.ValueParamProvider;
import org.ohdsi.webapi.info.InfoService;
import org.ohdsi.webapi.security.PermissionController;
import org.ohdsi.webapi.security.SSOController;
import org.ohdsi.webapi.service.ActivityService;
import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.service.CohortAnalysisService;
import org.ohdsi.webapi.service.CohortDefinitionService;
import org.ohdsi.webapi.service.CohortResultsService;
import org.ohdsi.webapi.service.CohortService;
import org.ohdsi.webapi.service.ConceptSetService;
import org.ohdsi.webapi.service.DDLService;
import org.ohdsi.webapi.service.EvidenceService;
import org.ohdsi.webapi.service.FeasibilityService;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.service.SqlRenderService;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.source.SourceController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.inject.Singleton;
import jakarta.ws.rs.ApplicationPath;
import org.ohdsi.webapi.cache.CacheService;

/**
 * Jersey configuration for JAX-RS resources
 */
@Configuration
@ApplicationPath("/WebAPI")
public class JerseyConfig extends ResourceConfig {
    
    public JerseyConfig(@Value("${jersey.resources.root.package}") String rootPackage) {
       // Register packages first
       packages(rootPackage);
       
       // Register individual services
       register(ActivityService.class);
       register(CacheService.class);
       register(CDMResultsService.class);
       register(CohortAnalysisService.class);
       register(CohortDefinitionService.class);
       register(CohortResultsService.class);
       register(CohortService.class);
       register(ConceptSetService.class);
       register(DDLService.class);
       register(EvidenceService.class);
       register(FeasibilityService.class);
       register(InfoService.class);
       register(JobService.class);
       register(MultiPartFeature.class);
       register(PermissionController.class);
       register(SourceController.class);
       register(SqlRenderService.class);
       register(SSOController.class);
       register(UserService.class);
       register(VocabularyService.class);
       
       // Register binder
       register(new AbstractBinder() {
           @Override
           protected void configure() {
               bind(PageableValueFactoryProvider.class)
                       .to(ValueParamProvider.class)
                       .in(Singleton.class);
           }
       });
       
       // Register encoding filter - must be last
       register(EncodingFilter.class);
       register(GZipEncoder.class);
    }
}
