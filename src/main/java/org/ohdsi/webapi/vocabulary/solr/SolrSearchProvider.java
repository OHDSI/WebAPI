package org.ohdsi.webapi.vocabulary.solr;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.BaseHttpSolrClient.RemoteSolrException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.ohdsi.webapi.vocabulary.Concept;
import org.ohdsi.webapi.vocabulary.SearchProviderConfig;
import org.ohdsi.webapi.extcommon.vocabulary.SearchProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

@Component
public class SolrSearchProvider implements SearchProvider {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    private static final int SOLR_PRIORITY = 1000;
    private static HashSet<String> solrCores = new HashSet<>();

    @Autowired
    SolrSearchClient solrSearchClient;

    @PostConstruct
    protected void init() {
        try {
            solrCores = solrSearchClient.getCores();
        } catch (Exception ex) {
            log.error("SOLR Core Initialization Error:  WebAPI was unable to obtain the list of available cores.", ex);
        }
    }

    @Override
    public boolean supports(String vocabularyVersionKey) {
        return solrCores.contains(vocabularyVersionKey);
    }

    @Override
    public int getPriority() {
        return SOLR_PRIORITY;
    }

    @Override
    public Collection<Concept> executeSearch(SearchProviderConfig config, String query, String rows) throws IOException, SolrServerException {
        ArrayList<Concept> concepts = new ArrayList<>();
        SolrClient client = solrSearchClient.getSolrClient(config.getVersionKey());

        SolrQuery q = new SolrQuery();
        SolrDocumentList results = new SolrDocumentList();
        QueryResponse response;
        q.setStart(0);
        q.setRows(Integer.parseInt(rows));
        boolean solrSearchError = false;
        try {
            q.setQuery(solrSearchClient.formatSearchQuery(query));
            response = client.query(q);
            results = response.getResults();
        } catch (RemoteSolrException rse) {
            // In this case, the default wildcard search did not work
            // properly. Log this error and try an alternative approach.
            log.error("SOLR Search Query: \"" + query + "\" failed with message: " + rse.getMessage());
            solrSearchError = true;
        }

        // If we did not receive results from issuing the initial wildcard
        // query OR there was an exception usually due to a maxBooleanClause 
        // violation from doing a wildcard search on a very common term, then 
        // we will make another attempt using the standard query approach
        if (results.isEmpty() || solrSearchError) {
            q.setQuery(solrSearchClient.formatSearchQuery(query, Boolean.FALSE));
            response = client.query(q);
            results = response.getResults();
        }

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
            c.validStartDate = convertObjectToDate(d.getFieldValue("valid_start_date"));
            c.validEndDate = convertObjectToDate(d.getFieldValue("valid_end_date"));
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

    protected Date convertObjectToDate(Object obj) {
        try {
            return DateUtils.parseDate(ConvertUtils.convert(obj), DateFormatUtils.ISO_8601_EXTENDED_DATE_FORMAT.getPattern());
        } catch (final Exception e) {
            return null;
        }
    }
}
