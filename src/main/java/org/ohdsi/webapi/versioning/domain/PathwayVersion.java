package org.ohdsi.webapi.versioning.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "pathway_version")
public class PathwayVersion extends Version {
}
