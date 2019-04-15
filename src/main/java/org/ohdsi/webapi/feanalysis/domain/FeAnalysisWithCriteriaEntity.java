package org.ohdsi.webapi.feanalysis.domain;

import org.ohdsi.analysis.cohortcharacterization.design.FeatureAnalysisWithCriteria;
import org.ohdsi.circe.cohortdefinition.ConceptSet;

import javax.persistence.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
public abstract class FeAnalysisWithCriteriaEntity<T extends FeAnalysisCriteriaEntity> extends FeAnalysisEntity<List<T>> implements FeatureAnalysisWithCriteria<T, Integer> {
    
    @OneToMany(targetEntity = FeAnalysisCriteriaEntity.class, fetch = FetchType.EAGER, mappedBy = "featureAnalysis",
            cascade = {CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH, CascadeType.DETACH})
    private List<T> design;

    @OneToOne(fetch = FetchType.EAGER, mappedBy = "featureAnalysis", cascade = CascadeType.ALL)
    private FeAnalysisConcepsetEntity conceptSetEntity;

    public FeAnalysisWithCriteriaEntity() {
        super();
    }
    
    public FeAnalysisWithCriteriaEntity(final FeAnalysisWithCriteriaEntity analysis) {
        super(analysis);
    }

    @Override
    public List<T> getDesign() {
        return design;
    }

    @Override
    public void setDesign(List<T> design) {
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
