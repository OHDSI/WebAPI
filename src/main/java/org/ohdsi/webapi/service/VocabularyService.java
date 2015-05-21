package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.vocabulary.Concept;
import org.ohdsi.webapi.vocabulary.ConceptRelationship;
import org.ohdsi.webapi.vocabulary.ConceptSearch;
import org.ohdsi.webapi.vocabulary.ConceptSetExpression;
import org.ohdsi.webapi.vocabulary.ConceptSetExpressionQueryBuilder;
import org.ohdsi.webapi.vocabulary.Domain;
import org.ohdsi.webapi.vocabulary.RelatedConcept;
import org.ohdsi.webapi.vocabulary.Vocabulary;
import org.ohdsi.webapi.vocabulary.VocabularyInfo;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author fdefalco
 */
@Path("{sourceKey}/vocabulary/")
@Component
public class VocabularyService extends AbstractDaoService {

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

  @Path("lookup")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeLookup(@PathParam("sourceKey") String sourceKey, String[] identifiers) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/lookup.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"identifiers", "CDM_schema"}, new String[]{
      JoinArray(identifiers), getCdmSchema()});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }
  
  @Path("search")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSearch(@PathParam("sourceKey") String sourceKey, ConceptSearch search) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
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
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"query", "CDM_schema", "filters"}, new String[]{
      search.query.toLowerCase(), getCdmSchema(), filters});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
   
    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }

  /**
   * @param query
   * @return
   */
  @Path("search/{query}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSearch(@PathParam("sourceKey") String sourceKey, @PathParam("query") String query) {
    // escape single quote for queries
    query = query.replace("'", "''");
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/search.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"query", "CDM_schema", "filters"}, new String[]{
      query, getCdmSchema(), ""});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }

  /**
   * @param id
   * @return
   */
  @GET
  @Path("concept/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Concept getConcept(@PathParam("sourceKey") final String sourceKey, @PathParam("id") final long id) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getConcept.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "CDM_schema"},
            new String[]{String.valueOf(id), getCdmSchema()});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
    Concept concept = null;
    try {
      concept = getSourceJdbcTemplate(source).queryForObject(sql_statement, this.rowMapper);
    } catch (EmptyResultDataAccessException e) {
      log.debug(String.format("Request for conceptId=%s resulted in 0 results", id));
      throw new WebApplicationException(Response.Status.RESET_CONTENT); // http 205
    }
    return concept;
  }

  /**
   * @param id
   * @return
   */
  @GET
  @Path("concept/{id}/related")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getRelatedConcepts(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getRelatedConcepts.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "CDM_schema"},
            new String[]{String.valueOf(id), getCdmSchema()});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());

    getSourceJdbcTemplate(source).query(sql_statement, new RowMapper<Void>() {

      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        addRelationships(concepts, resultSet);
        return null;
      }
    });

    return concepts.values();
  }

  @POST
  @Path("commonAncestors")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getCommonAncestors(@PathParam("sourceKey") String sourceKey, Object[] identifiers) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);    
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    String conceptIdentifierList = org.springframework.util.StringUtils.arrayToCommaDelimitedString(identifiers);
    String conceptIdentifierListLength = Integer.toString(identifiers.length);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getCommonAncestors.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"conceptIdentifierList", "conceptIdentifierListLength", "CDM_schema"},
            new String[]{conceptIdentifierList, conceptIdentifierListLength, getCdmSchema()});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());

    getSourceJdbcTemplate(source).query(sql_statement, new RowMapper<Void>() {

      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        addRelationships(concepts, resultSet);
        return null;
      }
    });

    return concepts.values();
  }

  private ArrayList<Long> identifiers;
  
  @POST
  @Path("resolveConceptSetExpression")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Long> resolveConceptSetExpression(@PathParam("sourceKey") String sourceKey, ConceptSetExpression conceptSetExpression) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    
    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    String query = builder.buildExpressionQuery(conceptSetExpression);

    query = SqlRender.renderSql(query, new String[]{"cdm_database_schema"}, new String[]{getCdmSchema()});
    query = SqlTranslate.translateSql(query, "sql server", getDialect());

    identifiers = new ArrayList<>();
    getSourceJdbcTemplate(source).query(query, new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet rs) throws SQLException {
        identifiers.add(rs.getLong("CONCEPT_ID"));
      }
    });
    
    return identifiers;
  }

  @GET
  @Path("concept/{id}/descendants")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getDescendantConcepts(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getDescendantConcepts.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "CDM_schema"},
            new String[]{String.valueOf(id), getCdmSchema()});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());

    getSourceJdbcTemplate(source).query(sql_statement, new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        addRelationships(concepts, resultSet);
        return null;
      }
    });

    return concepts.values();
  }

  @GET
  @Path("domains")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Domain> getDomains(@PathParam("sourceKey") String sourceKey) {
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getDomains.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"CDM_schema"}, new String[]{getCdmSchema()});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    
    return getSourceJdbcTemplate(source).query(sql_statement, new RowMapper<Domain>() {

      @Override
      public Domain mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
        final Domain domain = new Domain();
        domain.domainId = resultSet.getString("DOMAIN_ID");
        domain.domainName = resultSet.getString("DOMAIN_NAME");
        domain.domainConceptId = resultSet.getLong("DOMAIN_CONCEPT_ID");
        return domain;
      }
    });
  }

  @GET
  @Path("vocabularies")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Vocabulary> getVocabularies(@PathParam("sourceKey") String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getVocabularies.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"CDM_schema"}, new String[]{getCdmSchema()});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, new RowMapper<Vocabulary>() {

      @Override
      public Vocabulary mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
        final Vocabulary vocabulary = new Vocabulary();
        vocabulary.vocabularyId = resultSet.getString("VOCABULARY_ID");
        vocabulary.vocabularyName = resultSet.getString("VOCABULARY_NAME");
        vocabulary.vocabularyReference = resultSet.getString("VOCABULARY_REFERENCE");
        vocabulary.vocabularyVersion = resultSet.getString("VOCABULARY_VERSION");
        vocabulary.vocabularyConceptId = resultSet.getLong("VOCABULARY_CONCEPT_ID");
        return vocabulary;
      }
    });
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

  //TODO
  @GET
  @Path("info")
  @Produces(MediaType.APPLICATION_JSON)
  public VocabularyInfo getInfo(@PathParam("sourceKey") String sourceKey) {
    final VocabularyInfo info = new VocabularyInfo();
    Source source = getSourceRepository().findBySourceKey(sourceKey);    

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getInfo.sql");
    info.dialect = getDialect();

    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"CDM_schema"}, new String[]{getCdmSchema()});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", getDialect());

    return getSourceJdbcTemplate(source).queryForObject(sql_statement, new RowMapper<VocabularyInfo>() {
      @Override
      public VocabularyInfo mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
        info.version = resultSet.getString("VOCABULARY_VERSION");
        return info;
      }
    });
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
