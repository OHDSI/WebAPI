package org.ohdsi.webapi.pathway.dto.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PathwayAnalysisResult {

    private Map<Integer, String> codes = new HashMap<>();
    private Map<String, Integer> pathwaysCounts;

    public Map<Integer, String> getCodes() {

        return codes;
    }

    public void setCodes(Map<Integer, String> codes) {

        this.codes = codes;
    }

    public Map<String, Integer> getPathwaysCounts() {

        return pathwaysCounts;
    }

    public void setPathwaysCounts(Map<String, Integer> pathwaysCounts) {

        this.pathwaysCounts = pathwaysCounts;
    }
}
