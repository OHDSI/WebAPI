package org.ohdsi.webapi.prediction.specification;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.ohdsi.analysis.prediction.design.SeedSettings;

/**
 *
 * @author asena5
 */
public class SeedSettingsImpl extends ModelSettingsImpl implements SeedSettings {

    /**
     *
     */
    protected Integer seed = null;

    /**
     *
     * @return
     */
    @JsonProperty("seed")
    @Override
    public Integer getSeed() {
        return seed;
    }

    /**
     *
     * @param seed
     */
    public void setSeed(Integer seed) {
        this.seed = seed;
    }
}
