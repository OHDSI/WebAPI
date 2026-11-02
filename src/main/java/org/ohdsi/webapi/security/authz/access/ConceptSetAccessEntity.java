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
 * JPA Entity for sec_concept_set table
 */
@Entity(name = "ConceptSetAccess")
@Table(name = "sec_concept_set")
@IdClass(ConceptSetAccessEntity.ConceptSetAccessId.class)
public class ConceptSetAccessEntity {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "concept_set_id")
    private Long conceptSetId;

    @Id
    @Column(name = "access_type")
    @Enumerated(EnumType.STRING)
    private AccessType accessType;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getConceptSetId() {
        return conceptSetId;
    }

    public void setConceptSetId(Long conceptSetId) {
        this.conceptSetId = conceptSetId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    /**
     * Composite key class for ConceptSetAccessEntity
     */
    public static class ConceptSetAccessId implements Serializable {
        private Long roleId;
        private Long conceptSetId;
        private AccessType accessType;

        public ConceptSetAccessId() {
        }

        public ConceptSetAccessId(Long roleId, Long conceptSetId, AccessType accessType) {
            this.roleId = roleId;
            this.conceptSetId = conceptSetId;
            this.accessType = accessType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ConceptSetAccessId)) return false;
            ConceptSetAccessId that = (ConceptSetAccessId) o;
            return Objects.equals(roleId, that.roleId) &&
                   Objects.equals(conceptSetId, that.conceptSetId) &&
                   accessType == that.accessType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, conceptSetId, accessType);
        }
    }
}
