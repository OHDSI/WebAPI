package org.ohdsi.webapi.vocabulary;

import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.ohdsi.webapi.info.ConfigurationInfo;
import org.ohdsi.webapi.util.SolrUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VocabularyConfigurationInfo extends ConfigurationInfo {

    private static final String KEY = "vocabulary";

    public VocabularyConfigurationInfo(@Value("${solr.endpoint}") String solrEndpoint) {
        properties.put("solrEnabled", !StringUtils.isEmpty(solrEndpoint));
        if (!StringUtils.isEmpty(solrEndpoint)) {
            try {
                List<String> cores = SolrUtils.getCores(solrEndpoint).stream().collect(Collectors.toList());
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
