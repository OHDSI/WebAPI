package org.ohdsi.webapi.check.checker;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.ohdsi.webapi.check.Checker;
import org.ohdsi.webapi.check.builder.ValidatorGroupBuilder;
import org.ohdsi.webapi.check.builder.ValidatorBuilder;
import org.ohdsi.webapi.check.validator.Context;
import org.ohdsi.webapi.check.validator.Path;
import org.ohdsi.webapi.check.validator.ValidatorGroup;
import org.ohdsi.webapi.check.warning.DefaultWarning;
import org.ohdsi.webapi.check.warning.Warning;

public abstract class BaseChecker<T> implements Checker<T> {

    private ValidatorGroup<T, T> validator;

    public final List<Warning> check(T value) {

        Context context = new Context();
        validator.validate(value, context);

        return getWarnings(context);
    }

    protected void createValidator() {
            validator = new ValidatorGroupBuilder<T, T>()
                    .basePath(Path.createPath())
                    .valueGetter(Function.identity())
                    .validators(getValidatorBuilders())
                    .groups(getGroupBuilder())
                    .build();
    }


    public List<Warning> getWarnings(Context context) {

        return context.getWarnings().stream()
                .map(warning -> new DefaultWarning(
                        warning.getSeverity(),
                        String.format("%s - %s", warning.getPath().getPath(), warning.getMessage()))
                )
                .collect(Collectors.toList());
    }

    protected List<ValidatorBuilder<T>> getValidatorBuilders() {

        return Collections.emptyList();
    }

    protected List<ValidatorGroupBuilder<T, ?>> getGroupBuilder() {

        return Collections.emptyList();
    }

}
