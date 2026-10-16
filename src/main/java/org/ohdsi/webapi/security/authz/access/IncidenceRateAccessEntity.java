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
 * JPA Entity for sec_ir_analysis table
 */
@Entity(name = "IncidenceRateAccess")
@Table(name = "sec_ir_analysis")
@IdClass(IncidenceRateAccessEntity.IncidenceRateAccessId.class)
public class IncidenceRateAccessEntity {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "ir_id")
    private Long irId;

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

    public Long getIrId() {
        return irId;
    }

    public void setIrId(Long irId) {
        this.irId = irId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public static class IncidenceRateAccessId implements Serializable {
        private Long roleId;
        private Long irId;
        private AccessType accessType;

        public IncidenceRateAccessId() {
        }

        public IncidenceRateAccessId(Long roleId, Long irId, AccessType accessType) {
            this.roleId = roleId;
            this.irId = irId;
            this.accessType = accessType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof IncidenceRateAccessId)) return false;
            IncidenceRateAccessId that = (IncidenceRateAccessId) o;
            return Objects.equals(roleId, that.roleId) &&
                    Objects.equals(irId, that.irId) &&
                    accessType == that.accessType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, irId, accessType);
        }
    }
}
