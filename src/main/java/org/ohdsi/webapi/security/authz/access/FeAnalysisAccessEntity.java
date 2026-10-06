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
 * JPA Entity for sec_fe_analysis table
 */
@Entity(name = "FeAnalysisAccess")
@Table(name = "sec_fe_analysis")
@IdClass(FeAnalysisAccessEntity.FeAnalysisAccessId.class)
public class FeAnalysisAccessEntity {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "fe_analysis_id")
    private Long feAnalysisId;

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

    public Long getFeAnalysisId() {
        return feAnalysisId;
    }

    public void setFeAnalysisId(Long feAnalysisId) {
        this.feAnalysisId = feAnalysisId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    /**
     * Composite key class for FeAnalysisAccessEntity
     */
    public static class FeAnalysisAccessId implements Serializable {
        private Long roleId;
        private Long feAnalysisId;
        private AccessType accessType;

        public FeAnalysisAccessId() {
        }

        public FeAnalysisAccessId(Long roleId, Long feAnalysisId, AccessType accessType) {
            this.roleId = roleId;
            this.feAnalysisId = feAnalysisId;
            this.accessType = accessType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FeAnalysisAccessId)) return false;
            FeAnalysisAccessId that = (FeAnalysisAccessId) o;
            return Objects.equals(roleId, that.roleId) &&
                    Objects.equals(feAnalysisId, that.feAnalysisId) &&
                    accessType == that.accessType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, feAnalysisId, accessType);
        }
    }
}
