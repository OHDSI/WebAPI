package org.ohdsi.webapi.vocabulary;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.ohdsi.circe.vocabulary.Concept;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SolrSearchProvider implements SearchProvider {
    @Value("${solr.endpoint}")
    private String solrEndpoint;
    
    @Override
    public boolean supports(VocabularySearchProviderType type) {
        return Objects.equals(type, VocabularySearchProviderType.SOLR);
    }
    
    @Override
    public Collection<Concept> executeSearch(SearchProviderConfig config, String query, String rows) throws Exception {
      System.out.println("SolrSearchProvider");
      ArrayList<Concept> concepts = new ArrayList<>();
      RestTemplate restTemplate = new RestTemplate();
      String searchEndpoint = solrEndpoint + "/" + config.getVersionKey() + "/select?q=query:" + query + "&wt=json&rows=" + rows;
      ResponseEntity<String> responseJson = restTemplate.getForEntity(searchEndpoint, String.class);
      try {
        JSONObject jsonObject = new JSONObject(responseJson.getBody());
        JSONObject responseNode = jsonObject.getJSONObject("response");
        JSONArray docs = responseNode.getJSONArray("docs");
        for (int i=0; i<docs.length(); i++) {
          Concept c = new Concept();
          JSONObject conceptNode = docs.getJSONObject(i);
          c.conceptName = conceptNode.getString("concept_name");
          c.conceptId = conceptNode.getLong("id");
          try {
            c.conceptClassId = conceptNode.getString("concept_class_id");
          } catch (JSONException ex) {
            c.conceptClassId = "";
          }
          try {
            c.conceptCode = conceptNode.getString("concept_code");
          } catch (JSONException ex) {
            c.conceptCode = "";
          }
          c.domainId = conceptNode.getString("domain_id");
          try {
            c.invalidReason = conceptNode.getString("invalid_reason");
          } catch (JSONException ex) {
            c.invalidReason = "V";
          }
          try {
            c.standardConcept = conceptNode.getString("standard_concept");
          } catch (JSONException ex) {
            c.standardConcept = "N";
          }
          c.vocabularyId = conceptNode.getString("vocabulary_id");
          concepts.add(c);
        }
      } catch (JSONException jsonException) {
        throw jsonException;
      }

      return concepts;    
    }
}
