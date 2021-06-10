package org.ohdsi.webapi.common.generation;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class AnalysisGenerationInfo extends AnalysisGenerationBaseInfo{

    @Column(name = "design")
    protected String design;

    public String getDesign() {

        return design;
    }
}