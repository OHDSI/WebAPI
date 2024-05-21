package org.ohdsi.webapi.cohortcharacterization.report;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

import java.util.Objects;
import org.ohdsi.webapi.cohortcharacterization.dto.CcDistributionStat;

public class ComparativeDistributionItem extends ComparativeItem {
    private final Double targetStdDev;
    private final Double targetMin;
    private final Double targetP10;
    private final Double targetP25;
    private final Double targetMedian;
    private final Double targetP75;
    private final Double targetP90;
    private final Double targetMax;
    private final Double targetAvg;

    private final Double comparatorStdDev;
    private final Double comparatorMin;
    private final Double comparatorP10;
    private final Double comparatorP25;
    private final Double comparatorMedian;
    private final Double comparatorP75;
    private final Double comparatorP90;
    private final Double comparatorMax;
    private final Double comparatorAvg;

    private final Integer aggregateId;
    private final String aggregateName;
    private final Boolean missingMeansZero;

		private static final CcDistributionStat EMPTY_ITEM;
		
    public ComparativeDistributionItem(DistributionItem firstItem, DistributionItem secondItem, CohortDefinition firstCohortDef,
                                       CohortDefinition secondCohortDef) {
        super(firstItem, secondItem, firstCohortDef, secondCohortDef);

        DistributionItem item = Objects.nonNull(firstItem) ? firstItem : secondItem;
        this.aggregateId = item.getAggregateId();
        this.aggregateName = item.getAggregateName();
        this.missingMeansZero = item.isMissingMeansZero();

        this.targetStdDev = firstItem != null ? firstItem.getStdDev() : null;
        this.targetMin = firstItem != null ? firstItem.getMin() : null;
        this.targetP10 = firstItem != null ? firstItem.getP10() : null;
        this.targetP25 = firstItem != null ? firstItem.getP25() : null;
        this.targetMedian = firstItem != null ? firstItem.getMedian() : null;
        this.targetP75 = firstItem != null ? firstItem.getP75() : null;
        this.targetP90 = firstItem != null ? firstItem.getP90() : null;
        this.targetMax = firstItem != null ? firstItem.getMax() : null;
        this.targetAvg = firstItem != null ? firstItem.avg : null;

        this.comparatorStdDev = secondItem != null ? secondItem.getStdDev() : null;
        this.comparatorMin = secondItem != null ? secondItem.getMin() : null;
        this.comparatorP10 = secondItem != null ? secondItem.getP10() : null;
        this.comparatorP25 = secondItem != null ? secondItem.getP25() : null;
        this.comparatorMedian = secondItem != null ? secondItem.getMedian() : null;
        this.comparatorP75 = secondItem != null ? secondItem.getP75() : null;
        this.comparatorP90 = secondItem != null ? secondItem.getP90() : null;
        this.comparatorMax = secondItem != null ? secondItem.getMax() : null;
        this.comparatorAvg = secondItem != null ? secondItem.avg : null;
    }
		static {
			EMPTY_ITEM = new CcDistributionStat();
			EMPTY_ITEM.setAvg(0.0d);
			EMPTY_ITEM.setStdDev(0.0d);
		}
		
		@Override
		protected double calcDiff(ExportItem first, ExportItem second) {
			if (first == null) {
				first = new DistributionItem(EMPTY_ITEM, this.getTargetCohortName());
			}

			if (second == null) {
				second = new DistributionItem(EMPTY_ITEM, this.getComparatorCohortName());
			}
			return first.calcDiff(second);
		}
		
    public Double getTargetStdDev() {
        return targetStdDev;
    }

    public Double getTargetMin() {
        return targetMin;
    }

    public Double getTargetP10() {
        return targetP10;
    }

    public Double getTargetP25() {
        return targetP25;
    }

    public Double getTargetMedian() {
        return targetMedian;
    }

    public Double getTargetP75() {
        return targetP75;
    }

    public Double getTargetP90() {
        return targetP90;
    }

    public Double getTargetMax() {
        return targetMax;
    }

    public Double getTargetAvg() {
        return targetAvg;
    }

    public Double getComparatorStdDev() {
        return comparatorStdDev;
    }

    public Double getComparatorMin() {
        return comparatorMin;
    }

    public Double getComparatorP10() {
        return comparatorP10;
    }

    public Double getComparatorP25() {
        return comparatorP25;
    }

    public Double getComparatorMedian() {
        return comparatorMedian;
    }

    public Double getComparatorP75() {
        return comparatorP75;
    }

    public Double getComparatorP90() {
        return comparatorP90;
    }

    public Double getComparatorMax() {
        return comparatorMax;
    }

    public Double getComparatorAvg() {
        return comparatorAvg;
    }

    public Integer getAggregateId() {
        return aggregateId;
    }

    public String getAggregateName() {
        return aggregateName;
    }

    public Boolean isMissingMeansZero() {
        return missingMeansZero;
    }
}
