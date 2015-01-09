package org.ohdsi.webapi.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import org.apache.commons.dbutils.DbUtils;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.vocabulary.Concept;
import org.ohdsi.webapi.vocabulary.ConceptRelationship;
import org.ohdsi.webapi.vocabulary.ConceptSearch;
import org.ohdsi.webapi.vocabulary.Domain;
import org.ohdsi.webapi.vocabulary.RelatedConcept;
import org.ohdsi.webapi.vocabulary.Vocabulary;

/**
 *
 * @author fdefalco
 */
@Path("/vocabulary/")
public class VocabularyService {

    @Context
    ServletContext context;

    @Path("search")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Collection<Concept> ExecuteSearch(ConceptSearch search) {
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



        ArrayList<Concept> concepts = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String dialect = context.getInitParameter("database.dialect");
            String databaseDriver = context.getInitParameter("database.driver");
            String databaseUrl = context.getInitParameter("database.url");
            String cdmSchema = context.getInitParameter("database.cdm.schema");
            
            String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/search.sql");
            sql_statement = SqlRender.renderSql(sql_statement, new String[]{"query", "CDM_schema", "filters"}, new String[]{search.query, cdmSchema, filters});
            sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dialect);

            Class.forName(databaseDriver);
            connection = DriverManager.getConnection(databaseUrl);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_statement);

            while (resultSet.next()) {
                Concept concept = new Concept();
                concept.conceptId = resultSet.getLong("CONCEPT_ID");
                concept.conceptCode = resultSet.getString("CONCEPT_CODE");
                concept.conceptName = resultSet.getString("CONCEPT_NAME");
                concept.standardConcept = resultSet.getString("STANDARD_CONCEPT");
                concept.invalidReason = resultSet.getString("INVALID_REASON");
                concept.conceptClassId = resultSet.getString("CONCEPT_CLASS_ID");
                concept.vocabularyId = resultSet.getString("VOCABULARY_ID");
                concept.domainId = resultSet.getString("DOMAIN_ID");
                concepts.add(concept);
            }
            resultSet.close();

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
        return concepts;
    }

    /**
     *
     * @param query
     * @return
     */
    @Path("search/{query}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Concept> ExecuteSearch(@PathParam("query") String query) {

        // escape single quote for queries
        query = query.replace("'", "''");

        ArrayList<Concept> concepts = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/search.sql");
            
            String dialect = context.getInitParameter("database.dialect");
            String databaseDriver = context.getInitParameter("database.driver");
            String databaseUrl = context.getInitParameter("database.url");
            String cdmSchema = context.getInitParameter("database.cdm.schema");            

            sql_statement = SqlRender.renderSql(sql_statement, new String[]{"query","CDM_schema", "filters"}, new String[]{query, cdmSchema, ""});            
            sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dialect);

            Class.forName(databaseDriver);
            connection = DriverManager.getConnection(databaseUrl);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_statement);

            while (resultSet.next()) {
                Concept concept = new Concept();
                concept.conceptId = resultSet.getLong("CONCEPT_ID");
                concept.conceptCode = resultSet.getString("CONCEPT_CODE");
                concept.conceptName = resultSet.getString("CONCEPT_NAME");
                concept.standardConcept = resultSet.getString("STANDARD_CONCEPT");
                concept.invalidReason = resultSet.getString("INVALID_REASON");
                concept.conceptClassId = resultSet.getString("CONCEPT_CLASS_ID");
                concept.vocabularyId = resultSet.getString("VOCABULARY_ID");
                concept.domainId = resultSet.getString("DOMAIN_ID");
                concepts.add(concept);
            }
            resultSet.close();

        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
        return concepts;
    }

    /**
     *
     * @param id
     * @return
     */
    @GET
    @Path("concept/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Concept getConcept(@PathParam("id") long id) {
        Concept concept = new Concept();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {

            String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getConcept.sql");
            String dialect = context.getInitParameter("database.dialect");
            String databaseDriver = context.getInitParameter("database.driver");
            String databaseUrl = context.getInitParameter("database.url");
            String cdmSchema = context.getInitParameter("database.cdm.schema");            
            
            sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "CDM_schema"}, new String[]{String.valueOf(id), cdmSchema});
            sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dialect);

            Class.forName(databaseDriver);
            connection = DriverManager.getConnection(databaseUrl);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_statement);

            while (resultSet.next()) {
                concept.conceptId = resultSet.getLong("CONCEPT_ID");
                concept.conceptCode = resultSet.getString("CONCEPT_CODE");
                concept.conceptName = resultSet.getString("CONCEPT_NAME");
                concept.standardConcept = resultSet.getString("STANDARD_CONCEPT");
                concept.invalidReason = resultSet.getString("INVALID_REASON");
                concept.conceptClassId = resultSet.getString("CONCEPT_CLASS_ID");
                concept.vocabularyId = resultSet.getString("VOCABULARY_ID");
                concept.domainId = resultSet.getString("DOMAIN_ID");
            }

            resultSet.close();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }

        return concept;
    }

    /**
     *
     * @param id
     * @return
     */
    @GET
    @Path("concept/{id}/related")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<RelatedConcept> getRelatedConcepts(@PathParam("id") Long id) {
        HashMap<Long, RelatedConcept> concepts = new HashMap<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getRelatedConcepts.sql");
            String dialect = context.getInitParameter("database.dialect");
            String databaseDriver = context.getInitParameter("database.driver");
            String databaseUrl = context.getInitParameter("database.url");
            String cdmSchema = context.getInitParameter("database.cdm.schema");            
            
            sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "CDM_schema"}, new String[]{String.valueOf(id), cdmSchema});           
            sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dialect);

            Class.forName(databaseDriver);
            connection = DriverManager.getConnection(databaseUrl);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_statement);

            while (resultSet.next()) {
                Long concept_id = resultSet.getLong("CONCEPT_ID");
                if (!concepts.containsKey(concept_id)) {
                    RelatedConcept concept = new RelatedConcept();
                    concept.conceptId = concept_id;
                    concept.conceptCode = resultSet.getString("CONCEPT_CODE");
                    concept.conceptName = resultSet.getString("CONCEPT_NAME");
                    concept.standardConcept = resultSet.getString("STANDARD_CONCEPT");
                    concept.invalidReason = resultSet.getString("INVALID_REASON");
                    concept.vocabularyId = resultSet.getString("VOCABULARY_ID");
                    concept.conceptClassId = resultSet.getString("CONCEPT_CLASS_ID");
                    concept.domainId = resultSet.getString("DOMAIN_ID");

                    ConceptRelationship relationship = new ConceptRelationship();
                    relationship.relationshipName = resultSet.getString("RELATIONSHIP_NAME");
                    relationship.relationshipDistance = resultSet.getInt("RELATIONSHIP_DISTANCE");
                    concept.relationships.add(relationship);

                    concepts.put(concept_id, concept);
                } else {
                    ConceptRelationship relationship = new ConceptRelationship();
                    relationship.relationshipName = resultSet.getString("RELATIONSHIP_NAME");
                    relationship.relationshipDistance = resultSet.getInt("RELATIONSHIP_DISTANCE");
                    concepts.get(concept_id).relationships.add(relationship);
                }
            }

            resultSet.close();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }

        return concepts.values();
    }

        @GET
    @Path("concept/{id}/descendants")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<RelatedConcept> getDescendantConcepts(@PathParam("id") Long id) {
        HashMap<Long, RelatedConcept> concepts = new HashMap<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getDescendantConcepts.sql");
            String dialect = context.getInitParameter("database.dialect");
            String databaseDriver = context.getInitParameter("database.driver");
            String databaseUrl = context.getInitParameter("database.url");
            String cdmSchema = context.getInitParameter("database.cdm.schema");            
            
            sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "CDM_schema"}, new String[]{String.valueOf(id), cdmSchema});           
            sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dialect);

            Class.forName(databaseDriver);
            connection = DriverManager.getConnection(databaseUrl);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_statement);

            while (resultSet.next()) {
                Long concept_id = resultSet.getLong("CONCEPT_ID");
                if (!concepts.containsKey(concept_id)) {
                    RelatedConcept concept = new RelatedConcept();
                    concept.conceptId = concept_id;
                    concept.conceptCode = resultSet.getString("CONCEPT_CODE");
                    concept.conceptName = resultSet.getString("CONCEPT_NAME");
                    concept.standardConcept = resultSet.getString("STANDARD_CONCEPT");
                    concept.invalidReason = resultSet.getString("INVALID_REASON");
                    concept.vocabularyId = resultSet.getString("VOCABULARY_ID");
                    concept.conceptClassId = resultSet.getString("CONCEPT_CLASS_ID");
                    concept.domainId = resultSet.getString("DOMAIN_ID");

                    ConceptRelationship relationship = new ConceptRelationship();
                    relationship.relationshipName = resultSet.getString("RELATIONSHIP_NAME");
                    relationship.relationshipDistance = resultSet.getInt("RELATIONSHIP_DISTANCE");
                    concept.relationships.add(relationship);

                    concepts.put(concept_id, concept);
                } else {
                    ConceptRelationship relationship = new ConceptRelationship();
                    relationship.relationshipName = resultSet.getString("RELATIONSHIP_NAME");
                    relationship.relationshipDistance = resultSet.getInt("RELATIONSHIP_DISTANCE");
                    concepts.get(concept_id).relationships.add(relationship);
                }
            }

            resultSet.close();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }

        return concepts.values();
    }

    @GET
    @Path("domains")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Domain> getDomains() {
        ArrayList<Domain> domains = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getDomains.sql");
            String dialect = context.getInitParameter("database.dialect");
            String databaseDriver = context.getInitParameter("database.driver");
            String databaseUrl = context.getInitParameter("database.url");
            String cdmSchema = context.getInitParameter("database.cdm.schema");            

            sql_statement = SqlRender.renderSql(sql_statement, new String[] {"CDM_schema"}, new String[] {cdmSchema});
            sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dialect);

            Class.forName(databaseDriver);
            connection = DriverManager.getConnection(databaseUrl);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_statement);

            while (resultSet.next()) {
                Domain domain = new Domain();
                domain.domainId = resultSet.getString("DOMAIN_ID");
                domain.domainName = resultSet.getString("DOMAIN_NAME");
                domain.domainConceptId = resultSet.getLong("DOMAIN_CONCEPT_ID");
                domains.add(domain);
            }
            resultSet.close();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
        return domains;
    }

    @GET
    @Path("vocabularies")
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Vocabulary> getVocabularies() {
        ArrayList<Vocabulary> vocabularies = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getVocabularies.sql");
            String dialect = context.getInitParameter("database.dialect");
            String databaseDriver = context.getInitParameter("database.driver");
            String databaseUrl = context.getInitParameter("database.url");
            String cdmSchema = context.getInitParameter("database.cdm.schema");            

            sql_statement = SqlRender.renderSql(sql_statement, new String[] {"CDM_schema"}, new String[] {cdmSchema});
            sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dialect);

            Class.forName(databaseDriver);
            connection = DriverManager.getConnection(databaseUrl);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_statement);

            while (resultSet.next()) {
                Vocabulary vocabulary = new Vocabulary();
                vocabulary.vocabularyId = resultSet.getString("VOCABULARY_ID");
                vocabulary.vocabularyName = resultSet.getString("VOCABULARY_NAME");
                vocabulary.vocabularyReference = resultSet.getString("VOCABULARY_REFERENCE");
                vocabulary.vocabularyVersion = resultSet.getString("VOCABULARY_VERSION");
                vocabulary.vocabularyConceptId = resultSet.getLong("VOCABULARY_CONCEPT_ID");
                vocabularies.add(vocabulary);
            }
            resultSet.close();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        } finally {
            DbUtils.closeQuietly(resultSet);
            DbUtils.closeQuietly(statement);
            DbUtils.closeQuietly(connection);
        }
        return vocabularies;
    }
    
    private String JoinArray(String[] array) {
        String result = "";
        
        for (int i=0; i<array.length; i++) {
            if (i > 0) {
                result += ",";
            }
            
            result += "'" + array[i] + "'";
        }
        
        return result;
    }
}
