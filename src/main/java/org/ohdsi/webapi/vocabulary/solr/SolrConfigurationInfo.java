package org.ohdsi.webapi.vocabulary.solr;

import org.ohdsi.webapi.info.ConfigurationInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SolrConfigurationInfo extends ConfigurationInfo {
    private static final String KEY = "vocabulary";
    
    @Autowired
    public SolrConfigurationInfo(SolrSearchClient solrSearchClient) {
        properties.put("solrEnabled", true);
        if (solrSearchClient.enabled()) {
            try {
                List<String> cores = new ArrayList<>(solrSearchClient.getCores());
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
