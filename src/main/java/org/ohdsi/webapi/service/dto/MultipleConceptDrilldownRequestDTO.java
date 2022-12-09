package org.ohdsi.webapi.service.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultipleConceptDrilldownRequestDTO {
    private Map<String, List<Integer>> domainConceptMap = new HashMap<>();

    public Map<String, List<Integer>> getDomainConceptMap() {
        return domainConceptMap;
    }

    public void setDomainConceptMap(Map<String, List<Integer>> domainConceptMap) {
        this.domainConceptMap = domainConceptMap;
    }
}
