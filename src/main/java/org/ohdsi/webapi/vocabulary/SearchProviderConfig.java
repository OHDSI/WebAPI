package org.ohdsi.webapi.vocabulary;

import org.ohdsi.webapi.source.Source;

public class SearchProviderConfig {
    protected Source source;
    protected VocabularyInfo vocabularyInfo;
    protected String versionKey;
    
    public SearchProviderConfig(Source source, VocabularyInfo vocabularyInfo) {
        this.source = source;
        this.vocabularyInfo = vocabularyInfo;
        this.versionKey = vocabularyInfo.version.replace(' ', '_');
    }
    
    public String getVersionKey() {
        return versionKey;
    }
    
    public Source getSource() {
        return source;
    }
}
