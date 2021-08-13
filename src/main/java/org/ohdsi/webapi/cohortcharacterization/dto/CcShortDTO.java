package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;

public class CcShortDTO extends CommonEntityExtDTO {

    private Long id;
    private Integer hashCode;
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Integer getHashCode() {
        return hashCode;
    }

    public void setHashCode(final Integer hashCode) {
        this.hashCode = hashCode;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
