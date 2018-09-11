package org.ohdsi.webapi.pathway.dto.internal;

import java.util.Map;
import java.util.Set;

public class PathwayAnalysisResult {

    private Set<Integer> codes;
    private Map<String, Integer> pathwaysCounts;

    public Set<Integer> getCodes() {

        return codes;
    }

    public void setCodes(Set<Integer> codes) {

        this.codes = codes;
    }

    public Map<String, Integer> getPathwaysCounts() {

        return pathwaysCounts;
    }

    public void setPathwaysCounts(Map<String, Integer> pathwaysCounts) {

        this.pathwaysCounts = pathwaysCounts;
    }
}
