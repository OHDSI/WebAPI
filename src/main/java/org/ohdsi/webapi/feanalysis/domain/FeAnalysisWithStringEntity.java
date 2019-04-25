package org.ohdsi.webapi.feanalysis.domain;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Lob;
import org.hibernate.annotations.Type;
import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysis;

@Entity
@DiscriminatorValue("not null")
public class FeAnalysisWithStringEntity extends FeAnalysisEntity<String> {
    
    @Lob
    @Type(type = "org.hibernate.type.TextType")
    private String design;

    @Override
    public String getDesign() {

        return design;
    }

    public void setDesign(final String design) {

        this.design = design;
    }
}
