package org.ohdsi.webapi.check.validator.characterization;

import org.ohdsi.webapi.check.validator.Rule;
import org.ohdsi.webapi.check.validator.RuleValidator;
import org.ohdsi.webapi.cohortcharacterization.dto.BaseCcDTO;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;

public class CharacterizationValidator<T extends CohortCharacterizationDTO> extends RuleValidator<T> {
    @Override
    protected void buildInternal() {
        // Cohorts
        prepareCohortRule();

        // Feature Analyses
        prepareFeatureAnalysesRule();
    }

    private void prepareFeatureAnalysesRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("feature analyses"), reporter)
                        .setValueGetter( BaseCcDTO::getFeatureAnalyses);
        rules.add(rule);
    }

    private void prepareCohortRule() {
        Rule<T> rule =
                createRuleWithDefaultValidator(createPath("cohorts"), reporter)
                        .setValueGetter(BaseCcDTO::getCohorts);
        rules.add(rule);
    }
}
