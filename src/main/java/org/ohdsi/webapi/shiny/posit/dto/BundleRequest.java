package org.ohdsi.webapi.shiny.posit.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BundleRequest {
    @JsonProperty("bundle_id")
    private String bundleId;

    public String getBundleId() {
        return bundleId;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }
}
