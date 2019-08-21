package org.ohdsi.webapi.cohortcharacterization.report;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

public class ComparativeDistributionItem extends ComparativeItem {
    public final Double targetStdDev;
    public final Double targetMin;
    public final Double targetP10;
    public final Double targetP25;
    public final Double targetMedian;
    public final Double targetP75;
    public final Double targetP90;
    public final Double targetMax;
    public final Double targetAvg;

    public final Double comparatorStdDev;
    public final Double comparatorMin;
    public final Double comparatorP10;
    public final Double comparatorP25;
    public final Double comparatorMedian;
    public final Double comparatorP75;
    public final Double comparatorP90;
    public final Double comparatorMax;
    public final Double comparatorAvg;

    public ComparativeDistributionItem(DistributionItem firstItem, DistributionItem secondItem, CohortDefinition firstCohortDef,
                                       CohortDefinition secondCohortDef) {
        super(firstItem, secondItem, firstCohortDef, secondCohortDef);

        this.targetStdDev = firstItem != null ? ((DistributionItem) firstItem).stdDev : null;
        this.targetMin = firstItem != null ? ((DistributionItem) firstItem).min : null;
        this.targetP10 = firstItem != null ? ((DistributionItem) firstItem).p10 : null;
        this.targetP25 = firstItem != null ? ((DistributionItem) firstItem).p25 : null;
        this.targetMedian = firstItem != null ? ((DistributionItem) firstItem).median : null;
        this.targetP75 = firstItem != null ? ((DistributionItem) firstItem).p75 : null;
        this.targetP90 = firstItem != null ? ((DistributionItem) firstItem).p90 : null;
        this.targetMax = firstItem != null ? ((DistributionItem) firstItem).max : null;
        this.targetAvg = firstItem != null ? ((DistributionItem) firstItem).avg : null;

        this.comparatorStdDev = secondItem != null ? ((DistributionItem) secondItem).stdDev : null;
        this.comparatorMin = secondItem != null ? ((DistributionItem) secondItem).min : null;
        this.comparatorP10 = secondItem != null ? ((DistributionItem) secondItem).p10 : null;
        this.comparatorP25 = secondItem != null ? ((DistributionItem) secondItem).p25 : null;
        this.comparatorMedian = secondItem != null ? ((DistributionItem) secondItem).median : null;
        this.comparatorP75 = secondItem != null ? ((DistributionItem) secondItem).p75 : null;
        this.comparatorP90 = secondItem != null ? ((DistributionItem) secondItem).p90 : null;
        this.comparatorMax = secondItem != null ? ((DistributionItem) secondItem).max : null;
        this.comparatorAvg = secondItem != null ? ((DistributionItem) secondItem).avg : null;
    }
}
