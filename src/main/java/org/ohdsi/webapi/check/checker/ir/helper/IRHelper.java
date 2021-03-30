package org.ohdsi.webapi.check.checker.ir.helper;

import org.ohdsi.analysis.Utils;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.ircalc.IncidenceRateAnalysisExpression;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;

import java.util.function.Function;

public class IRHelper {

    public static ValidatorGroupBuilder<IRAnalysisDTO, IncidenceRateAnalysisExpression> prepareAnalysisExpressionBuilder() {

        Function<IRAnalysisDTO, IncidenceRateAnalysisExpression> valueGetter = t -> Utils.deserialize(t.getExpression(), IncidenceRateAnalysisExpression.class);

        ValidatorGroupBuilder<IRAnalysisDTO, IncidenceRateAnalysisExpression> builder = new ValidatorGroupBuilder<IRAnalysisDTO, IncidenceRateAnalysisExpression>()
                .valueGetter(valueGetter)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<IncidenceRateAnalysisExpression>()
                                .attrName("expression")
                )
                .groups(
                        IRAnalysisExpressionHelper.prepareTargetCohortsBuilder(),
                        IRAnalysisExpressionHelper.prepareOutcomeCohortsBuilder(),
                        IRAnalysisExpressionHelper.prepareStratifyRuleBuilder(),
                        IRAnalysisExpressionHelper.prepareStudyWindowBuilder()
                );
        return builder;
    }
}
