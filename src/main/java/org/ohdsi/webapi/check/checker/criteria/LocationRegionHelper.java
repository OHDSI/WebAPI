package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.LocationRegion;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class LocationRegionHelper {
    public static ValidatorGroupBuilder<Criteria, LocationRegion> prepareLocationRegionBuilder() {
        ValidatorGroupBuilder<Criteria, LocationRegion> builder =
                new ValidatorGroupBuilder<Criteria, LocationRegion>()
                        .attrName("location region")
                        .conditionGetter(t -> t instanceof LocationRegion)
                        .valueGetter(t -> (LocationRegion) t)
                        .groups(
                                prepareStartDateBuilder(),
                                prepareEndDateBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<LocationRegion, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<LocationRegion, DateRange> builder =
                new ValidatorGroupBuilder<LocationRegion, DateRange>()
                        .attrName("location region start date")
                        .valueGetter(t -> t.startDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<LocationRegion, DateRange> prepareEndDateBuilder() {
        ValidatorGroupBuilder<LocationRegion, DateRange> builder =
                new ValidatorGroupBuilder<LocationRegion, DateRange>()
                        .attrName("location region end date")
                        .valueGetter(t -> t.endDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }
}
