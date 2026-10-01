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
 * JPA Entity for sec_cohort_characterization table
 */
@Entity(name="CohortCharacterizationAccess")
@Table(name = "sec_cohort_characterization")
@IdClass(CohortCharacterizationAccessEntity.CohortCharacterizationAccessId.class)
public class CohortCharacterizationAccessEntity {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "cohort_characterization_id")
    private Long cohortCharacterizationId;

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

    public Long getCohortCharacterizationId() {
        return cohortCharacterizationId;
    }

    public void setCohortCharacterizationId(Long cohortCharacterizationId) {
        this.cohortCharacterizationId = cohortCharacterizationId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    /**
     * Composite key class for CohortCharacterizationAccessEntity
     */
    public static class CohortCharacterizationAccessId implements Serializable {
        private Long roleId;
        private Long cohortCharacterizationId;
        private AccessType accessType;

        public CohortCharacterizationAccessId() {
        }

        public CohortCharacterizationAccessId(Long roleId, Long cohortCharacterizationId, AccessType accessType) {
            this.roleId = roleId;
            this.cohortCharacterizationId = cohortCharacterizationId;
            this.accessType = accessType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof CohortCharacterizationAccessId)) return false;
            CohortCharacterizationAccessId that = (CohortCharacterizationAccessId) o;
            return Objects.equals(roleId, that.roleId) &&
                   Objects.equals(cohortCharacterizationId, that.cohortCharacterizationId) &&
                   accessType == that.accessType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, cohortCharacterizationId, accessType);
        }
    }
}
