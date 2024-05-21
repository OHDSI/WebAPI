package org.ohdsi.webapi.check.builder.tag;

import org.ohdsi.webapi.check.builder.ValidatorBuilder;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.tag.MandatoryTagValidator;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.dto.TagDTO;

import java.util.Collection;

public class MandatoryTagValidatorBuilder<T extends Collection<? extends TagDTO>> extends ValidatorBuilder<T> {
    private final TagService tagService;

    public MandatoryTagValidatorBuilder(TagService tagService) {
        this.tagService = tagService;
    }

    @Override
    public Validator<T> build() {
        return new MandatoryTagValidator<>(createChildPath(), severity, errorMessage, this.tagService);
    }
}
