package org.ohdsi.webapi.security.authz.access;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Objects;

/**
 * JPA Entity for sec_cohort_definition table
 */
@Entity(name="CohortDefinitionAccess")
@Table(name = "sec_cohort_definition")
@IdClass(CohortDefinitionAccessEntity.CohortDefinitionAccessId.class)
public class CohortDefinitionAccessEntity {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "cohort_definition_id")
    private Long cohortDefinitionId;

    @Id
    @Column(name = "access_type")
    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long userId) {
        this.roleId = userId;
    }

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(Long cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    /**
     * Composite key class for CohortDefinitionAccessEntity
     */
    public static class CohortDefinitionAccessId implements Serializable {
        private Long roleId;
        private Long cohortDefinitionId;
        private AccessType accessType;

        public CohortDefinitionAccessId() {
        }

        public CohortDefinitionAccessId(Long roleId, Long cohortDefinitionId, AccessType accessType) {
            this.roleId = roleId;
            this.cohortDefinitionId = cohortDefinitionId;
            this.accessType = accessType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CohortDefinitionAccessId)) return false;
            CohortDefinitionAccessId that = (CohortDefinitionAccessId) o;
            return Objects.equals(roleId, that.roleId) &&
                   Objects.equals(cohortDefinitionId, that.cohortDefinitionId) &&
                   accessType == that.accessType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, cohortDefinitionId, accessType);
        }
    }
}
