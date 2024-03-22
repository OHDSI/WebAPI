package org.ohdsi.webapi.cohortcharacterization.domain;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.feanalysis.domain.FeAnalysisEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "cc_analysis")
public class CcFeAnalysisEntity {

    @Id
    @GenericGenerator(
            name = "cc_analysis_generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "cc_analysis_seq"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    @GeneratedValue(generator = "cc_analysis_generator")
    private Long id;
    @ManyToOne(optional = false)
    @JoinColumn(name = "cohort_characterization_id")
    private CohortCharacterizationEntity cohortCharacterization;
    @ManyToOne(optional = false)
    @JoinColumn(name = "fe_analysis_id")
    private FeAnalysisEntity featureAnalysis;
    @Column(name = "include_annual")
    private Boolean includeAnnual;
    @Column(name = "include_temporal")
    private Boolean includeTemporal;

    public CohortCharacterizationEntity getCohortCharacterization() {
        return cohortCharacterization;
    }

    public void setCohortCharacterization(CohortCharacterizationEntity cohortCharacterization) {
        this.cohortCharacterization = cohortCharacterization;
    }

    public FeAnalysisEntity getFeatureAnalysis() {
        return featureAnalysis;
    }

    public void setFeatureAnalysis(FeAnalysisEntity featureAnalysis) {
        this.featureAnalysis = featureAnalysis;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getIncludeAnnual() {
        return includeAnnual;
    }

    public void setIncludeAnnual(Boolean includeAnnual) {
        this.includeAnnual = includeAnnual;
    }

    public Boolean getIncludeTemporal() {
        return includeTemporal;
    }

    public void setIncludeTemporal(Boolean includeTemporal) {
        this.includeTemporal = includeTemporal;
    }
}
