package org.ohdsi.webapi.service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpressionQueryBuilder;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.activity.Activity.ActivityType;
import org.ohdsi.webapi.activity.Tracker;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;
import org.ohdsi.webapi.conceptset.ConceptSetOptimizationResult;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.vocabulary.ConceptRelationship;
import org.ohdsi.webapi.vocabulary.ConceptSearch;
import org.ohdsi.webapi.vocabulary.DescendentOfAncestorSearch;
import org.ohdsi.webapi.vocabulary.Domain;
import org.ohdsi.webapi.vocabulary.RelatedConcept;
import org.ohdsi.webapi.vocabulary.RelatedConceptSearch;
import org.ohdsi.webapi.vocabulary.Vocabulary;
import org.ohdsi.webapi.vocabulary.VocabularyInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

/**
 * @author fdefalco
 */
@Path("vocabulary/")
@Component
public class VocabularyService extends AbstractDaoService {

  private static Hashtable<String, VocabularyInfo> vocabularyInfoCache = null;

  @Autowired
  private SourceService sourceService;  
  
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

  private String getDefaultVocabularySourceKey()
  {
    // fun with streams:
    // the below expression streams each source, returning the first (or null) which contains a daimon that is either Vocabulary or CDM
    SourceInfo firstSource = sourceService.getSources().stream()
            .filter(source -> source.daimons.stream()
                    .filter(daimon -> daimon.getDaimonType() == SourceDaimon.DaimonType.Vocabulary || daimon.getDaimonType() == SourceDaimon.DaimonType.CDM)
                    .collect(Collectors.toList()).size() > 0)
            .findFirst().orElse(null);
    
    if (firstSource != null)
      return firstSource.sourceKey;
    
    return null;
  }
    
  /**
   * @summary Perform a lookup of an array of concept identifiers returning the
   * matching concepts with their detailed properties.
   * @param sourceKey path parameter specifying the source key identifying the
   * source to use for access to the set of vocabulary tables
   * @param identifiers an array of concept identifiers
   * @return collection of concepts
   */
  @Path("{sourceKey}/lookup/identifiers")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeIdentifierLookup(@PathParam("sourceKey") String sourceKey, long[] identifiers) {
    if (identifiers.length == 0) {
      return new ArrayList<>();
    }
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/lookupIdentifiers.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"identifiers", "CDM_schema"}, new String[]{
      JoinArray(identifiers), tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }
  
  /**
   * @summary Perform a lookup of an array of concept identifiers returning the
   * matching concepts with their detailed properties, using the default source.
   * @param identifiers an array of concept identifiers
   * @return collection of concepts
   */
  @Path("lookup/identifiers")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeIdentifierLookup(long[] identifiers) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return executeIdentifierLookup(defaultSourceKey, identifiers);   
  }  
  
  public Collection<Concept> executeIncludedConceptLookup(String sourceKey, ConceptSetExpression conceptSetExpression) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    String query = builder.buildExpressionQuery(conceptSetExpression);

    query = SqlRender.renderSql(query, new String[]{"cdm_database_schema"}, new String[]{tableQualifier});
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/lookupIdentifiers.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"identifiers", "CDM_schema"}, new String[]{
      query, tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }
  

  /**
   * @summary Lookup source codes in the specified vocabulary
   * @param sourceKey path parameter specifying the source key identifying the
   * source to use for access to the set of vocabulary tables
   * @param sourcecodes array of source codes
   * @return collection of concepts
   */
  @Path("{sourceKey}/lookup/sourcecodes")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSourcecodeLookup(@PathParam("sourceKey") String sourceKey, String[] sourcecodes) {
    if (sourcecodes.length == 0) {
      return new ArrayList<>();
    }

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    for (String sourcecode : sourcecodes) {
      sourcecode = "'" + sourcecode + "'";
    }
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/lookupSourcecodes.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"sourcecodes", "CDM_schema"}, new String[]{
      JoinArray(sourcecodes), tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }

  /**
   * @summary Lookup source codes in the specified vocabulary using the default source.
   * @param sourcecodes array of source codes
   * @return collection of concepts
   */
  @Path("lookup/sourcecodes")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSourcecodeLookup(String[] sourcecodes) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return executeSourcecodeLookup(defaultSourceKey, sourcecodes);
  }
  
  /**
   * @summary find all concepts mapped to the identifiers provided
   * @param sourceKey path parameter specifying the source key identifying the
   * source to use for access to the set of vocabulary tables
   * @param identifiers an array of concept identifiers
   * @return collection of concepts
   */
  @Path("{sourceKey}/lookup/mapped")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeMappedLookup(@PathParam("sourceKey") String sourceKey, long[] identifiers) {
    if (identifiers.length == 0) {
      return new ArrayList<>();
    }

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getMappedSourcecodes.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"identifiers", "CDM_schema"}, new String[]{
      JoinArray(identifiers), tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }

  /**
   * @summary find all concepts mapped to the identifiers provided using the default vocabulary source.
   * @param identifiers an array of concept identifiers
   * @return collection of concepts
   */
  @Path("lookup/mapped")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeMappedLookup(long[] identifiers) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return executeMappedLookup(defaultSourceKey, identifiers);
  }
  
  public Collection<Concept> executeMappedLookup(String sourceKey, ConceptSetExpression conceptSetExpression) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    String query = builder.buildExpressionQuery(conceptSetExpression);

    query = SqlRender.renderSql(query, new String[]{"cdm_database_schema"}, new String[]{tableQualifier});

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getMappedSourcecodes.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"identifiers", "CDM_schema"}, new String[]{
      query, tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }

  @Path("{sourceKey}/search")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSearch(@PathParam("sourceKey") String sourceKey, ConceptSearch search) {
    Tracker.trackActivity(ActivityType.Search, search.query);

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    // escape single quote for queries
    search.query = search.query.replace("'", "''");

    // escape for bracket
    search.query = search.query.replace("[", "[[]");

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

    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/search.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"query", "CDM_schema", "filters"}, new String[]{
      search.query.toLowerCase(), tableQualifier, filters});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }
  
  /**
   * Perform a search using the default vocabulary source.
   * @param search
   * @return 
   */
  @Path("search")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSearch(ConceptSearch search) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return executeSearch(defaultSourceKey, search);    
  }
  
  /**
   * @param query
   * @return
   */
  @Path("{sourceKey}/search/{query}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSearch(@PathParam("sourceKey") String sourceKey, @PathParam("query") String query) {
    Tracker.trackActivity(ActivityType.Search, query);

    // escape single quote for queries
    query = query.replace("'", "''");
    query = query.replace("[", "[[]");

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/search.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"query", "CDM_schema", "filters"}, new String[]{
      query.toLowerCase(), tableQualifier, ""});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }

  /**
   * Executes a search on the highest priority source that is found first
   * @param query
   * @return
   */
  @Path("search/{query}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSearch(@PathParam("query") String query) {
    
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return executeSearch(defaultSourceKey, query);
  }
  /**
   * @param id
   * @return
   */
  @GET
  @Path("{sourceKey}/concept/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Concept getConcept(@PathParam("sourceKey") final String sourceKey, @PathParam("id") final long id) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getConcept.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "CDM_schema"},
            new String[]{String.valueOf(id), tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());
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
   * Returns concept details from the default vocabulary source.
   * @param id
   * @return
   */
  @GET
  @Path("concept/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Concept getConcept(@PathParam("id") final long id) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getConcept(defaultSourceKey, id);
    
  }
  /**
   * @param id
   * @return
   */
  @GET
  @Path("{sourceKey}/concept/{id}/related")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getRelatedConcepts(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getRelatedConcepts.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "CDM_schema"},
            new String[]{String.valueOf(id), tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    getSourceJdbcTemplate(source).query(sql_statement, new RowMapper<Void>() {

      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        addRelationships(concepts, resultSet);
        return null;
      }
    });

    return concepts.values();
  }
  
  /**
   * Returns related concepts from the default vocabulary source.
   * @param id
   * @return
   */
  @GET
  @Path("concept/{id}/related")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getRelatedConcepts(@PathParam("id") final Long id) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getRelatedConcepts(defaultSourceKey, id);
  }
 
  @POST
  @Path("{sourceKey}/commonAncestors")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getCommonAncestors(@PathParam("sourceKey") String sourceKey, Object[] identifiers) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    String conceptIdentifierList = org.springframework.util.StringUtils.arrayToCommaDelimitedString(identifiers);
    String conceptIdentifierListLength = Integer.toString(identifiers.length);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getCommonAncestors.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"conceptIdentifierList", "conceptIdentifierListLength", "CDM_schema"},
            new String[]{conceptIdentifierList, conceptIdentifierListLength, tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

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
  @Path("/commonAncestors")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getCommonAncestors(Object[] identifiers) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getCommonAncestors(defaultSourceKey, identifiers);
  }
  
  @POST
  @Path("{sourceKey}/resolveConceptSetExpression")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Long> resolveConceptSetExpression(@PathParam("sourceKey") String sourceKey, ConceptSetExpression conceptSetExpression) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    String query = builder.buildExpressionQuery(conceptSetExpression);

    query = SqlRender.renderSql(query, new String[]{"cdm_database_schema"}, new String[]{tableQualifier});
    query = SqlTranslate.translateSql(query, "sql server", source.getSourceDialect());

    final ArrayList<Long> identifiers = new ArrayList<>();
    getSourceJdbcTemplate(source).query(query, new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet rs) throws SQLException {
        identifiers.add(rs.getLong("CONCEPT_ID"));
      }
    });

    return identifiers;
  }

  @POST
  @Path("resolveConceptSetExpression")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Long> resolveConceptSetExpression(ConceptSetExpression conceptSetExpression) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return resolveConceptSetExpression(defaultSourceKey, conceptSetExpression);
  }

  @POST
  @Path("conceptSetExpressionSQL")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  public String getConceptSetExpressionSQL(ConceptSetExpression conceptSetExpression) {
    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    String query = builder.buildExpressionQuery(conceptSetExpression);
    
    return query;
  }
  
  @GET
  @Path("{sourceKey}/concept/{id}/descendants")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getDescendantConcepts(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getDescendantConcepts.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "CDM_schema"},
            new String[]{String.valueOf(id), tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

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
  @Path("concept/{id}/descendants")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getDescendantConcepts(@PathParam("id") final Long id) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getDescendantConcepts(defaultSourceKey, id);
  }
  
  @GET
  @Path("{sourceKey}/domains")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Domain> getDomains(@PathParam("sourceKey") String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getDomains.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"CDM_schema"}, new String[]{tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

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
  @Path("domains")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Domain> getDomains() {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getDomains(defaultSourceKey);
  }
  
  @GET
  @Path("{sourceKey}/vocabularies")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Vocabulary> getVocabularies(@PathParam("sourceKey") String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getVocabularies.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"CDM_schema"}, new String[]{tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

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
  
  @GET
  @Path("vocabularies")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Vocabulary> getVocabularies() {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getVocabularies(defaultSourceKey);
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
  @Path("{sourceKey}/info")
  @Produces(MediaType.APPLICATION_JSON)
  public VocabularyInfo getInfo(@PathParam("sourceKey") String sourceKey) {
    if (vocabularyInfoCache == null) {
      vocabularyInfoCache = new Hashtable<>();
    }

    if (!vocabularyInfoCache.containsKey(sourceKey)) {
      final VocabularyInfo info = new VocabularyInfo();
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

      String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getInfo.sql");
      info.dialect = source.getSourceDialect();

      sql_statement = SqlRender.renderSql(sql_statement, new String[]{"CDM_schema"}, new String[]{tableQualifier});
      sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

      vocabularyInfoCache.put(sourceKey, getSourceJdbcTemplate(source).queryForObject(sql_statement, new RowMapper<VocabularyInfo>() {
        @Override
        public VocabularyInfo mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
          info.version = resultSet.getString("VOCABULARY_VERSION");
          return info;
        }
      }));
    }
    
    return vocabularyInfoCache.get(sourceKey);
  }
  
  @POST
  @Path("{sourceKey}/descendantofancestor")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> getDescendantOfAncestorConcepts(@PathParam("sourceKey") String sourceKey, DescendentOfAncestorSearch search) {
    Tracker.trackActivity(ActivityType.Search, "getDescendantOfAncestorConcepts");
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary); 
    
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getDescendentOfAncestorConcepts.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"CDM_schema", "id", "ancestorVocabularyId", "ancestorClassId", "siblingVocabularyId", "siblingClassId"}, new String[]{
      tableQualifier, search.conceptId, search.ancestorVocabularyId, search.ancestorClassId, search.siblingVocabularyId, search.siblingClassId});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());
   
    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }

  @POST
  @Path("descendantofancestor")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> getDescendantOfAncestorConcepts(DescendentOfAncestorSearch search) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getDescendantOfAncestorConcepts(defaultSourceKey, search);
  }
  
  @Path("{sourceKey}/relatedconcepts")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> getRelatedConcepts(@PathParam("sourceKey") String sourceKey, RelatedConceptSearch search) {
    Tracker.trackActivity(ActivityType.Search, "getRelatedConcepts");
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    
    ArrayList<String> filterList = new ArrayList<String>();
    
    long[] conceptIds = search.conceptId;
    
    if (search.vocabularyId != null && search.vocabularyId.length > 0) {
      filterList.add("VOCABULARY_ID IN (" + JoinArray(search.vocabularyId) + ")");
    }

    if (search.conceptClassId != null) {
      filterList.add("CONCEPT_CLASS_ID IN (" + JoinArray(search.conceptClassId) + ")");
    }

    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary); 
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getRelatedConceptsFiltered.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"CDM_schema", "conceptList", "filters"}, new String[]{
      tableQualifier, this.JoinArray(conceptIds), this.JoinArrayList(filterList)});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());
   
    return getSourceJdbcTemplate(source).query(sql_statement, this.rowMapper);
  }

  @Path("relatedconcepts")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> getRelatedConcepts(RelatedConceptSearch search) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getRelatedConcepts(defaultSourceKey, search);
  }
  
  @Path("{sourceKey}/conceptlist/descendants")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getDescendantConceptsByList(@PathParam("sourceKey") String sourceKey, String[] conceptList) {
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String conceptListForQuery = this.JoinArray(conceptList);

    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/getDescendantConceptsMultipleConcepts.sql");
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"id", "CDM_schema"},
            new String[]{conceptListForQuery, tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    getSourceJdbcTemplate(source).query(sql_statement, new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        addRelationships(concepts, resultSet);
        return null;
      }
    });

    return concepts.values();
  }
  
  @Path("conceptlist/descendants")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getDescendantConceptsByList(String[] conceptList) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getDescendantConceptsByList(defaultSourceKey, conceptList);
  }  

  @Path("{sourceKey}/compare")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<ConceptSetComparison> compareConceptSets(@PathParam("sourceKey") String sourceKey, ConceptSetExpression[] conceptSetExpressionList) throws Exception {
      if (conceptSetExpressionList.length != 2) {
          throw new Exception("You must specify two concept set expressions in order to use this method.");
      }
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    
    // Get the comparison script
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/compareConceptSets.sql");

    // Get the queries that represent the ConceptSetExpressions that were passed
    // into the function
    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    String cs1Query = builder.buildExpressionQuery(conceptSetExpressionList[0]);
    String cs2Query = builder.buildExpressionQuery(conceptSetExpressionList[1]);

    // Insert the queries into the overall comparison script
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"cs1_expression", "cs2_expression"}, new String[]{cs1Query, cs2Query});
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"cdm_database_schema"}, new String[]{tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());
    
    // Execute the query
    Collection<ConceptSetComparison> returnVal = getSourceJdbcTemplate(source).query(sql_statement, new RowMapper<ConceptSetComparison>() {
      @Override
      public ConceptSetComparison mapRow(ResultSet rs, int rowNum) throws SQLException {
        ConceptSetComparison csc = new ConceptSetComparison();
        csc.conceptId = rs.getLong("concept_id");
        csc.conceptIn1Only = rs.getLong("concept_in_1_only");
        csc.conceptIn2Only = rs.getLong("concept_in_2_only");
        csc.conceptIn1And2 = rs.getLong("concept_in_both_1_and_2");
        csc.conceptName = rs.getString("concept_name");
        csc.standardConcept = rs.getString("standard_concept");
        csc.invalidReason = rs.getString("invalid_reason");
        csc.conceptCode = rs.getString("concept_code");
        csc.domainId = rs.getString("domain_id");
        csc.vocabularyId = rs.getString("vocabulary_id");
        csc.conceptClassId = rs.getString("concept_class_id");
        return csc;
      }
    });
    
    return returnVal;
  }

  @Path("compare")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<ConceptSetComparison> compareConceptSets(ConceptSetExpression[] conceptSetExpressionList) throws Exception {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return compareConceptSets(defaultSourceKey, conceptSetExpressionList);
  }
  
  @Path("{sourceKey}/optimize")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public ConceptSetOptimizationResult optimizeConceptSet(@PathParam("sourceKey") String sourceKey, ConceptSetExpression conceptSetExpression) throws Exception {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    
    // Get the optimization script
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/optimizeConceptSet.sql");
    
    // Find all of the concepts that should be considered for optimization
    // Create a hashtable to hold all of the contents of the ConceptSetExpression
    // for use later
    Hashtable<String, ConceptSetExpression.ConceptSetItem> allConceptSetItems = new Hashtable<String, ConceptSetExpression.ConceptSetItem>();
    ArrayList<String> includedConcepts = new ArrayList<String>();
    ArrayList<String> descendantConcepts = new ArrayList<String>();
    ArrayList<String> allOtherConcepts = new ArrayList<String>();
    for(ConceptSetExpression.ConceptSetItem item : conceptSetExpression.items) {
        allConceptSetItems.put(item.concept.conceptId.toString(), item);
        if (!item.isExcluded) {
            includedConcepts.add(item.concept.conceptId.toString());
            if (item.includeDescendants) {
                descendantConcepts.add(item.concept.conceptId.toString());
            }
        } else {
            allOtherConcepts.add(item.concept.conceptId.toString());
        }
    }
    
    // If no descendant concepts are specified, initialize this field to use concept_id = 0 so the query will work properly
    if (descendantConcepts.isEmpty())
        descendantConcepts.add("0");
    
    String allConceptsList = this.JoinArray(includedConcepts.toArray(new String[includedConcepts.size()]));
    String descendantConceptsList = this.JoinArray(descendantConcepts.toArray(new String[descendantConcepts.size()]));
    
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"allConcepts", "descendantConcepts", "cdm_database_schema"}, new String[]{allConceptsList, descendantConceptsList, tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, "sql server", source.getSourceDialect());

    // Execute the query to obtain a result set that contains the
    // most optimized version of the concept set. Then, using these results,
    // construct a new ConceptSetExpression object that only contains the
    // concepts that were identified as optimal to achieve the same definition
    ConceptSetOptimizationResult returnVal = new ConceptSetOptimizationResult();
    ArrayList<ConceptSetExpression.ConceptSetItem> optimzedExpressionItems = new ArrayList<>();
    ArrayList<ConceptSetExpression.ConceptSetItem> removedExpressionItems = new ArrayList<>();
    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
    for (Map rs : rows) {
        String conceptId = String.valueOf(rs.get("concept_id"));
        String removed = String.valueOf(rs.get("removed"));
        ConceptSetExpression.ConceptSetItem csi = allConceptSetItems.get(conceptId);
        if (removed.equals("0")) {
            optimzedExpressionItems.add(csi);            
        } else {
            removedExpressionItems.add(csi);
        }
    }
    // Re-add back the other concepts that are not considered
    // as part of the optimizatin process
    for(String conceptId : allOtherConcepts) {
        ConceptSetExpression.ConceptSetItem csi = allConceptSetItems.get(conceptId);
        optimzedExpressionItems.add(csi);
    }
    returnVal.optimizedConceptSet.items = optimzedExpressionItems.toArray(new ConceptSetExpression.ConceptSetItem[optimzedExpressionItems.size()]);
    returnVal.removedConceptSet.items = removedExpressionItems.toArray(new ConceptSetExpression.ConceptSetItem[removedExpressionItems.size()]);
    
    return returnVal;
  }
  
  @Path("optimize")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public ConceptSetOptimizationResult optimizeConceptSet(ConceptSetExpression conceptSetExpression) throws Exception {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return optimizeConceptSet(defaultSourceKey, conceptSetExpression);
  }
  
  private String JoinArray(final long[] array) {
    String result = "";

    for (int i = 0; i < array.length; i++) {
      if (i > 0) {
        result += ",";
      }

      result += array[i];
    }

    return result;
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
  
  private String JoinArrayList(final ArrayList<String> array){
      String result = "";
    
      for (int i = 0; i < array.size(); i++) {
        if (i > 0) {
          result += " AND ";
        }

        result += array.get(i);
      }

    return result;
  }
}
