package org.ohdsi.webapi.vocabulary;

import java.util.HashSet;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.params.CoreAdminParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SolrSearchClient {
    @Value("${solr.endpoint}")
    private String solrEndpoint;
    
    public boolean enabled() {
        return !StringUtils.isEmpty(solrEndpoint);
    }
    
    public SolrClient getSolrClient(String coreName) {
        return new HttpSolrClient.Builder(solrEndpoint + "/" + coreName).build();
    }
    
    public HashSet<String> getCores() throws Exception {
        HashSet<String> returnVal = new HashSet<>();
        SolrClient client = this.getSolrClient("");
        CoreAdminRequest request = new CoreAdminRequest();
        request.setAction(CoreAdminParams.CoreAdminAction.STATUS);
        CoreAdminResponse cores;

        try {
            cores = request.process(client);
            for (int i = 0; i < cores.getCoreStatus().size(); i++) {
                returnVal.add(cores.getCoreStatus().getName(i));
            }
        } catch (Exception ex) {
          throw ex;
        }
        return returnVal;
    }
    
}
