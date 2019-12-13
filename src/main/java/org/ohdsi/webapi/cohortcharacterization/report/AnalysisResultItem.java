package org.ohdsi.webapi.cohortcharacterization.report;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

public class AnalysisResultItem {
    private Set<String> domainIds;
    private Set<Pair<Integer, String>> cohorts;
    private Set<ExportItem> exportItems;

    public AnalysisResultItem(Set<String> domainIds, Set<Pair<Integer, String>> cohorts,
                              Set<ExportItem> exportItems) {
        this.domainIds = domainIds;
        this.cohorts = cohorts;
        this.exportItems = exportItems;
    }

    public Set<String> getDomainIds() {
        return domainIds;
    }

    public Set<Pair<Integer, String>> getCohorts() {
        return cohorts;
    }

    public Set<ExportItem> getExportItems() {
        return exportItems;
    }
}
