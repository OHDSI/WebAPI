package com.jnj.honeur.webapi.cohortfeaturesdist;

import javax.persistence.*;
import java.io.Serializable;

@Entity(name = "CohortFeaturesDistEntity")
@Table(name = "cohort_features_dist")
@IdClass(CohortFeaturesDistEntityId.class)
public class CohortFeaturesDistEntity implements Serializable {

    @Id
    @Column(name = "cohort_definition_id")
    private Long cohortDefinitionId;

    @Id
    @Column(name = "covariate_id")
    private Long covariateId;

    @Column(name = "count_value")
    private Double countValue;

    @Column(name = "min_value")
    private Double minValue;

    @Column(name = "max_value")
    private Double maxValue;

    @Column(name = "average_value")
    private Double averageValue;

    @Column(name = "standard_deviation")
    private Double standardDeviation;

    @Column(name = "median_value")
    private Double medianValue;

    @Column(name = "p10_value")
    private Double p10Value;

    @Column(name = "p25_value")
    private Double p25Value;

    @Column(name = "p75_value")
    private Double p75Value;

    @Column(name = "p90_value")
    private Double p90Value;

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

    public Double getCountValue() {
        return countValue;
    }

    public void setCountValue(Double countValue) {
        this.countValue = countValue;
    }

    public Double getMinValue() {
        return minValue;
    }

    public void setMinValue(Double minValue) {
        this.minValue = minValue;
    }

    public Double getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Double maxValue) {
        this.maxValue = maxValue;
    }

    public Double getAverageValue() {
        return averageValue;
    }

    public void setAverageValue(Double averageValue) {
        this.averageValue = averageValue;
    }

    public Double getStandardDeviation() {
        return standardDeviation;
    }

    public void setStandardDeviation(Double standardDeviation) {
        this.standardDeviation = standardDeviation;
    }

    public Double getMedianValue() {
        return medianValue;
    }

    public void setMedianValue(Double medianValue) {
        this.medianValue = medianValue;
    }

    public Double getP10Value() {
        return p10Value;
    }

    public void setP10Value(Double p10Value) {
        this.p10Value = p10Value;
    }

    public Double getP25Value() {
        return p25Value;
    }

    public void setP25Value(Double p25Value) {
        this.p25Value = p25Value;
    }

    public Double getP75Value() {
        return p75Value;
    }

    public void setP75Value(Double p75Value) {
        this.p75Value = p75Value;
    }

    public Double getP90Value() {
        return p90Value;
    }

    public void setP90Value(Double p90Value) {
        this.p90Value = p90Value;
    }
}
