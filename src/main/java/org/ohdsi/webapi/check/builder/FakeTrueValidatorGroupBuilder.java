package org.ohdsi.webapi.check.builder;

public class FakeTrueValidatorGroupBuilder<T, V> extends ValidatorGroupBuilder<T, V> {
    public FakeTrueValidatorGroupBuilder() {
        this.validatorBuilders.add(new FakeTrueValidatorBuilder<>());
    }
}
