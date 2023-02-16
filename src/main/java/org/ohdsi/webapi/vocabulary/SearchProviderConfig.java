package org.ohdsi.webapi.vocabulary;

public class SearchProviderConfig {
    private String sourceKey;
    private String versionKey;
    
    public SearchProviderConfig(String sourceKey, String versionKey) {
        this.sourceKey = sourceKey;
        this.versionKey = versionKey;
    }
    
    public String getVersionKey() {
        return versionKey;
    }
    
    public String getSourceKey() {
        return sourceKey;
    }
}
