package org.ohdsi.webapi.feanalysis.domain;

import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;

@Entity
@DiscriminatorValue(value = "CRITERIA_SET")
public class FeAnalysisWithCriteriaEntity extends FeAnalysisEntity<List<FeAnalysisCriteriaEntity>> {
    
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "featureAnalysis", cascade = CascadeType.ALL)
    private List<FeAnalysisCriteriaEntity> design;

    public FeAnalysisWithCriteriaEntity() {
        super();
    }
    
    public FeAnalysisWithCriteriaEntity(final FeAnalysisWithCriteriaEntity analysis) {
        super(analysis);
    }

    @Override
    public List<FeAnalysisCriteriaEntity> getDesign() {
        return design;
    }

    @Override
    public void setDesign(List<FeAnalysisCriteriaEntity> design) {
        this.design = design;
    }
}
