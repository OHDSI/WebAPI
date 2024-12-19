package org.ohdsi.webapi.feanalysis.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Lob;
import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysis;

@Entity
@DiscriminatorValue("not null")
public class FeAnalysisWithStringEntity extends FeAnalysisEntity<String> {
    public FeAnalysisWithStringEntity() {
        super();
    }

    public FeAnalysisWithStringEntity(final FeAnalysisWithStringEntity analysis) {
        super(analysis);
    }
    
    @Lob
    private String design;

    @Override
    public String getDesign() {

        return design;
    }

    public void setDesign(final String design) {

        this.design = design;
    }
}
