package org.ohdsi.webapi.tag.domain;

import java.util.Arrays;
import java.util.Optional;

public enum TagAssetType {
    CONCEPT_SET("concept_set"),
    COHORT("cohort"),
    COHORT_CHARACTERIZATION("cohort_characterization"),
    INCIDENT_RATE("incident_rate"),
    PATHWAY("pathway");

    private final String name;

    TagAssetType(String name) {
        this.name = name;
    }

    public static TagAssetType fromName(String name) {
        return Arrays.stream(values())
                .filter(t -> t.name.equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Asset type (%s) cannot be found", name)));
    }
}
