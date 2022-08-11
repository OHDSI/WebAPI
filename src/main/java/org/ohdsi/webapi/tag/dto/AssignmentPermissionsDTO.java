package org.ohdsi.webapi.tag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssignmentPermissionsDTO {
    private boolean isAnyAssetMultiAssignPermitted;

    public boolean isAnyAssetMultiAssignPermitted() {
        return isAnyAssetMultiAssignPermitted;
    }

    public void setAnyAssetMultiAssignPermitted(final boolean anyAssetMultiAssignPermitted) {
        isAnyAssetMultiAssignPermitted = anyAssetMultiAssignPermitted;
    }
}
