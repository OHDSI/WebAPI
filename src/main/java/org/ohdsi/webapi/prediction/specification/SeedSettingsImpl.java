package org.ohdsi.webapi.prediction.specification;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.prediction.design.SeedSettings;

public abstract class SeedSettingsImpl implements SeedSettings {
    protected Float seed = null;

    @JsonProperty("seed")
    @Override
    public Float getSeed() {
        return seed;
    }

    public void setSeed(Float seed) {
        this.seed = seed;
    }
}
