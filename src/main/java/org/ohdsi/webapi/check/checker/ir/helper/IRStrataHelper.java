package org.ohdsi.webapi.check.checker.ir.helper;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.ircalc.StratifyRule;

import static org.ohdsi.webapi.check.checker.criteria.CorelatedCriteriaHelper.prepareCorelatedCriteriaBuilder;
import static org.ohdsi.webapi.check.checker.criteria.CriteriaGroupHelper.prepareCriteriaGroupArrayBuilder;
import static org.ohdsi.webapi.check.checker.criteria.DemographicHelper.prepareDemographicBuilder;

public class IRStrataHelper {
    public static ValidatorGroupBuilder<StratifyRule, CriteriaGroup> prepareStrataBuilder() {
        ValidatorGroupBuilder<StratifyRule, CriteriaGroup> builder = new ValidatorGroupBuilder<StratifyRule, CriteriaGroup>()
                .attrName("stratify criteria")
                .valueGetter(t -> t.expression)
                .groups(
                        prepareCriteriaGroupArrayBuilder(),
                        prepareDemographicBuilder(),
                        prepareCorelatedCriteriaBuilder()
                );
        return builder;
    }
}
