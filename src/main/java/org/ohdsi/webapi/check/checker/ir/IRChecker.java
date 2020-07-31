package org.ohdsi.webapi.check.checker.ir;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.checker.ir.helper.IRHelper;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.springframework.stereotype.Component;

@Component
public class IRChecker extends BaseChecker<IRAnalysisDTO> {

    @PostConstruct
    public void init() {
        createValidator();
    }

    @Override
    protected List<ValidatorGroupBuilder<IRAnalysisDTO, ?>> getGroupBuilder() {

        return Arrays.asList(
                IRHelper.prepareAnalysisExpressionBuilder()
        );
    }

}
