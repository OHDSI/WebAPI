package org.ohdsi.webapi.tag.domain;

import java.util.Arrays;

public enum TagType {
    SYSTEM(0), CUSTOM(1), PRIZM(2);

    private final int value;

    TagType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static TagType fromValue(int value) {
        return Arrays.stream(values())
                .filter(t -> t.value == value)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Tag type (%s) cannot be found", value)));
    }
}
