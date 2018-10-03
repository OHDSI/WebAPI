package org.ohdsi.webapi.pathway.dto.internal;

public class PathwayCode {

    private Integer code;
    private String name;
    private boolean isCombo = false;

    public PathwayCode(Integer code, String name, boolean isCombo) {

        this.code = code;
        this.name = name;
        this.isCombo = isCombo;
    }

    public Integer getCode() {

        return code;
    }

    public void setCode(Integer code) {

        this.code = code;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public boolean isCombo() {

        return isCombo;
    }

    public void setCombo(boolean combo) {

        isCombo = combo;
    }
}
