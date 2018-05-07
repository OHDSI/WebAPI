package com.jnj.honeur.webapi.cohortdefinition;

import com.jnj.honeur.webapi.cohortinclusion.CohortInclusionEntity;
import com.jnj.honeur.webapi.cohortinclusionresult.CohortInclusionResultEntity;
import com.jnj.honeur.webapi.cohortinclusionstats.CohortInclusionStatsEntity;
import com.jnj.honeur.webapi.cohortsummarystats.CohortSummaryStatsEntity;
import org.ohdsi.webapi.cohort.CohortEntity;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

public class CohortGenerationResults implements Serializable {

    private List<CohortEntity> cohort;
    private List<CohortInclusionEntity> cohortInclusion;
    private List<CohortInclusionResultEntity> cohortInclusionResult;
    private List<CohortInclusionStatsEntity> cohortInclusionStats;
    private List<CohortSummaryStatsEntity> cohortSummaryStats;
    private CohortGenerationInfo cohortGenerationInfo;

    public List<CohortEntity> getCohort() {
        return cohort;
    }

    public void setCohort(List<CohortEntity> cohort) {
        this.cohort = cohort;
    }

    public List<CohortInclusionEntity> getCohortInclusion() {
        return cohortInclusion;
    }

    public void setCohortInclusion(List<CohortInclusionEntity> cohortInclusion) {
        this.cohortInclusion = cohortInclusion;
    }

    public List<CohortInclusionResultEntity> getCohortInclusionResult() {
        return cohortInclusionResult;
    }

    public void setCohortInclusionResult(
            List<CohortInclusionResultEntity> cohortInclusionResult) {
        this.cohortInclusionResult = cohortInclusionResult;
    }

    public List<CohortInclusionStatsEntity> getCohortInclusionStats() {
        return cohortInclusionStats;
    }

    public void setCohortInclusionStats(
            List<CohortInclusionStatsEntity> cohortInclusionStats) {
        this.cohortInclusionStats = cohortInclusionStats;
    }

    public List<CohortSummaryStatsEntity> getCohortSummaryStats() {
        return cohortSummaryStats;
    }

    public void setCohortSummaryStats(
            List<CohortSummaryStatsEntity> cohortSummaryStats) {
        this.cohortSummaryStats = cohortSummaryStats;
    }

    public CohortGenerationInfo getCohortGenerationInfo() {
        return cohortGenerationInfo;
    }

    public void setCohortGenerationInfo(CohortGenerationInfo cohortGenerationInfo) {
        this.cohortGenerationInfo = cohortGenerationInfo;
    }
}
