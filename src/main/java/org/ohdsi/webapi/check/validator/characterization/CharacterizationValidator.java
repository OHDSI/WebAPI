package org.ohdsi.webapi.check.validator.characterization;

import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;
import org.ohdsi.webapi.feanalysis.dto.FeAnalysisShortDTO;

import java.util.Collection;

public class CharacterizationValidator<T extends CohortCharacterizationDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Cohorts
        prepareCohortRule();

        // Feature Analyses
        prepareFeatureAnalysesRule();
    }

    private void prepareFeatureAnalysesRule() {
        Rule<T, Collection<FeAnalysisShortDTO>> rule = new Rule<T, Collection<FeAnalysisShortDTO>>()
                .setPath(createPath("feature analyses"))
                .setReporter(reporter)
                .setValueGetter(BaseCcDTO::getFeatureAnalyses)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }

    private void prepareCohortRule() {
        Rule<T, Collection<CohortMetadataDTO>> rule = new Rule<T, Collection<CohortMetadataDTO>>()
                .setPath(createPath("cohorts"))
                .setReporter(reporter)
                .setValueGetter(BaseCcDTO::getCohorts)
                .addValidator(new NotNullNotEmptyValidator<>());
        rules.add(rule);
    }
}
