package org.ohdsi.webapi.cohortcharacterization.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

public class ExportExecutionResultRequest {
    @JsonProperty("cohortIds")
    private List<Integer> cohortIds;

    @JsonProperty("analisysIds")
    private List<Integer> analisysIds;

    @JsonProperty("domainIds")
    private List<String> domainIds;

    public List<Integer> getCohortIds() {
        if(cohortIds == null) {
            return Collections.emptyList();
        }
        return cohortIds;
    }

    public void setCohortIds(List<Integer> cohortIds) {
        this.cohortIds = cohortIds;
    }

    public List<Integer> getAnalisysIds() {
        if(analisysIds == null) {
            return Collections.emptyList();
        }
        return analisysIds;
    }

    public void setAnalisysIds(List<Integer> analisysIds) {
        this.analisysIds = analisysIds;
    }

    public List<String> getDomainIds() {
        if(domainIds == null) {
            return Collections.emptyList();
        }
        return domainIds;
    }

    public void setDomainIds(List<String> domainIds) {
        this.domainIds = domainIds;
    }

    public boolean isFilterUsed() {
        return !(getAnalisysIds().isEmpty() && getDomainIds().isEmpty() && getCohortIds().isEmpty());
    }
}
