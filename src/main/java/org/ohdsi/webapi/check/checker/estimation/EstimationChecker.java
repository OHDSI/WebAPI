package org.ohdsi.webapi.check.checker.estimation;

import static org.ohdsi.webapi.check.checker.estimation.helper.EstimationHelper.*;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.estimation.dto.EstimationDTO;
import org.springframework.stereotype.Component;

@Component
public class EstimationChecker extends BaseChecker<EstimationDTO> {

    @PostConstruct
    public void init() {
        createValidator();
    }

    @Override
    protected List<ValidatorGroupBuilder<EstimationDTO, ?>> getGroupBuilder() {

        return Arrays.asList(
                prepareAnalysisExpressionBuilder()
        );
    }
}
