package org.ohdsi.webapi.cohortcharacterization.report;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AnalysisMap {
    // Key is analysis id
    private Map<Integer, AnalysisItem> map = new HashMap();

    public Set<Integer> keySet() {
        return map.keySet();
    }

    public AnalysisItem getOrCreateAnalysisItem(Integer analysisId) {
        AnalysisItem analysisItem = map.get(analysisId);
        if (analysisItem == null) {
            analysisItem = new AnalysisItem();
            map.put(analysisId, analysisItem);
        }
        return analysisItem;
    }
}
