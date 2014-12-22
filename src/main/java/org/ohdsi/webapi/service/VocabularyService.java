package org.ohdsi.webapi.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import javax.servlet.ServletContext;
import javax.ws.rs.GET;
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
import org.ohdsi.webapi.vocabulary.RelatedConcept;

/**
 *
 * @author fdefalco
 */
@Path("/vocabulary/")
public class VocabularyService {

    @Context
    ServletContext context;

    /**
     *
     * @param query
     * @return
     */
    @Path("search/{query}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Concept> ExecuteSearch(@PathParam("query") String query) {
        ArrayList<Concept> concepts = new ArrayList<>();
        Connection connection = null;
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/search.sql");
            sql_statement = SqlRender.renderSql(sql_statement, new String[]{"query"}, new String[]{query});

            String dialect = context.getInitParameter("database.dialect");
            String databaseDriver = context.getInitParameter("database.driver");
            String databaseUrl = context.getInitParameter("database.url");
            sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dialect);

            Class.forName(databaseDriver);
            connection = DriverManager.getConnection(databaseUrl);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_statement);

            while (resultSet.next()) {
                Concept concept = new Concept();
                concept.conceptId = resultSet.getLong("CONCEPT_ID");
                concept.conceptName = resultSet.getString("CONCEPT_NAME");
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
            sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id"}, new String[]{String.valueOf(id)});

            String dialect = context.getInitParameter("database.dialect");
            String databaseDriver = context.getInitParameter("database.driver");
            String databaseUrl = context.getInitParameter("database.url");
            sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", dialect);

            Class.forName(databaseDriver);
            connection = DriverManager.getConnection(databaseUrl);
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql_statement);

            while (resultSet.next()) {
                concept.conceptId = resultSet.getLong("CONCEPT_ID");
                concept.conceptName = resultSet.getString("CONCEPT_NAME");
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
            sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id"}, new String[]{String.valueOf(id)});

            String dialect = context.getInitParameter("database.dialect");
            String databaseDriver = context.getInitParameter("database.driver");
            String databaseUrl = context.getInitParameter("database.url");

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
                    concept.conceptName = resultSet.getString("CONCEPT_NAME");
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
}
