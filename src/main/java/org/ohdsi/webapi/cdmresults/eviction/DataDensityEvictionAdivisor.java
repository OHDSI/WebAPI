package org.ohdsi.webapi.cdmresults.eviction;

import org.ohdsi.webapi.cdmresults.keys.RefreshableSourceKey;
import org.ohdsi.webapi.report.CDMDataDensity;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DataDensityEvictionAdivisor extends CDMResultsSupport<RefreshableSourceKey, CDMDataDensity> {

    public DataDensityEvictionAdivisor(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public CDMDataDensity getActualValue(RefreshableSourceKey key, CDMDataDensity current) {

        return getCdmResultsService().getRawDataDesity(key.getSource(), key.getRefresh());
    }
}
