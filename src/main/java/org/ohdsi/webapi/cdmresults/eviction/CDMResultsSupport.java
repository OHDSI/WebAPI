package org.ohdsi.webapi.cdmresults.eviction;

import org.ohdsi.webapi.service.CDMResultsService;
import org.springframework.context.ApplicationContext;

import java.util.Objects;

public abstract class CDMResultsSupport<K, V> implements CDMEvictionAdvisor<K, V> {

    private CDMResultsService cdmResultsService;

    private final ApplicationContext applicationContext;

    protected CDMResultsSupport(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public boolean adviseAgainstEviction(K key, V value) {
        return !Objects.equals(value, getActualValue(key, value));
    }

    protected final CDMResultsService getCdmResultsService() {
        initialize();
        return cdmResultsService;
    }

    private synchronized void initialize() {
        if (Objects.isNull(cdmResultsService)) {
            cdmResultsService = applicationContext.getBean(CDMResultsService.class);
        }
    }
}
