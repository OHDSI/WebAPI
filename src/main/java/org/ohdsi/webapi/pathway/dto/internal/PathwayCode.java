package org.ohdsi.webapi.pathway.dto.internal;

import java.util.Objects;

public class PathwayCode {

    private long code;
    private String name;
    private boolean isCombo = false;

    public PathwayCode(long code, String name, boolean isCombo) {

        this.code = code;
        this.name = name;
        this.isCombo = isCombo;
    }

    public long getCode() {

        return code;
    }

    public void setCode(long code) {

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

    @Override
    public int hashCode() {
        return Objects.hash(getCode(), getName(), isCombo());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PathwayCode)) return false;
        PathwayCode that = (PathwayCode) o;
        return Objects.equals(getCode(), that.getCode()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(isCombo(), that.isCombo());
    }
}
