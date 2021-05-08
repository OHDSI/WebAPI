package org.ohdsi.webapi.check.validator.tag;

import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningSeverity;
import org.ohdsi.webapi.tag.TagService;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MandatoryTagValidator<T extends Collection<? extends TagDTO>> extends Validator<T> {
    private static final String INVALID = "no assigned mandatory tags";

    private final TagService tagService;

    public MandatoryTagValidator(Path path, WarningSeverity severity, String errorMessage,
                                 TagService tagService) {
        super(path, severity, errorMessage);
        this.tagService = tagService;
    }

    @Override
    public boolean validate(T value, Context context) {
        boolean isValid = true;
        if (Objects.nonNull(value)) {
            Set<Integer> groupIds = new HashSet<>();
            value.forEach(tagDTO -> {
                Set<Integer> ids = tagService.getAllGroupsForTag(tagDTO.getId());
                groupIds.addAll(ids);
            });
            List<Tag> mandatoryTags = tagService.findMandatoryTags();
            long count = mandatoryTags.stream()
                    .filter(t -> !groupIds.contains(t.getId()))
                    .count();

            isValid = count == 0;
        } else {
            isValid = tagService.findMandatoryTags().size() == 0;
        }
        if (!isValid) {
            context.addWarning(getSeverity(), getErrorMessage(value), path);
        }
        return isValid;
    }

    protected String getDefaultErrorMessage() {
        return INVALID;
    }
}
