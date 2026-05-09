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
 * JPA Entity for sec_reusable table
 */
@Entity(name = "ReusableAccess")
@Table(name = "sec_reusable")
@IdClass(ReusableAccessEntity.ReusableAccessId.class)
public class ReusableAccessEntity {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "reusable_id")
    private Long reusableId;

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

    public Long getReusableId() {
        return reusableId;
    }

    public void setReusableId(Long reusableId) {
        this.reusableId = reusableId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    /**
     * Composite key class for ReusableAccessEntity
     */
    public static class ReusableAccessId implements Serializable {
        private Long roleId;
        private Long reusableId;
        private AccessType accessType;

        public ReusableAccessId() {
        }

        public ReusableAccessId(Long roleId, Long reusableId, AccessType accessType) {
            this.roleId = roleId;
            this.reusableId = reusableId;
            this.accessType = accessType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ReusableAccessId)) return false;
            ReusableAccessId that = (ReusableAccessId) o;
            return Objects.equals(roleId, that.roleId) &&
                    Objects.equals(reusableId, that.reusableId) &&
                    Objects.equals(accessType, that.accessType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, reusableId, accessType);
        }
    }
}
