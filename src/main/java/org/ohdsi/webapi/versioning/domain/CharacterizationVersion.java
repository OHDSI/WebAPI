package org.ohdsi.webapi.versioning.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "cohort_characterization_versions")
public class CharacterizationVersion extends Version {
}
