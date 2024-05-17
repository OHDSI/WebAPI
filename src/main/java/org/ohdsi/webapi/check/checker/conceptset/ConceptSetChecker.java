package org.ohdsi.webapi.check.checker.conceptset;

import jakarta.annotation.PostConstruct;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.checker.BaseChecker;
import org.ohdsi.webapi.check.checker.tag.helper.TagHelper;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ConceptSetChecker extends BaseChecker<ConceptSetDTO> {
    private final TagHelper<ConceptSetDTO> tagHelper;

    public ConceptSetChecker(TagHelper<ConceptSetDTO> tagHelper) {
        this.tagHelper = tagHelper;
    }

    @PostConstruct
    public void init() {
        createValidator();
    }

    @Override
    protected List<ValidatorGroupBuilder<ConceptSetDTO, ?>> getGroupBuilder() {

        return Arrays.asList(
                tagHelper.prepareTagBuilder()
        );
    }
}
