package org.ohdsi.webapi.cohortcharacterization;

public class CcParameterDTO {
    
    private Long id;
    private String name;
    private String value;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }
}
