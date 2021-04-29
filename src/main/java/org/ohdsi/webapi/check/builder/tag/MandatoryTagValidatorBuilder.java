package org.ohdsi.webapi.check.builder.tag;

import org.ohdsi.webapi.check.builder.ValidatorBuilder;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.common.NotNullNotEmptyValidator;
import org.ohdsi.webapi.check.validator.tag.MandatoryTagValidator;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MandatoryTagValidatorBuilder<T extends Collection<? extends TagDTO>> extends ValidatorBuilder<T> {
    private final TagRepository tagRepository;
    private final List<String> groups;

    public MandatoryTagValidatorBuilder(TagRepository tagRepository, String[] groupArray) {
        this.tagRepository = tagRepository;
        groups = Arrays.asList(groupArray);
    }

    @Override
    public Validator<T> build() {
        return new MandatoryTagValidator<>(createChildPath(), severity, errorMessage, this.tagRepository, this.groups);
    }
}
