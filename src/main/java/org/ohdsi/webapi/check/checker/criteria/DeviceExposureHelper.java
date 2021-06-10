package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.DeviceExposure;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class DeviceExposureHelper {
    public static ValidatorGroupBuilder<Criteria, DeviceExposure> prepareDeviceExposureBuilder() {
        ValidatorGroupBuilder<Criteria, DeviceExposure> builder =
                new ValidatorGroupBuilder<Criteria, DeviceExposure>()
                        .attrName("device exposure")
                        .conditionGetter(t -> t instanceof DeviceExposure)
                        .valueGetter(t -> (DeviceExposure) t)
                        .groups(
                                prepareAgeBuilder(),
                                prepareStartDateBuilder(),
                                prepareEndDateBuilder(),
                                prepareQuantityBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DeviceExposure, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<DeviceExposure, NumericRange> builder =
                new ValidatorGroupBuilder<DeviceExposure, NumericRange>()
                        .attrName("age")
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DeviceExposure, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<DeviceExposure, DateRange> builder =
                new ValidatorGroupBuilder<DeviceExposure, DateRange>()
                        .attrName("occurrence start date")
                        .valueGetter(t -> t.occurrenceStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DeviceExposure, DateRange> prepareEndDateBuilder() {
        ValidatorGroupBuilder<DeviceExposure, DateRange> builder =
                new ValidatorGroupBuilder<DeviceExposure, DateRange>()
                        .attrName("occurrence end date")
                        .valueGetter(t -> t.occurrenceEndDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<DeviceExposure, NumericRange> prepareQuantityBuilder() {
        ValidatorGroupBuilder<DeviceExposure, NumericRange> builder =
                new ValidatorGroupBuilder<DeviceExposure, NumericRange>()
                        .attrName("quantity")
                        .valueGetter(t -> t.quantity)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
