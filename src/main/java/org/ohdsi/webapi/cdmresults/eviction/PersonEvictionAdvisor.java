package org.ohdsi.webapi.cdmresults.eviction;

import org.ohdsi.webapi.cdmresults.keys.RefreshableSourceKey;
import org.ohdsi.webapi.report.CDMPersonSummary;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class PersonEvictionAdvisor extends CDMResultsSupport<RefreshableSourceKey, CDMPersonSummary> {

    protected PersonEvictionAdvisor(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public CDMPersonSummary getActualValue(RefreshableSourceKey key, CDMPersonSummary current) {

        return getCdmResultsService().getRawPerson(key.getSource(), key.getRefresh());
    }
}
