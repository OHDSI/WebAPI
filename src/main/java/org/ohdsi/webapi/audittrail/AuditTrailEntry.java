package org.ohdsi.webapi.audittrail;

import org.ohdsi.webapi.shiro.Entities.UserEntity;

public class AuditTrailEntry {
    private UserEntity currentUser;
    private String remoteHost;
    private String actionLocation;
    private String requestMethod;
    private String requestUri;
    private String queryString;
    private Object returnedObject;

    public UserEntity getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(UserEntity currentUser) {
        this.currentUser = currentUser;
    }

    public String getActionLocation() {
        return actionLocation;
    }

    public void setActionLocation(String actionLocation) {
        this.actionLocation = actionLocation;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestUri() {
        return requestUri;
    }

    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public Object getReturnedObject() {
        return returnedObject;
    }

    public void setReturnedObject(Object returnedObject) {
        this.returnedObject = returnedObject;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
}
