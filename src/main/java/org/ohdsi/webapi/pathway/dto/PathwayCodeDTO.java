package org.ohdsi.webapi.pathway.dto;

import org.ohdsi.analysis.pathway.result.PathwayCode;

public class PathwayCodeDTO implements PathwayCode {

    private Integer code;
    private String name;
    private boolean isCombo = false;

    @Override
    public Integer getCode() {

        return code;
    }

    public void setCode(Integer code) {

        this.code = code;
    }

    @Override
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
