package org.ohdsi.webapi.check.builder;

import com.google.common.reflect.TypeToken;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.Validator;
import org.ohdsi.webapi.check.validator.ValidatorGroup;

public class ValidatorGroupBuilder<T, V> extends ValidatorBaseBuilder<T, ValidatorGroup<T, V>, ValidatorGroupBuilder<T, V>> {

    protected List<ValidatorBuilder<V>> validatorBuilders = new ArrayList<>();
    protected List<ValidatorGroupBuilder<V, ?>> validatorGroupBuilders = new ArrayList<>();
    protected Function<T, V> valueGetter;

    public ValidatorGroupBuilder<T, V> valueGetter(Function<T, V> valueGetter) {

        this.valueGetter = valueGetter;
        return this;
    }


    public ValidatorGroupBuilder<T, V> validators(List<ValidatorBuilder<V>> validators) {

        this.validatorBuilders.addAll(validators);
        return this;
    }

    public ValidatorGroupBuilder<T, V> groups(List<ValidatorGroupBuilder<V, ?>> groups) {

        this.validatorGroupBuilders.addAll(groups);
        return this;
    }

    @SafeVarargs
    public final ValidatorGroupBuilder<T, V> validators(ValidatorBuilder<V>... validators) {

        this.validatorBuilders.addAll(Arrays.asList(validators));
        return this;
    }

    @SafeVarargs
    public final ValidatorGroupBuilder<T, V> groups(ValidatorGroupBuilder<V, ?>... groups) {

        this.validatorGroupBuilders.addAll(Arrays.asList(groups));
        return this;
    }

    protected Path createChildPath() {

        return Path.createPath(this.basePath, this.attrName);
    }

    public ValidatorGroup<T, V> build() {

        List<ValidatorGroup<V, ?>> groups = initAndBuildList(this.validatorGroupBuilders);
        List<Validator<V>> validators = initAndBuildList(this.validatorBuilders);

        return new ValidatorGroup<>(validators, groups, valueGetter);
    }

    private <U> List<U> initAndBuildList(List<? extends ValidatorBaseBuilder<V, U, ?>> builders) {

        builders.forEach(builder -> {
            if (Objects.nonNull(this.errorMessage)) {
                builder.errorMessage(this.errorMessage);
            }
            if (Objects.isNull(builder.getBasePath())) {
                builder.basePath(createChildPath());
            }
        });
        return builders.stream()
                .map(ValidatorBaseBuilder::build)
                .collect(Collectors.toList());


    }

}
