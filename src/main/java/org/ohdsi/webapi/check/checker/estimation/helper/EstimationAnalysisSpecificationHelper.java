package org.ohdsi.webapi.check.checker.estimation.helper;

import org.ohdsi.analysis.Utils;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.CohortMethodAnalysis;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.ComparativeCohortAnalysis;
import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.TargetComparatorOutcomes;
import org.ohdsi.webapi.check.builder.DuplicateValidatorBuilder;
import org.ohdsi.webapi.check.builder.IterableForEachValidatorBuilder;
import org.ohdsi.webapi.check.builder.NotNullNotEmptyValidatorBuilder;
import org.ohdsi.webapi.check.builder.PredicateValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.estimation.comparativecohortanalysis.specification.CohortMethodAnalysisImpl;

import java.util.Collection;
import java.util.function.Function;

public class EstimationAnalysisSpecificationHelper {

    public static ValidatorGroupBuilder<ComparativeCohortAnalysis, Collection<? extends TargetComparatorOutcomes>> prepareTargetComparatorBuilder() {

        ValidatorBuilder<TargetComparatorOutcomes> predicateValidatorBuilder = new PredicateValidatorBuilder<TargetComparatorOutcomes>()
                .predicate(t -> {
                    if (t != null) {
                        return t.getComparatorId() != null
                                && t.getTargetId() != null
                                && t.getOutcomeIds().size() > 0;
                    }
                    return false;
                })
                .errorMessage("no target, comparator or outcome");

        ValidatorGroupBuilder<ComparativeCohortAnalysis, Collection<? extends TargetComparatorOutcomes>> builder = new ValidatorGroupBuilder<ComparativeCohortAnalysis, Collection<? extends TargetComparatorOutcomes>>()
                .attrName("target comparator outcome")
                .valueGetter(ComparativeCohortAnalysis::getTargetComparatorOutcomes)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new DuplicateValidatorBuilder<TargetComparatorOutcomes, String>()
                                .elementGetter(t -> String.format("%s,%s", t.getTargetId(), t.getComparatorId())),
                        new IterableForEachValidatorBuilder<TargetComparatorOutcomes>()
                                .validators(predicateValidatorBuilder)
                );
        return builder;
    }

    public static ValidatorGroupBuilder<ComparativeCohortAnalysis, Collection<? extends CohortMethodAnalysis>> prepareAnalysisSettingsBuilder() {

        Function<CohortMethodAnalysis, String> elementGetter = value -> {
            CohortMethodAnalysisImpl analysis = ((CohortMethodAnalysisImpl) value);
            Integer analysisId = analysis.getAnalysisId();
            String description = analysis.getDescription();

            // remove identifier and description
            analysis.setAnalysisId(null);
            analysis.setDescription(null);

            String json = Utils.serialize(analysis);

            // restore identifier and description
            analysis.setAnalysisId(analysisId);
            analysis.setDescription(description);

            return json;
        };

        ValidatorBuilder<CohortMethodAnalysis> iptwValidatorBuilder = new PredicateValidatorBuilder<CohortMethodAnalysis>()
                .predicate(t -> {
                    if (t != null && t.getFitOutcomeModelArgs() != null) {
                        if (Boolean.TRUE.equals(t.getFitOutcomeModelArgs().getInversePtWeighting())) {
                            return Boolean.TRUE.equals(t.getCreatePs());
                        } else {
                            return true;
                        }
                    }
                    return false;
                })
                .attrNameValueGetter(t -> t.getDescription())
                .errorMessage("createPs should be set to true when inverse probability of treatment weighting (IPTW) for the outcome model is specified ");

        ValidatorGroupBuilder<ComparativeCohortAnalysis, Collection<? extends CohortMethodAnalysis>> builder =
                new ValidatorGroupBuilder<ComparativeCohortAnalysis, Collection<? extends CohortMethodAnalysis>>()
                .attrName("analysis settings")
                .valueGetter(ComparativeCohortAnalysis::getCohortMethodAnalysisList)
                .validators(
                        new NotNullNotEmptyValidatorBuilder<>(),
                        new DuplicateValidatorBuilder<CohortMethodAnalysis, String>()
                                .elementGetter(elementGetter),
                        new IterableForEachValidatorBuilder<CohortMethodAnalysis>()
                                .validators(iptwValidatorBuilder)
                );
        return builder;
    }
}
