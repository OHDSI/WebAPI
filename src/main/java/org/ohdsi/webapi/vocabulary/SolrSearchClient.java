package org.ohdsi.webapi.vocabulary;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.params.CoreAdminParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SolrSearchClient {
    @Value("${solr.endpoint}")
    private String solrEndpoint;
    
    @Value("${solr.query.prefix}")
    private String solrQueryPrefix;
    
    public static final List<String> SOLR_ESCAPE_CHARACTERS = Arrays.asList("(", ")", "{", "}", "[", "]", "^", "\"", ":");
    
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
    
    public String formatSearchQuery(String query) {
        return formatSearchQuery(query, true);
    }
    
    public String formatSearchQuery(String query, Boolean useWildcardSearch) {
        String returnVal;
        if (useWildcardSearch) {
            returnVal = solrQueryPrefix + "query:\"*" + ClientUtils.escapeQueryChars(query) + "*\"";
        } else {
            returnVal = "query:" + escapeNonWildcardQuery(query);
        }
        System.out.println(returnVal);
        return returnVal;
    }
    
    // This escape function is used when building the non wildcard
    // query since the ClientUtils.escapeQueryChars will replace 
    // add an extra "\" to spaces which can change the query results.
    // So, here we escape a subset of the special characters for
    // this edge case
    public String escapeNonWildcardQuery(String query) {
        for (String item : SOLR_ESCAPE_CHARACTERS) {
            query = query.replace(item, "\\" + item);
        }
        return query;
    }
}
