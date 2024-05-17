package org.ohdsi.webapi.check.checker.characterization;

import jakarta.annotation.PostConstruct;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.checker.tag.helper.TagHelper;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

import static org.ohdsi.webapi.check.checker.characterization.helper.CharacterizationHelper.prepareCohortBuilder;
import static org.ohdsi.webapi.check.checker.characterization.helper.CharacterizationHelper.prepareFeatureAnalysesBuilder;
import static org.ohdsi.webapi.check.checker.characterization.helper.CharacterizationHelper.prepareStratifyRuleBuilder;

@Component
public class CharacterizationChecker extends BaseChecker<CohortCharacterizationDTO> {
    private final TagHelper<CohortCharacterizationDTO> tagHelper;

    public CharacterizationChecker(TagHelper<CohortCharacterizationDTO> tagHelper) {
        this.tagHelper = tagHelper;
    }

    @PostConstruct
    public void init() {
        createValidator();
    }

    @Override
    protected List<ValidatorGroupBuilder<CohortCharacterizationDTO, ?>> getGroupBuilder() {

        return Arrays.asList(
                tagHelper.prepareTagBuilder(),
                prepareCohortBuilder(),
                prepareFeatureAnalysesBuilder(),
                prepareStratifyRuleBuilder()
        );
    }

}
