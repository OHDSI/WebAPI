package org.ohdsi.webapi.check.checker.ir;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.checker.ir.helper.IRHelper;
import org.ohdsi.webapi.check.checker.tag.helper.TagHelper;
import org.ohdsi.webapi.cohortcharacterization.dto.CohortCharacterizationDTO;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IRChecker extends BaseChecker<IRAnalysisDTO> {
    private final TagHelper<IRAnalysisDTO> tagHelper;

    public IRChecker(TagHelper<IRAnalysisDTO> tagHelper) {
        this.tagHelper = tagHelper;
    }

    @PostConstruct
    public void init() {
        createValidator();
    }

    @Override
    protected List<ValidatorGroupBuilder<IRAnalysisDTO, ?>> getGroupBuilder() {

        return Arrays.asList(
                tagHelper.prepareTagBuilder(),
                IRHelper.prepareAnalysisExpressionBuilder()
        );
    }

}
