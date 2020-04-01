package org.ohdsi.webapi.check.checker.ir;

import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.ir.IRValidator;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.springframework.stereotype.Component;

@Component
public class IRChecker extends BaseChecker<IRAnalysisDTO> {
    @Override
    protected String getName(IRAnalysisDTO value) {
        return value.getName();
    }

    @Override
    protected Validator<IRAnalysisDTO> getValidator() {
        return new IRValidator<>();
    }
}
