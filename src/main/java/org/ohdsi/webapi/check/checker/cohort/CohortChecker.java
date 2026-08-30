package org.ohdsi.webapi.check.checker.cohort;

import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.checker.tag.helper.TagHelper;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Component
public class CohortChecker extends BaseChecker<CohortDTO> {
    private final TagHelper<CohortDTO> tagHelper;

    public CohortChecker(TagHelper<CohortDTO> tagHelper) {
        this.tagHelper = tagHelper;
    }

    @PostConstruct
    public void init() {
        createValidator();
    }

    @Override
    protected List<ValidatorGroupBuilder<CohortDTO, ?>> getGroupBuilder() {

        return Arrays.asList(
                tagHelper.prepareTagBuilder()
        );
    }
}
