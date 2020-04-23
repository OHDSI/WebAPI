package org.ohdsi.webapi.check.checker.pathway;

import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.pathway.PathwayValidator;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;

public class PathwayChecker extends BaseChecker<PathwayAnalysisDTO> {
    @Override
    protected Validator<PathwayAnalysisDTO> getValidator() {
        return new PathwayValidator<>();
    }
}
