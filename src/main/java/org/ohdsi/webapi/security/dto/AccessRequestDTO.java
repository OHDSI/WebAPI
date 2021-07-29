package org.ohdsi.webapi.security.dto;

import org.ohdsi.webapi.security.AccessType;

public class AccessRequestDTO {

    private AccessType accessType;

    public AccessType getAccessType() {

        return accessType;
    }

    public void setAccessType(AccessType accessType) {

        this.accessType = accessType;
    }
}
