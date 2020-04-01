package org.ohdsi.webapi.check.checker.estimation;

import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.estimation.EstimationValidator;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;

public class EstimationChecker extends BaseChecker<EstimationDTO> {
    @Override
    protected String getName(EstimationDTO value) {
        return value.getName();
    }

    @Override
    protected Validator<EstimationDTO> getValidator() {
        return new EstimationValidator<>();
    }
}
