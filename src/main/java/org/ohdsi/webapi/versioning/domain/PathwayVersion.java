package org.ohdsi.webapi.versioning.domain;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "pathway_version")
public class PathwayVersion extends Version {
}
