package org.ohdsi.webapi.common.generation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class AnalysisGenerationInfo extends AnalysisGenerationBaseInfo{

    @Column(name = "design")
    protected String design;

    public String getDesign() {

        return design;
    }
}