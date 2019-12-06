package org.ohdsi.webapi.vocabulary;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ohdsi.circe.vocabulary.Concept;
import org.springframework.stereotype.Component;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class SolrSearchProvider implements SearchProvider {
    @Autowired
    SolrSearchClient solrSearchClient;
    
    @Override
    public boolean supports(VocabularySearchProviderType type) {
        return Objects.equals(type, VocabularySearchProviderType.SOLR);
    }
    
    @Override
    public Collection<Concept> executeSearch(SearchProviderConfig config, String query, String rows) throws IOException, SolrServerException {
        ArrayList<Concept> concepts = new ArrayList<>();
        SolrClient client = solrSearchClient.getSolrClient(config.getVersionKey());

        SolrQuery q = new SolrQuery();
        q.setQuery("query:" + query);
        q.setStart(0);
        q.setRows(Integer.parseInt(rows));

        QueryResponse response = client.query(q);
        SolrDocumentList results = response.getResults();
        for (int i = 0; i < results.size(); ++i) {
            SolrDocument d = results.get(i);
            Concept c = new Concept();
            c.conceptName = convertObjectToString(d.getFieldValue("concept_name"));
            c.conceptId = convertObjectToLong(d.getFieldValue("id"));
            c.conceptClassId = convertObjectToString(d.getFieldValue("concept_class_id"), "");
            c.conceptCode = convertObjectToString(d.getFieldValue("concept_code"), "");
            c.domainId = ConvertUtils.convert(d.getFieldValue("domain_id"));
            c.invalidReason = convertObjectToString(d.getFieldValue("invalid_reason"), "V");
            c.standardConcept = convertObjectToString(d.getFieldValue("standard_concept"), "N");
            c.vocabularyId = ConvertUtils.convert(d.getFieldValue("vocabulary_id"));
            concepts.add(c);
        }        
        
        return concepts;
    }
    
    protected String convertObjectToString(Object obj) {
        return convertObjectToString(obj, null);
    }
    
    protected String convertObjectToString(Object obj, String defaultValue) {
        String returnVal = ConvertUtils.convert(obj);
        if (defaultValue != null && returnVal == null) {
            returnVal = defaultValue;
        }
        return returnVal;
    }
    
    protected Long convertObjectToLong(Object obj) {
        return NumberUtils.createLong(ConvertUtils.convert(obj));
    }
}
