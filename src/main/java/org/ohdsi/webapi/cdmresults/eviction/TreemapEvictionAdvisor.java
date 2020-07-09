package org.ohdsi.webapi.cdmresults.eviction;

import com.fasterxml.jackson.databind.node.ArrayNode;
import org.ohdsi.webapi.cdmresults.keys.TreemapKey;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class TreemapEvictionAdvisor extends CDMResultsSupport<TreemapKey, ArrayNode> {

    protected TreemapEvictionAdvisor(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public ArrayNode getActualValue(TreemapKey key, ArrayNode current) {

        return getCdmResultsService().getRawTreeMap(key.getDomain(), key.getSourceKey());
    }
}
