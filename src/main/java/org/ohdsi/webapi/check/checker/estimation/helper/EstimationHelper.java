package org.ohdsi.webapi.check.checker.estimation.helper;

import static org.ohdsi.webapi.check.checker.estimation.helper.EstimationSpecificationHelper.*;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.estimation.design.EstimationAnalysis;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;

public class EstimationHelper {

    public static ValidatorGroupBuilder<EstimationDTO, EstimationAnalysis> prepareAnalysisExpressionBuilder() {

        ValidatorGroupBuilder<EstimationDTO, EstimationAnalysis> builder = new ValidatorGroupBuilder<EstimationDTO, EstimationAnalysis>()
                .attrName("specification")
                .valueGetter(estimation -> Utils.deserialize(estimation.getSpecification(), EstimationAnalysisImpl.class))
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                )
                .groups(
                        prepareAnalysisSettingsBuilder(),
                        preparePositiveSynthesisBuilder(),
                        prepareNegativeControlOutcomeBuilder(),
                        prepareNegativeControlBuilder()
                );

        return builder;
    }
}
