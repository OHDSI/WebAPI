package org.ohdsi.webapi.shiro.Entities;

public class UserPrincipal {
    private String name;
    private String username;
    private String password;

    public UserPrincipal() {
    }

    public UserPrincipal(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
