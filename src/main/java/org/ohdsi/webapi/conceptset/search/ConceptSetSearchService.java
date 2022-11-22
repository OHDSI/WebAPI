package org.ohdsi.webapi.conceptset.search;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.common.SolrInputDocument;
import org.ohdsi.webapi.conceptset.ConceptSet;
import org.ohdsi.webapi.service.dto.ConceptSetSearchDTO;
import org.ohdsi.webapi.vocabulary.SolrSearchClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
public class ConceptSetSearchService {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @Value("${solr.conceptsets.core}")
    private String conceptSetsCore;

    @Autowired
    private SolrSearchClient solrSearchClient;

    public boolean isSearchAvailable() {
        try {
            return solrSearchClient.enabled() && solrSearchClient.getCores().contains(conceptSetsCore);
        } catch (final Exception e) {
            log.error("SOLR error: Concept sets search availability check failed", e);
            return false;
        }
    }

    public Set<Integer> searchConceptSets(final ConceptSetSearchDTO dto) {
        final SolrClient solrClient = solrSearchClient.getSolrClient(conceptSetsCore);

        final Set<Integer> results = new HashSet<>();

        try {
            final SolrQuery q = new SolrQuery();
            q.setQuery(composeSearchQuery(dto));
            q.add("group", "true");
            q.add("group.field", "concept_set_id");

            solrClient.query(q).getGroupResponse().getValues().forEach(gr -> {
                gr.getValues().forEach(g -> {
                    results.add(NumberUtils.createInteger(ConvertUtils.convert(g.getGroupValue())));
                });
            });

        } catch (final Exception e) {
            log.error("SOLR Search Query: {} failed with message: {}", dto.getQuery(), e.getMessage());
        }

        return results;
    }

    public void indexConceptSetsFull(final Collection<ConceptSetSearchDocument> documents) {
        try {
            final SolrClient solrClient = solrSearchClient.getSolrClient(conceptSetsCore);

            // delete all before index
            solrClient.deleteByQuery("*:*");

            documents.forEach(d -> addDocumentToIndex(solrClient, d));
            solrClient.commit();

        } catch (final Exception e) {
            log.error("SOLR error: concept sets index failed with message: {}", e.getMessage());
        }
    }

    public void reindexConceptSet(final Integer conceptSetId, final Collection<ConceptSetSearchDocument> documents) {
        try {
            final SolrClient solrClient = solrSearchClient.getSolrClient(conceptSetsCore);

            solrClient.deleteByQuery("concept_set_id:" + conceptSetId);
            documents.forEach(d -> addDocumentToIndex(solrClient, d));
            solrClient.commit();

        } catch (final Exception e) {
            log.error("SOLR error: concept set {} index failed with message: {}", conceptSetId, e.getMessage());
        }
    }

    public void deleteConceptSetIndex(final Integer conceptSetId) {
        try {
            final SolrClient solrClient = solrSearchClient.getSolrClient(conceptSetsCore);

            solrClient.deleteByQuery("concept_set_id:" + conceptSetId);
            solrClient.commit();

        } catch (final Exception e) {
            log.error("SOLR error: concept set {} index failed with message: {}", conceptSetId, e.getMessage());
        }
    }

    private String composeSearchQuery(final ConceptSetSearchDTO dto) {
        String searchQuery = solrSearchClient.formatSearchQuery(dto.getQuery().trim());

        if (dto.getDomainId() != null && dto.getDomainId().length > 0) {
            searchQuery += " AND domain_name:(" + String.join(" OR ", dto.getDomainId()) + ")";
        }

        return searchQuery;
    }

    private void addDocumentToIndex(final SolrClient solrClient, final ConceptSetSearchDocument searchDocument) {
        try {
            final SolrInputDocument document = new SolrInputDocument();
            document.addField("concept_set_id", searchDocument.getConceptSetId());
            document.addField("concept_id", searchDocument.getConceptId());
            document.addField("concept_name", searchDocument.getConceptName());
            document.addField("concept_code", searchDocument.getConceptCode());
            document.addField("domain_name", searchDocument.getDomainName());
            solrClient.add(document);
        } catch (final Exception e) {
            log.error("SOLR error: cannot add document to index: {}", e.getMessage());
        }
    }
}