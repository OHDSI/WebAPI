package org.ohdsi.webapi.security.authz.access;

public class AccessRequestDTO {

    private AccessType accessType;

    public AccessType getAccessType() {

        return accessType;
    }

    public void setAccessType(AccessType accessType) {

        this.accessType = accessType;
    }
}
