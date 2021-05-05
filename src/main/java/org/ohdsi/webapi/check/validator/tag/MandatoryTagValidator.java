package org.ohdsi.webapi.check.validator.tag;

import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.warning.WarningSeverity;
import org.ohdsi.webapi.tag.domain.Tag;
import org.ohdsi.webapi.tag.dto.TagDTO;
import org.ohdsi.webapi.tag.repository.TagRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MandatoryTagValidator<T extends Collection<? extends TagDTO>> extends Validator<T> {
    private static final String NULL_OR_EMPTY = "null or empty";

    private final TagRepository tagRepository;

    private final List<String> groups;

    public MandatoryTagValidator(Path path, WarningSeverity severity, String errorMessage,
                                 TagRepository tagRepository, List<String> groups) {
        super(path, severity, errorMessage);
        this.tagRepository = tagRepository;
        this.groups = groups;
    }

    @Override
    public boolean validate(T value, Context context) {
        Set<String> existingGroups = new HashSet<>();
        value.forEach(tagDTO -> {
            Tag tag = tagRepository.findOne(tagDTO.getId());
            findParentGroup(tag.getGroups(), existingGroups);
        });
        long count = groups.stream()
                .filter(g -> !existingGroups.contains(g))
                .count();

        return count == 0;
    }

    protected String getDefaultErrorMessage() {
        return NULL_OR_EMPTY;
    }

    private void findParentGroup(Set<Tag> groups, Set<String> groupNames) {
        groups.forEach(g -> {
            groupNames.add(g.getName());
            findParentGroup(g.getGroups(), groupNames);
        });
    }
}
