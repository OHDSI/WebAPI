package org.ohdsi.webapi.generationcache;

import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.internal.SessionFactoryImpl;
import org.ohdsi.webapi.source.Source;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;
import java.util.concurrent.Future;

@Configuration
public class SourceDeleteListenerConfigurer {

    @PersistenceUnit
    private EntityManagerFactory emf;

    private final GenerationCacheService generationCacheService;
    private final GenerationCacheRepository generationCacheRepository;

    public SourceDeleteListenerConfigurer(GenerationCacheService generationCacheService, GenerationCacheRepository generationCacheRepository) {

        this.generationCacheService = generationCacheService;
        this.generationCacheRepository = generationCacheRepository;
    }

    @PostConstruct
    protected void init() {

        SessionFactoryImpl sessionFactory = emf.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.PRE_DELETE).appendListener((PreDeleteEventListener) event -> {
            if (event.getEntity() instanceof Source) {
                Source source = (Source) event.getEntity();
                Future future = new SimpleAsyncTaskExecutor().submit(() -> clearSourceRelatedCaches(source));
                try {
                    future.get();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
            return false;
        });
    }

    private void clearSourceRelatedCaches(Source source) {

        List<GenerationCache> caches = generationCacheRepository.findAllBySourceSourceId(source.getSourceId());
        caches.forEach(gc -> generationCacheService.removeCache(gc.getType(), source, gc.getDesignHash()));
    }
}
