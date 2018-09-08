package org.ohdsi.webapi.pathway.domain;

import javax.persistence.Entity;

@Entity(name="pathway_target_cohorts")
public class PathwayTargetCohort extends PathwayCohort {

    @Override
    public int hashCode() {

        return java.util.Objects.hashCode(this.getId());
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof PathwayTargetCohort)) {
            return false;
        }

        final PathwayTargetCohort compare = (PathwayTargetCohort) obj;
        return java.util.Objects.equals(getId(), compare.getId());
    }
}
