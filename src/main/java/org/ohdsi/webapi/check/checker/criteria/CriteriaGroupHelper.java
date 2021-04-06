package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.Criteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.check.builder.ArrayForEachValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.builder.criteria.CriteriaGroupValidatorBuilder;

import static org.ohdsi.webapi.check.checker.criteria.CorelatedCriteriaHelper.prepareCorelatedCriteriaBuilder;
import static org.ohdsi.webapi.check.checker.criteria.DemographicHelper.prepareDemographicBuilder;

public class CriteriaGroupHelper {
    private static ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]> prepareGroupBuilder() {
        ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]> builder =
                new ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]>()
                        .valueGetter(t -> t.groups)
                        .validators(
                                new ArrayForEachValidatorBuilder<CriteriaGroup>()
                                        .validators(
                                                new CriteriaGroupValidatorBuilder<>()
                                        )
                        );
        return builder;
    }

    public static ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]> prepareCriteriaGroupArrayBuilder() {
        ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]> builder = new ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]>()
                .valueGetter(t -> t.groups)
                .validators(
                        new ArrayForEachValidatorBuilder<CriteriaGroup>()
                                .groups(
                                        prepareDemographicBuilder(),
                                        prepareCorelatedCriteriaBuilder(),
                                        prepareGroupBuilder())
                );
        return builder;
    }

    public static ValidatorGroupBuilder<Criteria, CriteriaGroup> prepareCriteriaGroupBuilder() {
        ValidatorGroupBuilder<Criteria, CriteriaGroup> builder = new ValidatorGroupBuilder<Criteria, CriteriaGroup>()
                .valueGetter(t -> t.CorrelatedCriteria)
                .groups(
                        prepareDemographicBuilder(),
                        prepareCorelatedCriteriaBuilder(),
                        prepareGroupBuilder()
                );
        return builder;
    }
}
