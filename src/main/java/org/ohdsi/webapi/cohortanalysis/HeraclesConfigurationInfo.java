package org.ohdsi.webapi.cohortanalysis;

import org.ohdsi.info.ConfigurationInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class HeraclesConfigurationInfo extends ConfigurationInfo {

    private static final String KEY = "heracles";

    public HeraclesConfigurationInfo(@Value("${heracles.smallcellcount}") String smallCellCount) {

        properties.put("smallCellCount", smallCellCount);
    }

    @Override
    public String getKey() {

        return KEY;
    }
}
