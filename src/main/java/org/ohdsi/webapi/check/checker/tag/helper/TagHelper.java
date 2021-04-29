package org.ohdsi.webapi.check.checker.tag.helper;

import org.ohdsi.analysis.estimation.comparativecohortanalysis.design.CohortMethodAnalysis;
import org.ohdsi.webapi.check.builder.IterableForEachValidatorBuilder;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.builder.tag.MandatoryTagValidatorBuilder;
import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

@Component
public class TagHelper {
    @Autowired
    private TagRepository tagRepository;

//    @Value("#{'${tag.mandatoryGroups}'.split(',')}")
    private String[] groups;

    @Value("${tag.enabled}")
    private boolean enabled;

    public ValidatorGroupBuilder<? extends CommonEntityExtDTO, Collection<? extends TagDTO>> prepareTagBuilder() {
        ValidatorGroupBuilder<CommonEntityExtDTO, Collection<? extends TagDTO>> builder =
                new ValidatorGroupBuilder<CommonEntityExtDTO, Collection<? extends TagDTO>>()
                        .attrName("tag")
                        .conditionGetter(t -> enabled)
                        .valueGetter(CommonEntityExtDTO::getTags)
                        .validators(
                                new MandatoryTagValidatorBuilder<>(tagRepository, groups)
                        );
        return builder;
    }
}
