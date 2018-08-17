package org.ohdsi.webapi.cohortcharacterization.domain;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;
import org.hibernate.annotations.DiscriminatorFormula;
import org.ohdsi.standardized_analysis_api.cohortcharacterization.result.PrevalenceStat;
import org.ohdsi.webapi.cohortcharacterization.domain.CcResultEntity;

@Entity
@Table(name = "cohort_characterization_results")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorFormula("type")
@DiscriminatorValue("PREVALENCE")
public class CcPrevalenceStat extends CcResultEntity implements PrevalenceStat {
    
    @Column
    private String timeWindow;
    @Column
    private Long value;
    @Column
    private Double proportion;
    @Column
    private Long covariateId;
    @Column
    private String covariateName;
    @Column
    private Long conceptId;
    @Column
    private Long count;
    
    @Override
    public Double getProportion() {
        return this.proportion;
    }

    @Override
    public Long getCovariateId() {
        return covariateId;
    }

    @Override
    public String getCovariateName() {
        return covariateName;
    }

    @Override
    public Long getConceptId() {
        return conceptId;
    }

    @Override
    public Long getCount() {
        return count;
    }

    public void setCovariateId(final Long covariateId) {
        this.covariateId = covariateId;
    }

    public void setCovariateName(final String covariateName) {
        this.covariateName = covariateName;
    }

    public void setConceptId(final Long conceptId) {
        this.conceptId = conceptId;
    }

    public void setCount(final Long count) {
        this.count = count;
    }
    
    public String getTimeWindow() {
        return timeWindow;
    }

    public void setTimeWindow(final String timeWindow) {
        this.timeWindow = timeWindow;
    }
}
