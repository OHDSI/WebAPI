package org.ohdsi.webapi.vocabulary;

import java.util.List;
import java.util.stream.Collectors;
import org.ohdsi.webapi.info.ConfigurationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class VocabularyConfigurationInfo extends ConfigurationInfo {

    private static final String KEY = "vocabulary";
    
    @Autowired
    public VocabularyConfigurationInfo(SolrSearchClient solrSearchClient) {
        properties.put("solrEnabled", solrSearchClient.enabled());
        if (solrSearchClient.enabled()) {
            try {
                List<String> cores = solrSearchClient.getCores().stream().collect(Collectors.toList());
                properties.put("cores", cores);
            } catch (Exception e) {
                properties.put("cores", "unable to retrieve from endpoint.");
            }
        }
    }

    @Override
    public String getKey() {

        return KEY;
    }
}
