package org.ohdsi.webapi.cohortcharacterization.report;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Set;

public class AnalysisResultItem {
    private Set<String> domainIds;
    private Set<Cohort> cohorts;
    private Set<ExportItem> exportItems;

    public AnalysisResultItem(Set<String> domainIds, Set<Cohort> cohorts,
                              Set<ExportItem> exportItems) {
        this.domainIds = domainIds;
        this.cohorts = cohorts;
        this.exportItems = exportItems;
    }

    public Set<String> getDomainIds() {
        return domainIds;
    }

    public Set<Cohort> getCohorts() {
        return cohorts;
    }

    public Set<ExportItem> getExportItems() {
        return exportItems;
    }
}
