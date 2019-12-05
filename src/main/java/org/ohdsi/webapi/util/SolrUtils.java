package org.ohdsi.webapi.util;

import java.util.HashSet;
import java.util.Iterator;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class SolrUtils {
    
    public static HashSet<String> getCores(String solrEndpoint) throws Exception {
      RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
      String searchEndpoint = solrEndpoint + "/admin/cores";
      HashSet<String> returnVal = new HashSet<>();

      try {
        ResponseEntity<String> responseJson = restTemplate.getForEntity(searchEndpoint, String.class);
        JSONObject responseObject = new JSONObject(responseJson.getBody());
        JSONObject statusObject = responseObject.getJSONObject("status");
        Iterator<String> keys = statusObject.keys();

        while(keys.hasNext()) {
          String key = keys.next();
          returnVal.add(key);
        }
        return returnVal;
      } catch (Exception ex) {
        throw ex;
      }
    }
}
