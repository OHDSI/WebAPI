package org.ohdsi.webapi.pathway.domain;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import javax.persistence.Entity;

@Entity(name="pathway_event_cohorts")
@SQLDelete(sql = "UPDATE pathway_event_cohorts SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class PathwayEventCohort extends PathwayCohort {

    @Override
    public int hashCode() {

        return java.util.Objects.hashCode(this.getId());
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        }

        if (obj == null || !(obj instanceof PathwayEventCohort)) {
            return false;
        }

        final PathwayEventCohort compare = (PathwayEventCohort) obj;
        return java.util.Objects.equals(getId(), compare.getId());
    }
}
