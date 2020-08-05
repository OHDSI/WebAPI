package org.ohdsi.webapi.check.checker.pathway.helper;

import java.util.List;
import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.PredicateValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.pathway.dto.BasePathwayAnalysisDTO;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;

public class PathwayHelper {

    public static ValidatorGroupBuilder<PathwayAnalysisDTO, Integer> prepareMaxPathLengthBuilder() {

        ValidatorGroupBuilder<PathwayAnalysisDTO, Integer> builder = new ValidatorGroupBuilder<PathwayAnalysisDTO, Integer>()
                .attrName("maximum path length")
                .valueGetter(BasePathwayAnalysisDTO::getMaxDepth)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Integer>()
                                .predicate(v -> v >= 1 && v <= 10)
                                .errorMessage("must be between 1 and 10")
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PathwayAnalysisDTO, Integer> prepareCellCountWindowBuilder() {

        ValidatorGroupBuilder<PathwayAnalysisDTO, Integer> builder = new ValidatorGroupBuilder<PathwayAnalysisDTO, Integer>()
                .attrName("minimum cell count")
                .valueGetter(BasePathwayAnalysisDTO::getMinCellCount)

                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Integer>()
                                .predicate(v -> v >= 0)
                                .errorMessage("must be greater or equal to 0")
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PathwayAnalysisDTO, Integer> prepareCombinationWindowBuilder() {

        ValidatorGroupBuilder<PathwayAnalysisDTO, Integer> builder = new ValidatorGroupBuilder<PathwayAnalysisDTO, Integer>()
                .attrName("combination window")
                .valueGetter(BasePathwayAnalysisDTO::getCombinationWindow)

                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new PredicateValidatorBuilder<Integer>()
                                .predicate(v -> v >= 0)
                                .errorMessage("must be greater or equal to 0")
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PathwayAnalysisDTO, List<? extends CohortMetadata>> prepareEventCohortsBuilder() {

        ValidatorGroupBuilder<PathwayAnalysisDTO, List<? extends CohortMetadata>> builder = new ValidatorGroupBuilder<PathwayAnalysisDTO, List<? extends CohortMetadata>>()
                .attrName("event cohorts")
                .valueGetter(BasePathwayAnalysisDTO::getEventCohorts)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                );
        return builder;
    }

    public static ValidatorGroupBuilder<PathwayAnalysisDTO, List<? extends CohortMetadata>> prepareTargetCohortsBuilder() {

        ValidatorGroupBuilder<PathwayAnalysisDTO, List<? extends CohortMetadata>> builder = new ValidatorGroupBuilder<PathwayAnalysisDTO, List<? extends CohortMetadata>>()
                .attrName("target cohorts")
                .valueGetter(BasePathwayAnalysisDTO::getTargetCohorts)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>()
                );
        return builder;
    }
}
