package org.ohdsi.webapi.feanalysis;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonDeserialize(using = FeAnalysisDeserializer.class)
public class FeAnalysisDTO extends FeAnalysisShortDTO {
    
    private String description;
    private String value;
    private Object design;

    public String getValue() {

        return value;
    }

    public void setValue(final String value) {

        this.value = value;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(final String description) {

        this.description = description;
    }

    public Object getDesign() {

        return design;
    }

    public void setDesign(final Object design) {

        this.design = design;
    }
}
