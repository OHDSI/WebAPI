package org.ohdsi.webapi.check.checker.ir.helper;

import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.common.CriteriaGroupHelper;
import org.ohdsi.webapi.ircalc.StratifyRule;

public class IRStrataHelper {
    public static ValidatorGroupBuilder<StratifyRule, CriteriaGroup> prepareStrataBuilder() {
        ValidatorGroupBuilder<StratifyRule, CriteriaGroup> builder = new ValidatorGroupBuilder<StratifyRule, CriteriaGroup>()
                .attrName("stratify criteria")
                .valueGetter(t -> t.expression)
                .groups(
                        CriteriaGroupHelper.prepareCriteriaGroupBuilder(),
                        CriteriaGroupHelper.prepareDemographicBuilder()
                );
        return builder;
    }
}
