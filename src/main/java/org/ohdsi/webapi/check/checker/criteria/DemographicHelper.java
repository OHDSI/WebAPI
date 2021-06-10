package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.builder.ArrayForEachValidatorBuilder;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public class DemographicHelper {
    public static ValidatorGroupBuilder<CriteriaGroup, DemographicCriteria[]> prepareDemographicBuilder() {
        ValidatorGroupBuilder<CriteriaGroup, DemographicCriteria[]> builder =
                new ValidatorGroupBuilder<CriteriaGroup, DemographicCriteria[]>()
                        .attrName("demographic")
                        .valueGetter(t -> t.demographicCriteriaList)
                        .validators(
                                new ArrayForEachValidatorBuilder<DemographicCriteria>()
                                        .groups(
                                                prepareAgeBuilder(),
                                                prepareOccurrenceStartDateBuilder(),
                                                prepareOccurrenceEndDateBuilder()
                                        )
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DemographicCriteria, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<DemographicCriteria, NumericRange> builder =
                new ValidatorGroupBuilder<DemographicCriteria, NumericRange>()
                        .attrName("age")
                        .severity(WarningSeverity.CRITICAL)
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DemographicCriteria, DateRange> prepareOccurrenceStartDateBuilder() {
        ValidatorGroupBuilder<DemographicCriteria, DateRange> builder =
                new ValidatorGroupBuilder<DemographicCriteria, DateRange>()
                        .attrName("occurrence start date")
                        .severity(WarningSeverity.CRITICAL)
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DemographicCriteria, DateRange> prepareOccurrenceEndDateBuilder() {
        ValidatorGroupBuilder<DemographicCriteria, DateRange> builder =
                new ValidatorGroupBuilder<DemographicCriteria, DateRange>()
                        .attrName("occurrence end date")
                        .severity(WarningSeverity.CRITICAL)
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
