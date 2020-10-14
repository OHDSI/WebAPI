package org.ohdsi.webapi.security.model;

import org.apache.shiro.authz.SimpleAuthorizationInfo;

public class UserSimpleAuthorizationInfo extends SimpleAuthorizationInfo {
    private Long userId;

    private String login;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
