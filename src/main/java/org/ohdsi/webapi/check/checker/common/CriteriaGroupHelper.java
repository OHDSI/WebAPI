package org.ohdsi.webapi.check.checker.common;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.cohortdefinition.DemographicCriteria;
import org.ohdsi.webapi.check.builder.ArrayForEachValidatorBuilder;
import org.ohdsi.webapi.check.builder.FakeTrueValidatorGroupBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;

public class CriteriaGroupHelper {
    private final static int MAX_RECURSION_DEPTH = 10;
    private final static int START_RECURSION_DEPTH = 1;

    public static ValidatorGroupBuilder<CriteriaGroup, DemographicCriteria[]> prepareDemographicBuilder() {
        return prepareDemographicBuilder(START_RECURSION_DEPTH);
    }

    public static ValidatorGroupBuilder<CriteriaGroup, DemographicCriteria[]> prepareDemographicBuilder(int depth) {
        ValidatorGroupBuilder<CriteriaGroup, DemographicCriteria[]> builder = new ValidatorGroupBuilder<CriteriaGroup, DemographicCriteria[]>()
                .attrName("level " + depth)
                .valueGetter(t -> t.demographicCriteriaList)
                .validators(
                        new ArrayForEachValidatorBuilder<DemographicCriteria>()
                                .groups(DemographicHelper.prepareDemographicCriteriaBuilder())
                );
        return builder;
    }

    public static ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]> prepareCriteriaGroupBuilder() {
        return prepareCriteriaGroupBuilder(START_RECURSION_DEPTH);
    }

    public static ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]> prepareCriteriaGroupBuilder(int depth) {
        if (depth > MAX_RECURSION_DEPTH) {
            return new FakeTrueValidatorGroupBuilder();
        }
        ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]> builder = new ValidatorGroupBuilder<CriteriaGroup, CriteriaGroup[]>()
                .valueGetter(t -> t.groups)
                .validators(
                        new ArrayForEachValidatorBuilder<CriteriaGroup>()
                                .groups(
                                        prepareDemographicBuilder(depth + 1),
                                        prepareCriteriaGroupBuilder(depth + 1))
                );
        return builder;
    }
}
