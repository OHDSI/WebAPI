package org.ohdsi.webapi.check.checker.common;

import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.warning.WarningSeverity;

public class DemographicHelper {
    public static ValidatorGroupBuilder<DemographicCriteria, NumericRange> prepareAgeBuilder() {

        ValidatorGroupBuilder<DemographicCriteria, NumericRange> builder = new ValidatorGroupBuilder<DemographicCriteria, NumericRange>()
                .attrName("age")
                .severity(WarningSeverity.CRITICAL)
                .valueGetter(t -> t.age)
                .validators(
                        new NumericRangeValidatorBuilder<>()
                );
        return builder;
    }

    public static ValidatorGroupBuilder<DemographicCriteria, DemographicCriteria> prepareDemographicCriteriaBuilder() {

        ValidatorGroupBuilder<DemographicCriteria, DemographicCriteria> builder = new ValidatorGroupBuilder<DemographicCriteria, DemographicCriteria>()
                .attrName("demographic criteria")
                .valueGetter(t -> t)
                .groups(
                        prepareAgeBuilder()
                );
        return builder;
    }
}
