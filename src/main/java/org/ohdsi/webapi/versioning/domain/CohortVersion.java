package org.ohdsi.webapi.versioning.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity(name = "CohortVersion")
@Table(name = "cohort_version")
public class CohortVersion extends Version {
    @Column(name = "description")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
