package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.ConceptSetSelection;
import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.VisitDetail;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.builder.conceptset.ConceptSetSelectionValidatorBuilder;

public class VisitDetailHelper {
    public static ValidatorGroupBuilder<Criteria, VisitDetail> prepareVisitDetailBuilder() {
        ValidatorGroupBuilder<Criteria, VisitDetail> builder =
                new ValidatorGroupBuilder<Criteria, VisitDetail>()
                        .attrName("VisitDetail")
                        .conditionGetter(t -> t instanceof VisitDetail)
                        .valueGetter(t -> (VisitDetail) t)
                        .groups(
                                prepareAgeBuilder(),
                                prepareStartDateBuilder(),
                                prepareEndDateBuilder(),
                                prepareVisitDetailLengthBuilder(),
                                prepareVisitDetailTypeBuilder(),
                                prepareGenderBuilder(),
                                prepareProviderSpecialtyBuilder(),
                                preparePlaceOfServiceCBuilder()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitDetail, NumericRange> prepareAgeBuilder() {
        ValidatorGroupBuilder<VisitDetail, NumericRange> builder =
                new ValidatorGroupBuilder<VisitDetail, NumericRange>()
                        .attrName("age")
                        .valueGetter(t -> t.age)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitDetail, DateRange> prepareStartDateBuilder() {
        ValidatorGroupBuilder<VisitDetail, DateRange> builder =
                new ValidatorGroupBuilder<VisitDetail, DateRange>()
                        .attrName("visit detail start date")
                        .valueGetter(t -> t.visitDetailStartDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitDetail, DateRange> prepareEndDateBuilder() {
        ValidatorGroupBuilder<VisitDetail, DateRange> builder =
                new ValidatorGroupBuilder<VisitDetail, DateRange>()
                        .attrName("visit detail end date")
                        .valueGetter(t -> t.visitDetailEndDate)
                        .validators(
                                new DateRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitDetail, NumericRange> prepareVisitDetailLengthBuilder() {
        ValidatorGroupBuilder<VisitDetail, NumericRange> builder =
                new ValidatorGroupBuilder<VisitDetail, NumericRange>()
                        .attrName("visit detail length")
                        .valueGetter(t -> t.visitDetailLength)
                        .validators(
                                new NumericRangeValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitDetail, ConceptSetSelection> prepareVisitDetailTypeBuilder() {
        ValidatorGroupBuilder<VisitDetail, ConceptSetSelection> builder =
                new ValidatorGroupBuilder<VisitDetail, ConceptSetSelection>()
                        .attrName("visit detail type")
                        .valueGetter(t -> t.visitDetailTypeCS)
                        .validators(
                                new ConceptSetSelectionValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitDetail, ConceptSetSelection> prepareGenderBuilder() {
        ValidatorGroupBuilder<VisitDetail, ConceptSetSelection> builder =
                new ValidatorGroupBuilder<VisitDetail, ConceptSetSelection>()
                        .attrName("visit detail gender")
                        .valueGetter(t -> t.genderCS)
                        .validators(
                                new ConceptSetSelectionValidatorBuilder<>()
                        );
        return builder;
    }

    private static ValidatorGroupBuilder<VisitDetail, ConceptSetSelection> prepareProviderSpecialtyBuilder() {
        ValidatorGroupBuilder<VisitDetail, ConceptSetSelection> builder =
            new ValidatorGroupBuilder<VisitDetail, ConceptSetSelection>()
                    .attrName("visit detail provider speciality")
                    .valueGetter(t -> t.providerSpecialtyCS)
                    .validators(
                            new ConceptSetSelectionValidatorBuilder<>()
                    );
        return builder;

    }

    private static ValidatorGroupBuilder<VisitDetail, ConceptSetSelection> preparePlaceOfServiceCBuilder() {
        ValidatorGroupBuilder<VisitDetail, ConceptSetSelection> builder =
            new ValidatorGroupBuilder<VisitDetail, ConceptSetSelection>()
                    .attrName("visit detail place of service")
                    .valueGetter(t -> t.placeOfServiceCS)
                    .validators(
                            new ConceptSetSelectionValidatorBuilder<>()
                    );
        return builder;
    }
}
