package org.ohdsi.webapi.cohortcharacterization.dto;

public class UserDTO {
    
    private Long id;
    private String name;
    private String login;

    public String getName() {

        return name;
    }

    public void setName(final String name) {

        this.name = name;
    }

    public String getLogin() {

        return login;
    }

    public void setLogin(final String login) {

        this.login = login;
    }

    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }
}
