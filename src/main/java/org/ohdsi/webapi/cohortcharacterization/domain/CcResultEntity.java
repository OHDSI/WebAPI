package org.ohdsi.webapi.cohortcharacterization.domain;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.SequenceGenerator;
import org.ohdsi.webapi.cohortcharacterization.CcResultType;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;

@MappedSuperclass
public abstract class CcResultEntity {
    
    @Id
    @SequenceGenerator(name = "cc_results_pk_sequence", sequenceName = "cc_results_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cc_results_pk_sequence")
    private Long id;
//    private CcGeneration cohortGeneration;
    @ManyToOne
    @JoinColumn(name="analysis_id", nullable=false)
    private FeAnalysisEntity feAnalysis;
    
    @Column(name = "type")
    private CcResultType resultType;

//    public CcGeneration getCohortGeneration() {
//        return cohortGeneration;
//    }

//    public void setCohortGeneration(final CcGeneration cohortGeneration) {
//        this.cohortGeneration = cohortGeneration;
//    }

    public FeAnalysisEntity getFeAnalysis() {
        return feAnalysis;
    }

    public void setFeAnalysis(final FeAnalysisEntity feAnalysis) {
        this.feAnalysis = feAnalysis;
    }

    public Long getId() {

        return id;
    }

    public void setId(final Long id) {

        this.id = id;
    }

    public CcResultType getResultType() {

        return resultType;
    }

    public void setResultType(final CcResultType resultType) {

        this.resultType = resultType;
    }
}
