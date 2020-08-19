package org.ohdsi.webapi.cdmresults.eviction;

import org.ohdsi.webapi.report.CDMDashboard;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DashboardEvictionAdvisor extends CDMResultsSupport<String, CDMDashboard> {

    @Autowired
    public DashboardEvictionAdvisor(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public CDMDashboard getActualValue(String key, CDMDashboard current) {

        return getCdmResultsService().getRawDashboard(key);
    }
}
