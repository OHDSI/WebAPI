package org.ohdsi.webapi.pathway.dto;

public class PathwayCodeDTO  {

    private Long code;
    private String name;
    private boolean isCombo = false;

    public Long getCode() {

        return code;
    }

    public void setCode(Long code) {

        this.code = code;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public boolean getIsCombo() {

        return isCombo;
    }

    public void setIsCombo(boolean combo) {

        isCombo = combo;
    }
}
