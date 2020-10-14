package org.ohdsi.webapi.cdmresults.eviction;

import com.fasterxml.jackson.databind.JsonNode;
import org.ohdsi.webapi.cdmresults.keys.DrilldownKey;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class DrilldownEvictionAdvisor extends CDMResultsSupport<DrilldownKey, JsonNode> {

    protected DrilldownEvictionAdvisor(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public JsonNode getActualValue(DrilldownKey key, JsonNode current) {

        return getCdmResultsService().getRawDrilldown(key.getDomain(), key.getConceptId(), key.getSource());
    }
}
