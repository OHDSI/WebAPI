package org.ohdsi.webapi.versioning.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "cohort_characterization_versions")
public class CharacterizationVersion extends Version {
    @EmbeddedId
    private VersionPK pk;

    @Override
    public VersionPK getPk() {
        return pk;
    }

    @Override
    public void setPk(VersionPK pk) {
        this.pk = pk;
    }
}
