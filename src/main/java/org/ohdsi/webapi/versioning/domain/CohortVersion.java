package org.ohdsi.webapi.versioning.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity(name = "CohortVersion")
@Table(name = "cohort_versions")
public class CohortVersion extends Version {
    @EmbeddedId
    private VersionPK pk;

    @Column(name = "description")
    private String description;

    @Override
    public VersionPK getPk() {
        return pk;
    }

    @Override
    public void setPk(VersionPK pk) {
        this.pk = pk;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
