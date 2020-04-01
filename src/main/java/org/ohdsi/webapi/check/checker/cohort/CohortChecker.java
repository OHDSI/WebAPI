package org.ohdsi.webapi.check.checker.cohort;

import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.cohort.CohortValidator;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;

public class CohortChecker extends BaseChecker<CohortDTO> {
    @Override
    protected String getName(CohortDTO value) {
        return value.getName();
    }

    @Override
    protected Validator<CohortDTO> getValidator() {
        return new CohortValidator<>();
    }
}
