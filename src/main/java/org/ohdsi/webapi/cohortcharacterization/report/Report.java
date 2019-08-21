package org.ohdsi.webapi.cohortcharacterization.report;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.tuple.Pair;
import org.ohdsi.analysis.cohortcharacterization.design.CcResultType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Report {
    public String analysisName;
    public Integer analysisId;
    public CcResultType resultType;
    @JsonIgnore
    public List<String[]> header;
    public Set<String> domainIds;
    public Set<Pair<Integer, String>> cohorts;
    public Set<ExportItem> items;
    public boolean isComparative;
    public boolean isSummary;
    public String faType;
    public String domainId;

    public Report(String analysisName, Integer analysisId, AnalysisResultItem resultItem) {
        this.analysisName = analysisName;
        this.analysisId = analysisId;
        this.items = resultItem.exportItems;
        cohorts = resultItem.cohorts;
        domainIds = resultItem.domainIds;
    }

    public Report(String analysisName, List<AnalysisResultItem> simpleResultSummary) {
        this.analysisName = analysisName;
        domainIds = new HashSet<>();
        cohorts = new HashSet<>();
        items = new HashSet<>();
        simpleResultSummary.stream()
                .forEach(item -> {
                    domainIds.addAll(item.domainIds);
                    cohorts.addAll(item.cohorts);
                    items.addAll(item.exportItems);
                });
    }

    @JsonIgnore
    public List<String[]> getResultArray() {
        return items
                .stream()
                .sorted()
                .map(x -> x.getValueArray())
                .collect(Collectors.toList());
    }
}
