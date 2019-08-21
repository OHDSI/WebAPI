package org.ohdsi.webapi.cohortcharacterization.report;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

public class AnalysisResultItem {
    public Set<String> domainIds;
    public Set<Pair<Integer, String>> cohorts;
    public Set<ExportItem> exportItems;

    public AnalysisResultItem(Set<String> domainIds, Set<Pair<Integer, String>> cohorts,
                              Set<ExportItem> exportItems) {
        this.domainIds = domainIds;
        this.cohorts = cohorts;
        this.exportItems = exportItems;
    }
}
