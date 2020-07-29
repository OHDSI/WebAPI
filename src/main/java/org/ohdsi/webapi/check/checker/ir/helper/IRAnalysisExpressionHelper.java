package org.ohdsi.webapi.check.checker.ir.helper;

import java.util.List;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExpression;

public class IRAnalysisExpressionHelper {


    public static ValidatorGroupBuilder<IncidenceRateAnalysisExpression, List<Integer>> prepareOutcomeCohortsBuilder() {

        ValidatorGroupBuilder<IncidenceRateAnalysisExpression, List<Integer>> builder = new ValidatorGroupBuilder<IncidenceRateAnalysisExpression, List<Integer>>()
                .attrName("outcome cohorts")
                .valueGetter(t -> t.outcomeIds)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                );
        return builder;
    }

    public static ValidatorGroupBuilder<IncidenceRateAnalysisExpression, List<Integer>> prepareTargetCohortsBuilder() {

        ValidatorGroupBuilder<IncidenceRateAnalysisExpression, List<Integer>> builder = new ValidatorGroupBuilder<IncidenceRateAnalysisExpression, List<Integer>>()
                .attrName("target cohorts")
                .valueGetter(t -> t.targetIds)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                );
        return builder;
    }
}
