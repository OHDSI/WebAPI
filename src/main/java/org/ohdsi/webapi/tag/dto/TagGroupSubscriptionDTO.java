package org.ohdsi.webapi.tag.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TagGroupSubscriptionDTO {
    @JsonProperty
    private Set<Integer> tags = new HashSet<>();

    @JsonProperty
    private AssetGroup assets;

    public Set<Integer> getTags() {
        return tags;
    }

    public void setTags(Set<Integer> tags) {
        this.tags = tags;
    }

    public AssetGroup getAssets() {
        return assets;
    }

    public void setAssets(AssetGroup assets) {
        this.assets = assets;
    }
}
