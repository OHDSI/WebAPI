package org.ohdsi.webapi.check.checker.tag.helper;

import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.builder.tag.MandatoryTagValidatorBuilder;
import org.ohdsi.webapi.check.warning.WarningSeverity;
import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
public class TagHelper<T extends CommonEntityExtDTO> {
    private final TagService tagService;

    @Value("${tag.enabled}")
    private boolean enabled;

    public TagHelper(TagService tagService) {
        this.tagService = tagService;
    }

    public ValidatorGroupBuilder<T, Collection<? extends TagDTO>> prepareTagBuilder() {
        ValidatorGroupBuilder<T, Collection<? extends TagDTO>> builder =
                new ValidatorGroupBuilder<T, Collection<? extends TagDTO>>()
                        .attrName("Tags")
                        .conditionGetter(t -> enabled)
                        .severity(WarningSeverity.WARNING)
                        .valueGetter(T::getTags)
                        .validators(
                                new MandatoryTagValidatorBuilder<>(tagService)
                        );
        return builder;
    }
}
