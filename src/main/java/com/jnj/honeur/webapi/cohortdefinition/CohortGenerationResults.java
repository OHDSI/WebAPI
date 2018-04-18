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

//    public static class Cohort {
//        public long id;
//        public Date startDate;
//        public Date endDate;
//    }
//    public static class CohortInclusion {
//        public int ruleSequence;
//        public String name;
//        public String description;
//    }
//    public static class CohortInclusionResult {
//        public long inclusionRuleMask;
//        public long personCount;
//    }
//    public static class CohortInclusionStats {
//        public int ruleSequence;
//        public long personCount;
//        public long gainCount;
//        public long personTotal;
//    }
//    public static class CohortSummaryStats {
//        public long baseCount;
//        public long finalCount;
//    }

    public List<CohortEntity> cohort;
    public List<CohortInclusionEntity> cohortInclusion;
    public List<CohortInclusionResultEntity> cohortInclusionResult;
    public List<CohortInclusionStatsEntity> cohortInclusionStats;
    public List<CohortSummaryStatsEntity> cohortSummaryStats;
    public List<CohortGenerationInfo> cohortGenerationInfo;
}
