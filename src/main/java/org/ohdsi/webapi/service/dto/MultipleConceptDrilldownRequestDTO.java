package org.ohdsi.webapi.service.dto;

import java.util.List;

public class MultipleConceptDrilldownRequestDTO {
    private List<Integer> conceptIds;

    public List<Integer> getConceptIds() {
        return conceptIds;
    }

    public void setConceptIds(List<Integer> conceptIds) {
        this.conceptIds = conceptIds;
    }
}
