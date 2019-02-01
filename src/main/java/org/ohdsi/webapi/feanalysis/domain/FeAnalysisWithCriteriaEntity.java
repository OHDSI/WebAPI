package org.ohdsi.webapi.feanalysis.domain;

import org.ohdsi.circe.cohortdefinition.ConceptSet;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.persistence.*;

@Entity
@DiscriminatorValue(value = "CRITERIA_SET")
public class FeAnalysisWithCriteriaEntity extends FeAnalysisEntity<List<FeAnalysisCriteriaEntity>> {
    
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "featureAnalysis", cascade = CascadeType.ALL)
    private List<FeAnalysisCriteriaEntity> design;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "featureAnalysis", cascade = CascadeType.ALL)
    private FeAnalysisConcepsetEntity conceptSetEntity;

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

    public FeAnalysisConcepsetEntity getConceptSetEntity() {
        return conceptSetEntity;
    }

    public void setConceptSetEntity(FeAnalysisConcepsetEntity conceptSetEntity) {
        this.conceptSetEntity = conceptSetEntity;
    }

    public List<ConceptSet> getConceptSets() {

        return Objects.nonNull(this.conceptSetEntity) ? this.conceptSetEntity.getConceptSets() : Collections.emptyList();
    }
}
