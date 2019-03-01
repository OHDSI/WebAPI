package org.ohdsi.webapi.cohortcharacterization.dto;

import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterizationParam;

public class CcParameterDTO implements CohortCharacterizationParam, Comparable<CcParameterDTO> {
    
    private Long id;
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    @Override
    public int compareTo(CcParameterDTO o) {
        return ObjectUtils.compare(this.id, o.id);
    }
}
