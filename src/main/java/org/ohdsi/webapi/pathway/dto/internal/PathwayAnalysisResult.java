package org.ohdsi.webapi.pathway.dto.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PathwayAnalysisResult {

    private List<PathwayCode> codes = new ArrayList<>();
    private Map<String, Integer> pathwaysCounts;

    public List<PathwayCode> getCodes() {

        return codes;
    }

    public void setCodes(List<PathwayCode> codes) {

        this.codes = codes;
    }

    public Map<String, Integer> getPathwaysCounts() {

        return pathwaysCounts;
    }

    public void setPathwaysCounts(Map<String, Integer> pathwaysCounts) {

        this.pathwaysCounts = pathwaysCounts;
    }
}
