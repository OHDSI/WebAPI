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
 * JPA Entity for sec_source table
 */
@Entity(name="SourceAccess")
@Table(name = "sec_source")
@IdClass(SourceAccessEntity.SourceAccessId.class)
public class SourceAccessEntity {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "source_id")
    private Long sourceId;

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

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
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
    public static class SourceAccessId implements Serializable {
        private Long roleId;
        private Long sourceId;
        private AccessType accessType;

        public SourceAccessId() {
        }

        public SourceAccessId(Long roleId, Long sourceId, AccessType accessType) {
            this.roleId = roleId;
            this.sourceId = sourceId;
            this.accessType = accessType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SourceAccessId)) return false;
            SourceAccessId that = (SourceAccessId) o;
            return Objects.equals(roleId, that.roleId) &&
                   Objects.equals(sourceId, that.sourceId) &&
                   accessType == that.accessType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, sourceId, accessType);
        }
    }
}

