package org.ohdsi.webapi.check.builder;

import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.ValidatorGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class AbstractForEachValidatorBuilder<T, V> extends ValidatorBuilder<V> {

    private List<ValidatorBuilder<T>> validatorBuilders = new ArrayList<>();
    private List<ValidatorGroupBuilder<T, ?>> validatorGroupBuilders = new ArrayList<>();

    public AbstractForEachValidatorBuilder<T, V> validators(List<ValidatorBuilder<T>> validators) {

        this.validatorBuilders.addAll(validators);
        return this;
    }

    public AbstractForEachValidatorBuilder<T, V> groups(List<ValidatorGroupBuilder<T, ?>> groups) {

        this.validatorGroupBuilders.addAll(groups);
        return this;
    }

    @SafeVarargs
    public final AbstractForEachValidatorBuilder<T, V> validators(ValidatorBuilder<T>... validators) {

        this.validatorBuilders.addAll(Arrays.asList(validators));
        return this;
    }

    @SafeVarargs
    public final AbstractForEachValidatorBuilder<T, V> groups(ValidatorGroupBuilder<T, ?>... groups) {

        this.validatorGroupBuilders.addAll(Arrays.asList(groups));
        return this;
    }

    protected List<ValidatorGroup<T, ?>> initGroups() {
        return initAndBuildList(this.validatorGroupBuilders);
    }

    protected List<Validator<T>> initValidators() {
        return initAndBuildList(this.validatorBuilders);
    }

    private <U> List<U> initAndBuildList(List<? extends ValidatorBaseBuilder<T, U, ?>> builders) {

        builders.forEach(builder -> {
            if (Objects.isNull(builder.getBasePath())) {
                builder.basePath(createChildPath());
            }
            if (Objects.isNull(builder.getErrorMessage())) {
                builder.errorMessage(this.errorMessage);
            }
            if (Objects.isNull(builder.getSeverity())) {
                builder.severity(this.severity);
            }
            if (Objects.isNull(builder.getAttrName())) {
                builder.attrName(this.attrName);
            }
        });
        return builders.stream()
                .map(ValidatorBaseBuilder::build)
                .collect(Collectors.toList());


    }
}
