package org.ohdsi.webapi.check.checker.criteria;

import org.ohdsi.circe.cohortdefinition.CorelatedCriteria;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.check.builder.ArrayForEachValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.builder.criteria.CorelatedCriteriaValidatorBuilder;

public class CorelatedCriteriaHelper {
    public static ValidatorGroupBuilder<CriteriaGroup, CorelatedCriteria[]> prepareCorelatedCriteriaBuilder() {
        ValidatorGroupBuilder<CriteriaGroup, CorelatedCriteria[]> builder =
                new ValidatorGroupBuilder<CriteriaGroup, CorelatedCriteria[]>()
                        .valueGetter(t -> t.criteriaList)
                        .validators(
                                new ArrayForEachValidatorBuilder<CorelatedCriteria>()
                                        .validators(
                                                new CorelatedCriteriaValidatorBuilder<>()
                                        )
                        );
        return builder;
    }
}
