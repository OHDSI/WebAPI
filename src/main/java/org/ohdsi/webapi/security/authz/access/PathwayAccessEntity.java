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
 * JPA Entity for sec_pathway_analysis table
 */
@Entity(name = "PathwayAccess")
@Table(name = "sec_pathway_analysis")
@IdClass(PathwayAccessEntity.PathwayAccessId.class)
public class PathwayAccessEntity {

    @Id
    @Column(name = "role_id")
    private Long roleId;

    @Id
    @Column(name = "pathway_analysis_id")
    private Long pathwayAnalysisId;

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

    public Long getPathwayAnalysisId() {
        return pathwayAnalysisId;
    }

    public void setPathwayAnalysisId(Long pathwayAnalysisId) {
        this.pathwayAnalysisId = pathwayAnalysisId;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public static class PathwayAccessId implements Serializable {
        private Long roleId;
        private Long pathwayAnalysisId;
        private AccessType accessType;

        public PathwayAccessId() {
        }

        public PathwayAccessId(Long roleId, Long pathwayAnalysisId, AccessType accessType) {
            this.roleId = roleId;
            this.pathwayAnalysisId = pathwayAnalysisId;
            this.accessType = accessType;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PathwayAccessId)) return false;
            PathwayAccessId that = (PathwayAccessId) o;
            return Objects.equals(roleId, that.roleId) &&
                    Objects.equals(pathwayAnalysisId, that.pathwayAnalysisId) &&
                    accessType == that.accessType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(roleId, pathwayAnalysisId, accessType);
        }
    }
}
