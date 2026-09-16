package org.ohdsi.webapi.versioning.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "cohort_characterization_version")
public class CharacterizationVersion extends Version {
}
