package org.ohdsi.webapi.feanalysis.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.ObjectUtils;
import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysis;
import org.ohdsi.webapi.feanalysis.FeAnalysisDeserializer;

@JsonDeserialize(using = FeAnalysisDeserializer.class)
public class FeAnalysisDTO extends FeAnalysisShortDTO implements FeatureAnalysis, Comparable<FeAnalysisDTO> {

    private String value;
    @JsonProperty("design")
    private Object design;

    public String getValue() {

        return value;
    }

    public void setValue(final String value) {

        this.value = value;
    }

    @Override
    public Object getDesign() {

        return design;
    }

    public void setDesign(final Object design) {

        this.design = design;
    }

    @Override
    public String getDescr() {

        return getDescription();
    }

    @Override
    public int compareTo(FeAnalysisDTO o) {
        return ObjectUtils.compare(this.getId(), o.getId());
    }
}
