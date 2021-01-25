package org.ohdsi.webapi;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;
import org.ohdsi.webapi.cohortcharacterization.CcController;
import org.ohdsi.webapi.executionengine.controller.ScriptExecutionCallbackController;
import org.ohdsi.webapi.executionengine.controller.ScriptExecutionController;
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
import org.ohdsi.webapi.service.FeatureExtractionService;
import org.ohdsi.webapi.service.IRAnalysisResource;
import org.ohdsi.webapi.service.JobService;
import org.ohdsi.webapi.service.PersonService;
import org.ohdsi.webapi.service.SqlRenderService;
import org.ohdsi.webapi.service.TherapyPathResultsService;
import org.ohdsi.webapi.service.UserService;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.source.SourceController;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.inject.Singleton;
import javax.ws.rs.ext.RuntimeDelegate;

/**
 *
 */
@Component
public class JerseyConfig extends ResourceConfig implements InitializingBean {
    
    @Value("${jersey.resources.root.package}")
    private String rootPackage;
    
    public JerseyConfig() {
       RuntimeDelegate.setInstance(new org.glassfish.jersey.internal.RuntimeDelegateImpl());
       EncodingFilter.enableFor(this, GZipEncoder.class);
    }
    
    /* (non-Jsdoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        packages(this.rootPackage);
        register(ActivityService.class);
        register(CDMResultsService.class);
        register(CohortAnalysisService.class);
        register(CohortDefinitionService.class);
        register(CohortResultsService.class);
        register(CohortService.class);
        register(ConceptSetService.class);
        register(EvidenceService.class);
        register(FeasibilityService.class);
        register(InfoService.class);
        register(IRAnalysisResource.class);
        register(JobService.class);
        register(PersonService.class);
        register(SourceController.class);
        register(SqlRenderService.class);
        register(DDLService.class);
        register(TherapyPathResultsService.class);
        register(UserService.class);
        register(VocabularyService.class);
        register(ScriptExecutionController.class);
        register(ScriptExecutionCallbackController.class);
        register(MultiPartFeature.class);
        register(FeatureExtractionService.class);
        register(CcController.class);
        register(SSOController.class);
        register(PermissionController.class);
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(PageableValueFactoryProvider.class)
                        .to(ValueFactoryProvider.class)
                        .in(Singleton.class);
            }
        });
    }
}
