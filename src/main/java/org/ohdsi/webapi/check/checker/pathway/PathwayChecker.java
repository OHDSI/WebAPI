package org.ohdsi.webapi.check.checker.pathway;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.checker.pathway.helper.PathwayHelper;
import org.ohdsi.webapi.pathway.dto.PathwayAnalysisDTO;
import org.springframework.stereotype.Component;

@Component
public class PathwayChecker extends BaseChecker<PathwayAnalysisDTO> {

    @PostConstruct
    public void init() {
        createValidator();
    }

    @Override
    protected List<ValidatorGroupBuilder<PathwayAnalysisDTO, ?>> getGroupBuilder() {
        return Arrays.asList(
                PathwayHelper.prepareTargetCohortsBuilder(),
                PathwayHelper.prepareEventCohortsBuilder(),
                PathwayHelper.prepareCombinationWindowBuilder(),
                PathwayHelper.prepareCellCountWindowBuilder(),
                PathwayHelper.prepareMaxPathLengthBuilder()
        );
    }
}
