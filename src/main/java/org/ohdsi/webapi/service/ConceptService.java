/**
 * The contents of this file are subject to the Regenstrief Public License
 * Version 1.0 (the "License"); you may not use this file except in compliance with the License.
 * Please contact Regenstrief Institute if you would like to obtain a copy of the license.
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) Regenstrief Institute.  All Rights Reserved.
 */
package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.vocabulary.Concept;
import org.ohdsi.webapi.vocabulary.ConceptRelationship;
import org.ohdsi.webapi.vocabulary.ConceptSearch;
import org.ohdsi.webapi.vocabulary.RelatedConcept;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
@Path("/concept/")
public class ConceptService extends AbstractDAOService {
    
    private final RowMapper<Concept> rowMapper = new RowMapper<Concept>() {
        
        @Override
        public Concept mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
            final Concept concept = new Concept();
            concept.conceptId = resultSet.getLong("CONCEPT_ID");
            concept.conceptCode = resultSet.getString("CONCEPT_CODE");
            concept.conceptName = resultSet.getString("CONCEPT_NAME");
            concept.standardConcept = resultSet.getString("STANDARD_CONCEPT");
            concept.invalidReason = resultSet.getString("INVALID_REASON");
            concept.conceptClassId = resultSet.getString("CONCEPT_CLASS_ID");
            concept.vocabularyId = resultSet.getString("VOCABULARY_ID");
            concept.domainId = resultSet.getString("DOMAIN_ID");
            return concept;
        }
    };
    
    /**
     * @param query
     * @return
     */
    @Path("search/{query}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Concept> executeSearch(@PathParam("query") String query) {
        // escape single quote for queries
        query = query.replace("'", "''");
        
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/search.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "query", "CDM_schema", "filters" }, new String[] {
                query, getCdmSchema(), "" });
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
        
        return getJdbcTemplate().query(sql_statement, this.rowMapper);
    }
    
    /**
     * @param id
     * @return
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Concept getConcept(@PathParam("id") final long id) {
        
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getConcept.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "id", "CDM_schema" },
            new String[] { String.valueOf(id), getCdmSchema() });
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
        
        return getJdbcTemplate().queryForObject(sql_statement, this.rowMapper);
    }
    
    private void addRelationships(final Map<Long, RelatedConcept> concepts, final ResultSet resultSet) throws SQLException {
        final Long concept_id = resultSet.getLong("CONCEPT_ID");
        if (!concepts.containsKey(concept_id)) {
            final RelatedConcept concept = new RelatedConcept();
            concept.conceptId = concept_id;
            concept.conceptCode = resultSet.getString("CONCEPT_CODE");
            concept.conceptName = resultSet.getString("CONCEPT_NAME");
            concept.standardConcept = resultSet.getString("STANDARD_CONCEPT");
            concept.invalidReason = resultSet.getString("INVALID_REASON");
            concept.vocabularyId = resultSet.getString("VOCABULARY_ID");
            concept.conceptClassId = resultSet.getString("CONCEPT_CLASS_ID");
            concept.domainId = resultSet.getString("DOMAIN_ID");
            
            final ConceptRelationship relationship = new ConceptRelationship();
            relationship.relationshipName = resultSet.getString("RELATIONSHIP_NAME");
            relationship.relationshipDistance = resultSet.getInt("RELATIONSHIP_DISTANCE");
            concept.relationships.add(relationship);
            
            concepts.put(concept_id, concept);
        } else {
            final ConceptRelationship relationship = new ConceptRelationship();
            relationship.relationshipName = resultSet.getString("RELATIONSHIP_NAME");
            relationship.relationshipDistance = resultSet.getInt("RELATIONSHIP_DISTANCE");
            concepts.get(concept_id).relationships.add(relationship);
        }
    }
    
    @Path("search")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Concept> executeSearch(ConceptSearch search) {
        // escape single quote for queries
        search.query = search.query.replace("'", "''");
        
        String filters = "";
        if (search.domainId != null) {
            filters += " AND DOMAIN_ID IN (" + JoinArray(search.domainId) + ")";
        }
        
        if (search.vocabularyId != null) {
            filters += " AND VOCABULARY_ID IN (" + JoinArray(search.vocabularyId) + ")";
        }
        
        if (search.conceptClassId != null) {
            filters += " AND CONCEPT_CLASS_ID IN (" + JoinArray(search.conceptClassId) + ")";
        }
        
        if (search.invalidReason != null) {
            if (search.invalidReason.equals("V")) {
                filters += " AND INVALID_REASON IS NULL ";
            } else {
                filters += " AND INVALID_REASON = '" + search.invalidReason + "' ";
            }
        }
        
        if (search.standardConcept != null) {
            if (search.standardConcept.equals("N")) {
                filters += " AND STANDARD_CONCEPT IS NULL ";
            } else {
                filters += " AND STANDARD_CONCEPT = '" + search.standardConcept + "'";
            }
        }
        
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/search.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "query", "CDM_schema", "filters" }, new String[] {
                search.query, getCdmSchema(), filters });
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
        
        return getJdbcTemplate().query(sql_statement, this.rowMapper);
    }
    
    /**
     * @param id
     * @return
     */
    @GET
    @Path("{id}/related")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<RelatedConcept> getRelatedConcepts(@PathParam("id") final Long id) {
        final Map<Long, RelatedConcept> concepts = new HashMap<>();
        
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getRelatedConcepts.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "id", "CDM_schema" },
            new String[] { String.valueOf(id), getCdmSchema() });
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
        
        getJdbcTemplate().query(sql_statement, new RowMapper<Void>() {
            
            @Override
            public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
                addRelationships(concepts, resultSet);
                return null;
            }
        });
        
        return concepts.values();
    }
    
    @GET
    @Path("{id}/descendants")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<RelatedConcept> getDescendantConcepts(@PathParam("id") final Long id) {
        final Map<Long, RelatedConcept> concepts = new HashMap<>();
        
        String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getDescendantConcepts.sql");
        sql_statement = SqlRender.renderSql(sql_statement, new String[] { "id", "CDM_schema" },
            new String[] { String.valueOf(id), getCdmSchema() });
        sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
        
        getJdbcTemplate().query(sql_statement, new RowMapper<Void>() {
            
            @Override
            public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
                addRelationships(concepts, resultSet);
                return null;
            }
        });
        
        return concepts.values();
    }
    
    private String JoinArray(final String[] array) {
        String result = "";
        
        for (int i = 0; i < array.length; i++) {
            if (i > 0) {
                result += ",";
            }
            
            result += "'" + array[i] + "'";
        }
        
        return result;
    }
}
