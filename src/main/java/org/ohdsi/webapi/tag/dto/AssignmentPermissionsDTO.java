package org.ohdsi.webapi.tag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignmentPermissionsDTO {
    private boolean isAnyAssetMultiAssignPermitted;
    private boolean canAssignProtectedTags;
    private boolean canUnassignProtectedTags;

    public boolean isCanAssignProtectedTags() {
        return canAssignProtectedTags;
    }

    public void setCanAssignProtectedTags(final boolean canAssignProtectedTags) {
        this.canAssignProtectedTags = canAssignProtectedTags;
    }

    public boolean isCanUnassignProtectedTags() {
        return canUnassignProtectedTags;
    }

    public void setCanUnassignProtectedTags(final boolean canUnassignProtectedTags) {
        this.canUnassignProtectedTags = canUnassignProtectedTags;
    }

    public boolean isAnyAssetMultiAssignPermitted() {
        return isAnyAssetMultiAssignPermitted;
    }

    public void setAnyAssetMultiAssignPermitted(final boolean anyAssetMultiAssignPermitted) {
        isAnyAssetMultiAssignPermitted = anyAssetMultiAssignPermitted;
    }
}
