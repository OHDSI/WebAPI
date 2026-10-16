package org.ohdsi.webapi.versioning.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "concept_set_version")
public class ConceptSetVersion extends Version {
}
