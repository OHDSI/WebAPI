package com.jnj.honeur.webapi.cohortfeatures;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "CohortFeaturesEntity")
@Table(name = "cohort_features")
@IdClass(CohortFeaturesEntityId.class)
public class CohortFeaturesEntity implements Serializable {

    @Id
    @Column(name = "cohort_definition_id")
    private Long cohortDefinitionId;

    @Id
    @Column(name = "covariate_id")
    private Long covariateId;

    @Column(name = "sum_value")
    private Long sumValue;

    @Column(name = "average_value")
    private double averageValue;

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(Long cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    public Long getCovariateId() {
        return covariateId;
    }

    public void setCovariateId(Long covariateId) {
        this.covariateId = covariateId;
    }

    public Long getSumValue() {
        return sumValue;
    }

    public void setSumValue(Long sumValue) {
        this.sumValue = sumValue;
    }

    public double getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(double averageValue) {
        this.averageValue = averageValue;
    }
}
