package com.jnj.honeur.webapi.cohortsummarystats;

import com.jnj.honeur.webapi.cohortinclusionstats.CohortInclusionStatsEntityId;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity(name = "CohortSummaryStatsEntity")
@Table(name = "cohort_summary_stats")
@IdClass(CohortSummaryStatsEntityId.class)
public class CohortSummaryStatsEntity implements Serializable {

    private static final long serialVersionUID = 8196131925781288320L;

    @Id
    @Column(name = "cohort_definition_id")
    private Long cohortDefinitionId;

    @Id
    @Column(name = "mode_id")
    private int modeId;

    @Column(name = "base_count")
    private Long baseCount;

    @Column(name = "final_count")
    private Long finalCount;

    public Long getCohortDefinitionId() {
        return cohortDefinitionId;
    }

    public void setCohortDefinitionId(Long cohortDefinitionId) {
        this.cohortDefinitionId = cohortDefinitionId;
    }

    public Long getBaseCount() {
        return baseCount;
    }

    public void setBaseCount(Long baseCount) {
        this.baseCount = baseCount;
    }

    public Long getFinalCount() {
        return finalCount;
    }

    public void setFinalCount(Long finalCount) {
        this.finalCount = finalCount;
    }

    public int getModeId() {
        return modeId;
    }

    public void setModeId(int modeId) {
        this.modeId = modeId;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CohortSummaryStatsEntity that = (CohortSummaryStatsEntity) o;
        return cohortDefinitionId == that.cohortDefinitionId &&
                modeId == that.modeId &&
                Objects.equals(baseCount, that.baseCount) &&
                Objects.equals(finalCount, that.finalCount);
    }

    @Override
    public int hashCode() {

        return Objects.hash(cohortDefinitionId, modeId, baseCount, finalCount);
    }
}
