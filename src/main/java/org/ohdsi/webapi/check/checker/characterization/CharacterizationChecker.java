package org.ohdsi.webapi.check.checker.characterization;

import static org.ohdsi.webapi.check.checker.characterization.helper.CharacterizationHelper.prepareCohortBuilder;
import static org.ohdsi.webapi.check.checker.characterization.helper.CharacterizationHelper.prepareFeatureAnalysesBuilder;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.springframework.stereotype.Component;

@Component
public class CharacterizationChecker extends BaseChecker<CohortCharacterizationDTO> {

    @PostConstruct
    public void init() {
        createValidator();
    }

    @Override
    protected List<ValidatorGroupBuilder<CohortCharacterizationDTO, ?>> getGroupBuilder() {

        return Arrays.asList(
                prepareCohortBuilder(),
                prepareFeatureAnalysesBuilder()
        );
    }

}
