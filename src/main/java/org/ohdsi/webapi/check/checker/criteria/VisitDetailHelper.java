package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.DateRange;
import org.ohdsi.circe.cohortdefinition.NumericRange;
import org.ohdsi.circe.cohortdefinition.VisitDetail;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.webapi.check.builder.DateRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.NumericRangeValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

import static org.ohdsi.webapi.check.checker.concept.ConceptArrayHelper.prepareConceptBuilder;

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
                                prepareVisitDetailAdmittedFromConceptBuilder(),
                                prepareVisitDetailTypeBuilder(),
                                prepareVisitDetailDischargedToConceptBuilder(),
                                prepareVisitDetailGenderBuilder(),
                                prepareVisitDetailPlaceOfServiceBuilder(),
                                prepareVisitDetailProviderSpecialityBuilder()
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

    private static ValidatorGroupBuilder<VisitDetail, Concept[]> prepareVisitDetailAdmittedFromConceptBuilder() {
        return prepareConceptBuilder(t -> t.admittedFromConcept, "visit detail admitted from concept");
    }

    private static ValidatorGroupBuilder<VisitDetail, Concept[]> prepareVisitDetailTypeBuilder() {
        return prepareConceptBuilder(t -> t.visitDetailType, "visit detail type");
    }

    private static ValidatorGroupBuilder<VisitDetail, Concept[]> prepareVisitDetailDischargedToConceptBuilder() {
        return prepareConceptBuilder(t -> t.dischargedToConcept, "visit detail discharged to concept");
    }

    private static ValidatorGroupBuilder<VisitDetail, Concept[]> prepareVisitDetailGenderBuilder() {
        return prepareConceptBuilder(t -> t.gender, "visit detail gender");
    }

    private static ValidatorGroupBuilder<VisitDetail, Concept[]> prepareVisitDetailPlaceOfServiceBuilder() {
        return prepareConceptBuilder(t -> t.placeOfService, "visit detail place of service");
    }

    private static ValidatorGroupBuilder<VisitDetail, Concept[]> prepareVisitDetailProviderSpecialityBuilder() {
        return prepareConceptBuilder(t -> t.providerSpecialty, "visit detail provider speciality");
    }
}
