package org.ohdsi.webapi.check.checker.characterization;

import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.characterization.CharacterizationValidator;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;

public class CharacterizationChecker extends BaseChecker<CohortCharacterizationDTO> {
    @Override
    protected String getName(CohortCharacterizationDTO value) {
        return value.getName();
    }

    @Override
    protected Validator<CohortCharacterizationDTO> getValidator() {
        return new CharacterizationValidator<>();
    }
}
