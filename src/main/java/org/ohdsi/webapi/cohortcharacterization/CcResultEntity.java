package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.feanalysis.FeAnalysisEntity;

public class CcResultEntity {
    private Long id;
    private CcGeneration cohortGeneration;
    private FeAnalysisEntity feAnalysis;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public CcGeneration getCohortGeneration() {
        return cohortGeneration;
    }

    public void setCohortGeneration(final CcGeneration cohortGeneration) {
        this.cohortGeneration = cohortGeneration;
    }

    public FeAnalysisEntity getFeAnalysis() {
        return feAnalysis;
    }

    public void setFeAnalysis(final FeAnalysisEntity feAnalysis) {
        this.feAnalysis = feAnalysis;
    }
}
