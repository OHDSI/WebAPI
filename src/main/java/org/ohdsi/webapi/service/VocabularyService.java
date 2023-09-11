package org.ohdsi.webapi.service;

import static org.ohdsi.webapi.service.cscompare.ConceptSetCompareService.CONCEPT_SET_COMPARISON_ROW_MAPPER;
import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.ohdsi.analysis.Utils;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.circe.vocabulary.ConceptSetExpressionQueryBuilder;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.vocabulary.Concept;
import org.ohdsi.vocabulary.SearchProviderConfig;
import org.ohdsi.webapi.activity.Activity.ActivityType;
import org.ohdsi.webapi.activity.Tracker;
import org.ohdsi.webapi.conceptset.ConceptSetComparison;
import org.ohdsi.webapi.conceptset.ConceptSetExport;
import org.ohdsi.webapi.conceptset.ConceptSetOptimizationResult;
import org.ohdsi.webapi.service.cscompare.CompareArbitraryDto;
import org.ohdsi.webapi.service.cscompare.ConceptSetCompareService;
import org.ohdsi.webapi.service.cscompare.ExpressionFileUtils;
import org.ohdsi.webapi.service.vocabulary.ConceptSetStrategy;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.util.PreparedSqlRender;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.vocabulary.ConceptRecommendedNotInstalledException;
import org.ohdsi.webapi.vocabulary.ConceptRelationship;
import org.ohdsi.webapi.vocabulary.ConceptSearch;
import org.ohdsi.webapi.vocabulary.DescendentOfAncestorSearch;
import org.ohdsi.webapi.vocabulary.Domain;
import org.ohdsi.webapi.vocabulary.RecommendedConcept;
import org.ohdsi.webapi.vocabulary.RelatedConcept;
import org.ohdsi.webapi.vocabulary.RelatedConceptSearch;
import org.ohdsi.webapi.vocabulary.Vocabulary;
import org.ohdsi.webapi.vocabulary.VocabularyInfo;
import org.ohdsi.webapi.vocabulary.VocabularySearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

 /**
  * Provides REST services for working with
  * the OMOP standardized vocabularies
  * 
  * @summary Vocabulary
  */
@Path("vocabulary/")
@Component
public class VocabularyService extends AbstractDaoService {

  private static Hashtable<String, VocabularyInfo> vocabularyInfoCache = null;
  public static final String DEFAULT_SEARCH_ROWS = "20000";

  @Autowired
  private SourceService sourceService;
  
  @Autowired
  private VocabularySearchService vocabSearchService;

  @Autowired
  protected GenericConversionService conversionService;

  @Autowired
  private ConceptSetCompareService conceptSetCompareService;
  
  @Value("${datasource.driverClassName}")
  private String driver;

  private final RowMapper<Concept> rowMapper = (resultSet, arg1) -> {
    final Concept concept = new Concept();
    concept.conceptId = resultSet.getLong("CONCEPT_ID");
    concept.conceptCode = resultSet.getString("CONCEPT_CODE");
    concept.conceptName = resultSet.getString("CONCEPT_NAME");
    concept.standardConcept = resultSet.getString("STANDARD_CONCEPT");
    concept.invalidReason = resultSet.getString("INVALID_REASON");
    concept.conceptClassId = resultSet.getString("CONCEPT_CLASS_ID");
    concept.vocabularyId = resultSet.getString("VOCABULARY_ID");
    concept.domainId = resultSet.getString("DOMAIN_ID");
    concept.validStartDate = resultSet.getDate("VALID_START_DATE");
    concept.validEndDate = resultSet.getDate("VALID_END_DATE");
    return concept;
  };

  public RowMapper<Concept> getRowMapper() {
    return this.rowMapper;
  }

  private String getDefaultVocabularySourceKey() {
    Source vocabSource = sourceService.getPriorityVocabularySource();
    return Objects.nonNull(vocabSource) ? vocabSource.getSourceKey() : null;
  }

  public Source getPriorityVocabularySource() {

    Source source = sourceService.getPriorityVocabularySource();
    if (Objects.isNull(source)) {
      throw new ForbiddenException();
    }
    return source;
  }

  public ConceptSetExport exportConceptSet(ConceptSet conceptSet, SourceInfo vocabSource) {

    ConceptSetExport export = conversionService.convert(conceptSet, ConceptSetExport.class);
    // Lookup the identifiers
    export.identifierConcepts = executeIncludedConceptLookup(vocabSource.sourceKey, conceptSet.expression);
    // Lookup the mapped items
    export.mappedConcepts = executeMappedLookup(vocabSource.sourceKey, conceptSet.expression);
    return export;
  }
  
  /**
   * Calculates the full set of ancestor and descendant concepts for a list of 
   * ancestor and descendant concepts specified. This is used by ATLAS when
   * navigating the list of included concepts in a concept set - the full list
   * of ancestors (as defined in the concept set) and the descendants (those
   * concepts included when resolving the concept set) are used to determine 
   * which descendant concepts share one or more ancestors.
   * 
   * @summary Calculates ancestors for a list of concepts
   * @param ids Concepts identifiers from concept set
   * @return A map of the form: {id -> List<ascendant id>}
   */
  @Path("{sourceKey}/lookup/identifiers/ancestors")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Map<Long, List<Long>> calculateAscendants(@PathParam("sourceKey") String sourceKey, Ids ids) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    
    if (CollectionUtils.isEmpty(ids.ancestors) || CollectionUtils.isEmpty(ids.descendants)) { 
        return new HashMap<>();
    }

    final int limit = Math.floorDiv(PreparedSqlRender.getParameterLimit(source), 2);

    final List<Map.Entry<Long, Long>> result = new ArrayList<>();
    
    // Here we calculate cartesian product of batches
    for (final List<Long> ancestorsBatch : Lists.partition(ids.ancestors, limit)) {
      
        for (final List<Long> descendantsBatch : Lists.partition(ids.descendants, limit)) {
          
            final PreparedStatementRenderer psr = prepareAscendantsCalculating(
                    ancestorsBatch.toArray(new Long[0]),
                    descendantsBatch.toArray(new Long[0]), 
                    source
            );
            
            result.addAll(getSourceJdbcTemplate(source)
                    .query(
                            psr.getSql(),
                            psr.getSetter(),
                            (resultSet, arg1) -> Maps.immutableEntry(resultSet.getLong("ANCESTOR_ID"), resultSet.getLong("DESCENDANT_ID"))));
        }
    }

    return result
            .stream()
            .collect(
                    Collectors.groupingBy(
                            Map.Entry::getValue, 
                            Collectors.mapping(
                                    Map.Entry::getKey, 
                                    Collectors.toList()
                            )
                    )
            ); 
  }
  
  private static class Ids {
    public List<Long> ancestors;
    public List<Long> descendants;
  }

  protected PreparedStatementRenderer prepareAscendantsCalculating(Long[] identifiers, Long[] descendants, Source source) {

    String sqlPath = "/resources/vocabulary/sql/calculateAscendants.sql";
    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    return new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, 
            new String[]{ "ancestors", "descendants" }, 
            new Object[]{ identifiers, descendants });
  }
  
  /**
   * Get concepts from concept identifiers (IDs) from a specific source
   * 
   * @summary Perform a lookup of an array of concept identifiers returning the
   * matching concepts with their detailed properties.
   * @param sourceKey path parameter specifying the source key identifying the
   * source to use for access to the set of vocabulary tables
   * @param identifiers an array of concept identifiers
   * @return A collection of concepts
   */
  @Path("{sourceKey}/lookup/identifiers")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeIdentifierLookup(@PathParam("sourceKey") String sourceKey, long[] identifiers) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    return executeIdentifierLookup(source, identifiers);
  }

  protected Collection<Concept> executeIdentifierLookup(Source source, long[] identifiers) {
    Collection<Concept> concepts = new ArrayList<>();
    if (identifiers.length == 0) {
      return concepts;
    } else {
      // Determine if we need to chunk up ther request based on the parameter
      // limit of the source RDBMS
      int parameterLimit = PreparedSqlRender.getParameterLimit(source);
      if (parameterLimit > 0 && identifiers.length > parameterLimit) {
        concepts = executeIdentifierLookup(source, Arrays.copyOfRange(identifiers, parameterLimit, identifiers.length));
        identifiers = Arrays.copyOfRange(identifiers, 0, parameterLimit);
      }

      PreparedStatementRenderer psr = prepareExecuteIdentifierLookup(identifiers, source);
      return concepts.addAll(getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), this.rowMapper))
          ? concepts : new ArrayList<>();
    }
  }

  protected PreparedStatementRenderer prepareExecuteIdentifierLookup(long[] identifiers, Source source) {

    String sqlPath = "/resources/vocabulary/sql/lookupIdentifiers.sql";
    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    return new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, "identifiers", identifiers);
  }
  
  /**
   * Get concepts from concept identifiers (IDs) from the default vocabulary 
   * source
   * 
   * @summary Perform a lookup of an array of concept identifiers returning the
   * matching concepts with their detailed properties, using the default source.
   * @param identifiers an array of concept identifiers
   * @return A collection of concepts
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
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    String query = builder.buildExpressionQuery(conceptSetExpression);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, query, "vocabulary_database_schema", tqValue);
    String sqlPath = "/resources/vocabulary/sql/lookupIdentifiers.sql";
    String[] searches = new String[]{"identifiers", "CDM_schema"};
    String[] replacements = new String[]{psr.getSql(), tqValue};
    psr = new PreparedStatementRenderer(source, sqlPath, searches, replacements, (String[]) null, null);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), this.rowMapper);
  }
  

  /**
   * Get concepts from source codes from a specific source
   * 
   * @summary Lookup source codes from the concept CONCEPT_CODE field
   * in the specified vocabulary
   * @param sourceKey path parameter specifying the source key identifying the
   * source to use for access to the set of vocabulary tables
   * @param sourcecodes array of source codes
   * @return A collection of concepts
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
    PreparedStatementRenderer psr = prepareExecuteSourcecodeLookup(sourcecodes, source);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), this.rowMapper);
  }

  protected PreparedStatementRenderer prepareExecuteSourcecodeLookup(String[] sourcecodes, Source source) {

    String sqlPath = "/resources/vocabulary/sql/lookupSourcecodes.sql";
    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, "sourcecodes", sourcecodes);
    return psr;
  }

  /**
   * Get concepts from source codes from the default vocabulary source
   * 
   * @summary Lookup source codes from the concept CONCEPT_CODE field
   * in the specified vocabulary
   * @param sourcecodes array of source codes
   * @return A collection of concepts
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
   * Get concepts mapped to the selected concept identifiers from a 
   * specific source. Find all concepts mapped to the concept identifiers 
   * provided. This end-point will check the CONCEPT, CONCEPT_RELATIONSHIP and
   * SOURCE_TO_CONCEPT_MAP tables.
   * 
   * @summary Concepts mapped to other concepts
   * @param sourceKey path parameter specifying the source key identifying the
   * source to use for access to the set of vocabulary tables
   * @param identifiers an array of concept identifiers
   * @return A collection of concepts
   */
  @Path("{sourceKey}/lookup/mapped")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeMappedLookup(@PathParam("sourceKey") String sourceKey, long[] identifiers) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    return executeMappedLookup(source, identifiers);
  }

	protected Collection<Concept> executeMappedLookup(Source source, long[] identifiers) {
    Collection<Concept> concepts = new HashSet<>();
    if (identifiers.length == 0) {
      return concepts;
    } else {
      // Determine if we need to chunk up the request based on the parameter
      // limit of the source RDBMS
      int parameterLimit = PreparedSqlRender.getParameterLimit(source);
      // Next take into account the fact that the identifiers are used in 3
      // places in the query so the parameter limit will need to be divided
      parameterLimit = Math.floorDiv(parameterLimit, 3);
      if (parameterLimit > 0 && identifiers.length > parameterLimit) {
        concepts = executeMappedLookup(source, Arrays.copyOfRange(identifiers, parameterLimit, identifiers.length));
        identifiers = Arrays.copyOfRange(identifiers, 0, parameterLimit);
      }
      PreparedStatementRenderer psr = prepareExecuteMappedLookup(identifiers, source);
      return concepts.addAll(getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), this.rowMapper))
          ? concepts : new HashSet<>();
    }
  }

  protected PreparedStatementRenderer prepareExecuteMappedLookup(long[] identifiers, Source source) {

    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String resourcePath = "/resources/vocabulary/sql/getMappedSourcecodes.sql";
    return new PreparedStatementRenderer(source, resourcePath, tqName, tqValue, "identifiers", identifiers);
  }

  /**
   * Get concepts mapped to the selected concept identifiers from a 
   * specific source. Find all concepts mapped to the concept identifiers 
   * provided. This end-point will check the CONCEPT, CONCEPT_RELATIONSHIP and
   * SOURCE_TO_CONCEPT_MAP tables.
   * 
   * @summary Concepts mapped to other concepts
   * @param identifiers an array of concept identifiers
   * @return A collection of concepts
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
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, query, "vocabulary_database_schema", tableQualifier);
    String sqlPath = "/resources/vocabulary/sql/getMappedSourcecodes.sql";
    String[] search = new String[]{"identifiers", "CDM_schema"};
    String[] replace = new String[]{psr.getSql(), tableQualifier};
    psr = new PreparedStatementRenderer(source, sqlPath, search, replace, (String[]) null, null);
    return getSourceJdbcTemplate(source).query(psr.getSql(), this.rowMapper);
  }

  /**
   * Search for a concept on the selected source.
   * 
   * @summary Search for a concept on the selected source
   * @param sourceKey The source key for the concept search
   * @param search The ConceptSearch parameters
   * @return A collection of concepts
   */
  @Path("{sourceKey}/search")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSearch(@PathParam("sourceKey") String sourceKey, ConceptSearch search) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);

    PreparedStatementRenderer psr = prepareExecuteSearch(search, source);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), rowMapper);
  }

  protected PreparedStatementRenderer prepareExecuteSearch(ConceptSearch search, Source source) {
    // escape for bracket
    search.query = search.query.replace("[", "[[]");

    String resourcePath = search.isLexical ? "/resources/vocabulary/sql/searchLexical.sql" : "/resources/vocabulary/sql/search.sql";
    String searchSql = ResourceHelper.GetResourceAsString(resourcePath);
    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    List<String> searchNamesList = new ArrayList<>();
    List<Object> replacementNamesList = new ArrayList<>();
    List<String> variableNameList = new ArrayList<>();
    List<Object> variableValueList = new ArrayList<>();

    searchNamesList.add(tqName);
    replacementNamesList.add(tqValue);
    
    String filters = "";
    if (search.domainId != null && search.domainId.length > 0) {
      // lexical search domain filters work slightly differeant than non-lexical
      if (!search.isLexical) {
        // use domain_ids as-is
        filters += " AND DOMAIN_ID IN (@domainId)";
        variableNameList.add("domainId");
        variableValueList.add(search.domainId);
      } else {
        // MEASUREMENT domain is a special case where we want to ensure concept class is 'lab test' or 'procedure'
        ArrayList<String> domainClauses = new ArrayList<>();
        String[] nonMeasurementDomains = Stream.of(search.domainId).filter(s -> !"Measurement".equals(s)).collect(Collectors.toList()).toArray(new String[0]);
        if (nonMeasurementDomains.length > 0) {
          domainClauses.add("DOMAIN_ID IN (@domainId)");
          variableNameList.add("domainId");
          variableValueList.add(nonMeasurementDomains);
        }
        if (Arrays.asList(search.domainId).contains("Measurement")) {
          domainClauses.add("(DOMAIN_ID = 'Measurement' and LOWER(concept_class_id) in ('lab test', 'procedure'))");
        }
        if (!domainClauses.isEmpty()) {
          filters += String.format(" AND (%s)", StringUtils.join(domainClauses, " OR "));
        }
      }
    }

    if (search.vocabularyId != null && search.vocabularyId.length > 0) {
      filters += " AND VOCABULARY_ID IN (@vocabularyId)";
      variableNameList.add("vocabularyId");
      variableValueList.add(search.vocabularyId);
    }

    if (search.conceptClassId != null && search.conceptClassId.length > 0) {
      filters += " AND CONCEPT_CLASS_ID IN (@conceptClassId)";
      variableNameList.add("conceptClassId");
      variableValueList.add(search.conceptClassId);

    }

    if (search.invalidReason != null && !search.invalidReason.trim().isEmpty()) {
      if (search.invalidReason.equals("V")) {
        filters += " AND INVALID_REASON IS NULL ";
      } else {
        filters += " AND INVALID_REASON = @invalidReason";
        variableNameList.add("invalidReason");
        variableValueList.add(search.invalidReason.trim());
      }
    }

    if (search.standardConcept != null) {
      if (search.standardConcept.equals("N")) {
        filters += " AND STANDARD_CONCEPT IS NULL ";
      } else {
        filters += " AND STANDARD_CONCEPT = @standardConcept";
        variableNameList.add("standardConcept");
        variableValueList.add(search.standardConcept.trim());
      }
    }
    
    if (search.isLexical) {
      // 1. Create term variables for the expressions including truncated terms (terms >=8 are truncated to 6 letters
      List<String> searchTerms = Arrays.asList(StringUtils.split(search.query.toLowerCase(), " "));
      List<String> allTerms = Stream.concat(
          searchTerms.stream(), 
          searchTerms.stream().filter(i -> i.length() >= 8).map(i -> StringUtils.left(i,6))
      ).sorted((a,b) -> b.length() - a.length()).collect(Collectors.toList());
      LinkedHashMap<String, String> termMap = new LinkedHashMap<>();
      for (int i=0;i<allTerms.size();i++) {
        termMap.put(String.format("term_%d",i+1), allTerms.get(i));
      }
      // 2. Create REPLACE expressions to caluclate the match ratio
      String replaceExpression = termMap.keySet().stream()
          .reduce("", (acc, element) -> {
            return "".equals(acc) ? 
                String.format("REPLACE(lower(concept_name), '@%s','')",element) // the first iteration
                : String.format("REPLACE(%s, '@%s','')", acc, element); // the subsequent iterations
          });
      searchNamesList.add("replace_expression");
      replacementNamesList.add(replaceExpression);
      
      // 3. Create the set of 'like' expressions for concept name from the terms that are < 8 chars
      List<String> nameFilterList = termMap.keySet().stream()
          .filter(k -> termMap.get(k).length() < 8)
          .map(k -> String.format("lower(concept_name) like '%%@%s%%'",k))
          .collect(Collectors.toList());
      searchNamesList.add("name_filters");
      replacementNamesList.add(StringUtils.join(nameFilterList, " AND "));
      
      // 4. Create the set of 'like' expressions for concept synonyms
      List<String> synonymFilterList = termMap.keySet().stream()
          .filter(k -> termMap.get(k).length() < 8)
          .map(k -> String.format("lower(concept_synonym_name) like '%%@%s%%'",k))
          .collect(Collectors.toList());
      searchNamesList.add("synonym_filters");
      replacementNamesList.add(StringUtils.join(synonymFilterList, " AND "));
      
     // 5. Create name-value pairs for each term paramater
     for (Map.Entry<String,String> entry : termMap.entrySet()) {
       variableNameList.add(entry.getKey());
       variableValueList.add(entry.getValue());
     }
    } else {
      if (!search.query.isEmpty()) {
        String queryFilter = "LOWER(CONCEPT_NAME) LIKE '%@query%' or LOWER(CONCEPT_CODE) LIKE '%@query%'";
        if (StringUtils.isNumeric(search.query)) {
          queryFilter += " or CONCEPT_ID = CAST(@query as int)";
        }
        filters += " AND (" + queryFilter + ")";
        variableNameList.add("query");
        variableValueList.add(search.query.toLowerCase());
      }
    }
    searchSql = StringUtils.replace(searchSql, "@filters", filters);

    String[] searchNames = searchNamesList.toArray(new String[0]);
    String[] replacementNames = replacementNamesList.toArray(new String[0]);

    String[] variableNames = variableNameList.toArray(new String[variableNameList.size()]);
    Object[] variableValues = variableValueList.toArray(new Object[variableValueList.size()]);

    PreparedStatementRenderer renderer = new PreparedStatementRenderer(source, searchSql, searchNames, replacementNames, variableNames, variableValues);
    String debugSql = renderer.generateDebugSql(searchSql, searchNames, replacementNames, variableNames, variableValues);
    return renderer;

  }

  /**
   * Search for a concept on the default vocabulary source.
   * 
   * @summary Search for a concept (default vocabulary source)
   * @param search The ConceptSearch parameters
   * @return A collection of concepts
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
   * Search for a concept based on a query using the selected vocabulary source.
   * 
   * @summary Search for a concept using a query
   * @param sourceKey The source key holding the OMOP vocabulary
   * @param query The query to use to search for concepts
   * @return A collection of concepts
   */
  @Path("{sourceKey}/search/{query}")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSearch(@PathParam("sourceKey") String sourceKey, @PathParam("query") String query) {
    return this.executeSearch(sourceKey, query, DEFAULT_SEARCH_ROWS);
  }
  
  /**
   * Search for a concept based on a query using the default vocabulary source.
   * NOTE: This method uses the query as part of the URL query string
   * 
   * @summary Search for a concept using a query (default vocabulary)
   * @param sourceKey The source key holding the OMOP vocabulary
   * @param query The query to use to search for concepts
   * @param rows The number of rows to return.
   * @return A collection of concepts
   */
  @Path("{sourceKey}/search")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSearch(@PathParam("sourceKey") String sourceKey, @QueryParam("query") String query, @DefaultValue(DEFAULT_SEARCH_ROWS) @QueryParam("rows") String rows) {
    // Verify that the rows parameter contains an integer and is > 0
    try {
        Integer r = Integer.parseInt(rows);
        if (r <= 0) {
            throw new NumberFormatException("The rows parameter must be greater than 0");
        }
    } catch (NumberFormatException nfe) {
        throw nfe;
    }
    
    Collection<Concept> concepts = new ArrayList<>();
    try {
        Source source = getSourceRepository().findBySourceKey(sourceKey);
        VocabularyInfo vocabularyInfo = getInfo(sourceKey);
        String versionKey = vocabularyInfo.version.replace(' ', '_');
        SearchProviderConfig searchConfig = new SearchProviderConfig(source.getSourceKey(), versionKey);
        concepts = vocabSearchService.getSearchProvider(searchConfig).executeSearch(searchConfig, query, rows);
    } catch (Exception ex) {
        log.error("An error occurred during the vocabulary search", ex);
    }
    return concepts;
  }

  public PreparedStatementRenderer prepareExecuteSearchWithQuery(String query, Source source) {

    ConceptSearch search = new ConceptSearch();
    search.query = query;
    return this.prepareExecuteSearch(search, source);
  }

  /**
   * Search for a concept based on a query using the default vocabulary source.
   * NOTE: This method uses the query as part of the URL and not the 
   * query string
   * 
   * @summary Search for a concept using a query (default vocabulary)
   * @param query The query to use to search for concepts
   * @return A collection of concepts
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
   * Get a concept based on the concept identifier from the specified 
   * source
   * 
   * @summary Get concept details
   * @param sourceKey The source containing the vocabulary
   * @param id The concept ID to find
   * @return The concept details
   */
  @GET
  @Path("{sourceKey}/concept/{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Concept getConcept(@PathParam("sourceKey") final String sourceKey, @PathParam("id") final long id) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sqlPath = "/resources/vocabulary/sql/getConcept.sql";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, "CDM_schema", tqValue, "id", whitelist(id));

    Concept concept;
    try {
      concept = getSourceJdbcTemplate(source).queryForObject(psr.getSql(), psr.getOrderedParams(), this.rowMapper);
    } catch (EmptyResultDataAccessException e) {
      log.error("Request for conceptId={} resulted in 0 results", id);
      throw new NotFoundException(String.format("There is no concept with id = %d.", id));
    }
    return concept;
  }
  
  /**
   * Get a concept based on the concept identifier from the default
   * vocabulary source
   * 
   * @summary Get concept details (default vocabulary source)
   * @param id The concept ID to find
   * @return The concept details
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
   * Get related concepts for the selected concept identifier from a source. 
   * Related concepts will include those concepts that have a relationship
   * to the selected concept identifier in the CONCEPT_RELATIONSHIP and 
   * CONCEPT_ANCESTOR tables.
   * 
   * @summary Get related concepts
   * @param sourceKey The source containing the vocabulary
   * @param id The concept ID to find
   * @return A collection of related concepts
   */
  @GET
  @Path("{sourceKey}/concept/{id}/related")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getRelatedConcepts(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sqlPath = "/resources/vocabulary/sql/getRelatedConcepts.sql";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, "CDM_schema", tqValue, "id", whitelist(id));
    getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (RowMapper<Void>) (resultSet, arg1) -> {

      addRelationships(concepts, resultSet);
      return null;
    });

    return concepts.values();
  }

  /**
   * Get ancestor and descendant concepts for the selected concept identifier 
   * from a source. 
   * 
   * @summary Get ancestors and descendants for a concept
   * @param sourceKey The source containing the vocabulary
   * @param id The concept ID
   * @return A collection of related concepts
   */
  @GET
  @Path("{sourceKey}/concept/{id}/ancestorAndDescendant")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getConceptAncestorAndDescendant(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sqlPath = "/resources/vocabulary/sql/getConceptAncestorAndDescendant.sql";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, "CDM_schema", tqValue, "id", whitelist(id));
    getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (RowMapper<Void>) (resultSet, arg1) -> {

      addRelationships(concepts, resultSet);
      return null;
    });

    return concepts.values();
  }
  
  /**
   * Get related concepts for the selected concept identifier from the
   * default vocabulary source. 
   * 
   * @summary Get related concepts (default vocabulary)
   * @param id The concept identifier
   * @return A collection of related concepts
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
 
  /**
   * Get a list of common ancestor concepts for a selected list of concept
   * identifiers using the selected vocabulary source. 
   * 
   * @summary Get common ancestor concepts
   * @param sourceKey The source containing the vocabulary
   * @param identifiers An array of concept identifiers
   * @return A collection of related concepts
   */
  @POST
  @Path("{sourceKey}/commonAncestors")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getCommonAncestors(@PathParam("sourceKey") String sourceKey, Object[] identifiers) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetCommonAncestors(identifiers, source);
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), (RowMapper<Void>) (resultSet, arg1) -> {

        addRelationships(concepts, resultSet);
        return null;
      }
    );

    return concepts.values();
  }

  protected PreparedStatementRenderer prepareGetCommonAncestors(Object[] identifiers, Source source) {

    String sqlPath = "/resources/vocabulary/sql/getCommonAncestors.sql";
    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);


    return new PreparedStatementRenderer(source, sqlPath, tqName, tqValue,
      new String[]{"conceptIdentifierList", "conceptIdentifierListLength"},
      new Object[]{identifiers, identifiers.length});
  }

  /**
   * Get a list of common ancestor concepts for a selected list of concept
   * identifiers using the default vocabulary source. 
   * 
   * @summary Get common ancestor concepts (default vocabulary)
   * @param identifiers An array of concept identifiers
   * @return A collection of related concepts
   */
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
  
  /**
   * Resolve a concept set expression into a collection 
   * of concept identifiers using the selected vocabulary source. 
   * 
   * @summary Resolve concept set expression
   * @param sourceKey The source containing the vocabulary
   * @param conceptSetExpression A concept set expression
   * @return A collection of concept identifiers
   */
  @POST
  @Path("{sourceKey}/resolveConceptSetExpression")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Long> resolveConceptSetExpression(@PathParam("sourceKey") String sourceKey, ConceptSetExpression conceptSetExpression) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = new ConceptSetStrategy(conceptSetExpression).prepareStatement(source, null);
    final ArrayList<Long> identifiers = new ArrayList<>();
    getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new RowCallbackHandler() {
      @Override
      public void processRow(ResultSet rs) throws SQLException {
        identifiers.add(rs.getLong("CONCEPT_ID"));
      }
    });

    return identifiers;
  }

  /**
   * Resolve a concept set expression into a collection 
   * of concept identifiers using the default vocabulary source. 
   * 
   * @summary Resolve concept set expression (default vocabulary)
   * @param conceptSetExpression A concept set expression
   * @return A collection of concept identifiers
   */
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

  /**
   * Resolve a concept set expression to get the count
   * of included concepts using the selected vocabulary source. 
   * 
   * @summary Get included concept counts for concept set expression
   * @param sourceKey The source containing the vocabulary
   * @param conceptSetExpression A concept set expression
   * @return A count of included concepts
   */
  @POST
  @Path("{sourceKey}/included-concepts/count")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Integer countIncludedConceptSets(@PathParam("sourceKey") String sourceKey, ConceptSetExpression conceptSetExpression) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String query = new ConceptSetStrategy(conceptSetExpression).prepareStatement(source, sql -> "select count(*) from (" + sql + ") Q;").getSql();
    return getSourceJdbcTemplate(source).query(query, rs -> rs.next() ? rs.getInt(1) : 0);
  }

  /**
   * Resolve a concept set expression to get the count
   * of included concepts using the default vocabulary source. 
   * 
   * @summary Get included concept counts for concept set expression (default vocabulary)
   * @param conceptSetExpression A concept set expression
   * @return A count of included concepts
   */
  @POST
  @Path("included-concepts/count")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Integer countIncludedConcepSets(ConceptSetExpression conceptSetExpression) {

    String defaultSourceKey = getDefaultVocabularySourceKey();
    if (Objects.isNull(defaultSourceKey)) {
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE);
    }
    return countIncludedConceptSets(defaultSourceKey, conceptSetExpression);
  }


  /**
   * Produces a SQL query to use against your OMOP CDM to create the
   * resolved concept set
   * 
   * @summary Get SQL to resolve concept set expression
   * @param conceptSetExpression A concept set expression
   * @return SQL Statement as text
   */
  @POST
  @Path("conceptSetExpressionSQL")
  @Produces(MediaType.TEXT_PLAIN)
  @Consumes(MediaType.APPLICATION_JSON)
  public String getConceptSetExpressionSQL(ConceptSetExpression conceptSetExpression) {
    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    String query = builder.buildExpressionQuery(conceptSetExpression);
    
    return query;
  }
  
  /**
   * Get a collection of descendant concepts for the selected concept
   * identifier using the selected source key
   * 
   * @summary Get descendant concepts for the selected concept identifier
   * @param sourceKey The source containing the vocabulary
   * @param id The concept identifier
   * @return A collection of concepts
   */
  @GET
  @Path("{sourceKey}/concept/{id}/descendants")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getDescendantConcepts(@PathParam("sourceKey") String sourceKey, @PathParam("id") final Long id) {
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sqlPath = "/resources/vocabulary/sql/getDescendantConcepts.sql";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, "CDM_schema", tqValue, "id", whitelist(id));
    getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        addRelationships(concepts, resultSet);
        return null;
      }
    });

    return concepts.values();
  }

  /**
   * Get a collection of descendant concepts for the selected concept
   * identifier using the default vocabulary
   * 
   * @summary Get descendant concepts for the selected concept identifier (default vocabulary)
   * @param id The concept identifier
   * @return A collection of concepts
   */
  @GET
  @Path("concept/{id}/descendants")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getDescendantConcepts(@PathParam("id") final Long id) {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getDescendantConcepts(defaultSourceKey, id);
  }
  
  /**
   * Get a collection of domains from the domain table in the 
   * vocabulary for the the selected source key.
   * 
   * @summary Get domains
   * @param sourceKey The source containing the vocabulary
   * @return A collection of domains
   */
  @GET
  @Path("{sourceKey}/domains")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Domain> getDomains(@PathParam("sourceKey") String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String sqlPath = "/resources/vocabulary/sql/getDomains.sql";
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, "CDM_schema", tableQualifier);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new RowMapper<Domain>() {

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
  
  /**
   * Get a collection of domains from the domain table in the 
   * default vocabulary.
   * 
   * @summary Get domains (default vocabulary)
   * @return A collection of domains
   */
  @GET
  @Path("domains")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Domain> getDomains() {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    
    if (defaultSourceKey == null)
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503      

    return getDomains(defaultSourceKey);
  }
  
  /**
   * Get a collection of vocabularies from the vocabulary table in the 
   * selected source key.
   * 
   * @summary Get vocabularies
   * @param sourceKey The source containing the vocabulary
   * @return A collection of vocabularies
   */
  @GET
  @Path("{sourceKey}/vocabularies")
  @Produces(MediaType.APPLICATION_JSON)
  public Collection<Vocabulary> getVocabularies(@PathParam("sourceKey") String sourceKey) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String sqlPath = "/resources/vocabulary/sql/getVocabularies.sql";
    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, "CDM_schema", tableQualifier);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new RowMapper<Vocabulary>() {

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
  
  /**
   * Get a collection of vocabularies from the vocabulary table in the 
   * default vocabulary
   * 
   * @summary Get vocabularies (default vocabulary)
   * @param sourceKey The source containing the vocabulary
   * @return A collection of vocabularies
   */
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
      concept.validStartDate = resultSet.getDate("VALID_START_DATE");
      concept.validEndDate = resultSet.getDate("VALID_END_DATE");
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

  /**
   * Get the vocabulary version from the vocabulary table using
   * the selected source key
   * 
   * @summary Get vocabulary version info
   * @param sourceKey The source containing the vocabulary
   * @return The vocabulary info
   */
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
      String sqlPath = "/resources/vocabulary/sql/getInfo.sql";
      String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
      PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, "CDM_schema", tqValue);
      info.dialect = source.getSourceDialect();
      vocabularyInfoCache.put(sourceKey, getSourceJdbcTemplate(source).queryForObject(psr.getSql(), psr.getOrderedParams(), new RowMapper<VocabularyInfo>() {
        @Override
        public VocabularyInfo mapRow(final ResultSet resultSet, final int arg1) throws SQLException {
          info.version = resultSet.getString("VOCABULARY_VERSION");
          return info;
        }
      }));
    }
    
    return vocabularyInfoCache.get(sourceKey);
  }

  public void clearVocabularyInfoCache() {
    vocabularyInfoCache = null;
  }
  
  /**
   * Get the descendant concepts of the selected ancestor vocabulary and 
   * concept class for the selected sibling vocabulary and concept class. 
   * It is unclear how this endpoint is used so it may be a candidate to
   * deprecate.
   * 
   * @summary Get descendant concepts by source
   * @param sourceKey The source containing the vocabulary
   * @param search The descendant of ancestor search object
   * @return A collection of concepts
   */
  @POST
  @Path("{sourceKey}/descendantofancestor")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> getDescendantOfAncestorConcepts(@PathParam("sourceKey") String sourceKey, DescendentOfAncestorSearch search) {
    Tracker.trackActivity(ActivityType.Search, "getDescendantOfAncestorConcepts");
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetDescendantOfAncestorConcepts(search, source);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), this.rowMapper);
  }

  protected PreparedStatementRenderer prepareGetDescendantOfAncestorConcepts(DescendentOfAncestorSearch search, Source source) {

    String sqlPath = "/resources/vocabulary/sql/getDescendentOfAncestorConcepts.sql";
    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String[] names = new String[]{"id", "ancestorVocabularyId", "ancestorClassId", "siblingVocabularyId", "siblingClassId"};
    Object[] values = new Object[]{Integer.valueOf(search.conceptId), search.ancestorVocabularyId, search.ancestorClassId, search.siblingVocabularyId, search.siblingClassId};

    return new PreparedStatementRenderer(source, sqlPath, tqName, tqValue, names, values);
  }

  /**
   * Get the descendant concepts of the selected ancestor vocabulary and 
   * concept class for the selected sibling vocabulary and concept class. 
   * It is unclear how this endpoint is used so it may be a candidate to
   * deprecate.
   * 
   * @summary Get descendant concepts (default vocabulary)
   * @param search The descendant of ancestor search object
   * @return A collection of concepts
   */
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
  
  /**
   * Get the related concepts for a list of concept ids using the 
   * concept_relationship table for the selected source key
   * 
   * @summary Get related concepts
   * @param sourceKey The source containing the vocabulary
   * @param search The concept identifiers of interest
   * @return A collection of concepts
   */
  @Path("{sourceKey}/relatedconcepts")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> getRelatedConcepts(@PathParam("sourceKey") String sourceKey, RelatedConceptSearch search) {
    Tracker.trackActivity(ActivityType.Search, "getRelatedConcepts");
    
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetRelatedConcepts(search, source);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), this.rowMapper);
  }

  protected PreparedStatementRenderer prepareGetRelatedConcepts(RelatedConceptSearch search, Source source) {

    String tableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String resourcePath = "/resources/vocabulary/sql/getRelatedConceptsFiltered.sql";

    String filtersSql = "";
    if (search.vocabularyId != null && search.vocabularyId.length > 0) {
      filtersSql += "VOCABULARY_ID IN (@vocabularyId)";
    }
    if (search.conceptClassId != null && search.conceptClassId.length > 0) {
      if (!filtersSql.isEmpty()) {
        filtersSql += " AND ";
      }
      filtersSql += "CONCEPT_CLASS_ID IN (@conceptClassId)";
    }
    String[] searchStrings = {"CDM_schema", "filters"};
    String[] replacementStrings = {tableQualifier, filtersSql};

    String[] varNames = {"vocabularyId", "conceptClassId", "conceptList"};
    Object[] varValues = {search.vocabularyId, search.conceptClassId, search.conceptId};

    return new PreparedStatementRenderer(source, resourcePath, searchStrings, replacementStrings, varNames, varValues);
  }

  /**
   * Get the related concepts for a list of concept ids using the 
   * concept_relationship table
   * 
   * @summary Get related concepts (default vocabulary)
   * @param search The concept identifiers of interest
   * @return A collection of concepts
   */
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
  
  /**
   * Get the descendant concepts for a selected list of concept ids for a
   * selected source key
   * 
   * @summary Get descendant concepts for selected concepts
   * @param sourceKey The source containing the vocabulary
   * @param conceptList The list of concept identifiers
   * @return A collection of concepts
   */
  @Path("{sourceKey}/conceptlist/descendants")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<RelatedConcept> getDescendantConceptsByList(@PathParam("sourceKey") String sourceKey, String[] conceptList) {
    final Map<Long, RelatedConcept> concepts = new HashMap<>();
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareGetDescendantConceptsByList(conceptList, source);
    getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new RowMapper<Void>() {
      @Override
      public Void mapRow(ResultSet resultSet, int arg1) throws SQLException {
        addRelationships(concepts, resultSet);
        return null;
      }
    });

    return concepts.values();
  }
  
  /**
   * Get the descendant concepts for a selected list of concept ids 
   * 
   * @summary Get descendant concepts for selected concepts (default vocabulary)
   * @param conceptList The list of concept identifiers
   * @return A collection of concepts
   */
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

  protected PreparedStatementRenderer prepareGetDescendantConceptsByList(String[] conceptList, Source source) {
    String sqlPath = "/resources/vocabulary/sql/getDescendantConceptsMultipleConcepts.sql";
    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    List<Integer> conceptArray = Arrays.stream(conceptList).map(NumberUtils::toInt).collect(Collectors.toList());
    return new PreparedStatementRenderer( source, sqlPath, tqName, tqValue, "id", conceptArray.toArray());
  }

  /**
   * Get the recommended concepts for a selected list of concept ids for a
   * selected source key
   * 
   * @summary Get recommended concepts for selected concepts
   * @param sourceKey The source containing the vocabulary
   * @param conceptList The list of concept identifiers
   * @return A collection of recommended concepts
   */
  @Path("{sourceKey}/lookup/recommended")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<RecommendedConcept> getRecommendedConceptsByList(@PathParam("sourceKey") String sourceKey, long[] conceptList) {
    if (conceptList.length == 0) {
      return new ArrayList<RecommendedConcept>(); // empty list of recommendations
    }
    try {
      final Map<Long, RecommendedConcept> concepts = new HashMap<>();
      Source source = getSourceRepository().findBySourceKey(sourceKey);
      PreparedStatementRenderer psr = prepareGetRecommendedConceptsByList(conceptList, source);
      getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), new RowMapper<Void>() {
        @Override
        public Void mapRow(ResultSet resultSet, int i) throws SQLException {
          addRecommended(concepts, resultSet);
          return null;
        }
      });

      return concepts.values();

    } catch (Exception e) {
      if (e.getCause().getMessage().contains("concept_recommended")) {
        throw new ConceptRecommendedNotInstalledException();
      }
      throw e;
    }
  }

  protected PreparedStatementRenderer prepareGetRecommendedConceptsByList(long[] conceptList, Source source) {
    String sqlPath = "/resources/vocabulary/sql/getRecommendConcepts.sql";
    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    return new PreparedStatementRenderer( source, sqlPath, tqName, tqValue, "conceptList", conceptList);
  }

  private void addRecommended(Map<Long, RecommendedConcept> concepts, ResultSet resultSet) throws SQLException {
    final Long concept_id = resultSet.getLong("CONCEPT_ID");

    if (!concepts.containsKey(concept_id)) {
      // use rowmaper to read the standard conncept columns and re-serialize into RecommendedConcept
      final RecommendedConcept concept = Utils.deserialize(Utils.serialize(this.rowMapper.mapRow(resultSet, 0), Boolean.TRUE),RecommendedConcept.class) ;
      concept.relationships = new ArrayList<>();
      concepts.put(concept_id, concept);
    }
    RecommendedConcept concept = concepts.get(concept_id);
    concept.relationships.add(resultSet.getString("RELATIONSHIP_ID"));
  }

  /**
   * Compares two concept set expressions to find which concepts are
   * shared or unique to each concept set for the selected vocabulary source.
   * 
   * @summary Compare concept sets
   * @param sourceKey The source containing the vocabulary
   * @param conceptSetExpressionList Expects a list of exactly 2 concept set expressions
   * @return A collection of concept set comparisons
   */
  @Path("{sourceKey}/compare")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<ConceptSetComparison> compareConceptSets(@PathParam("sourceKey") String sourceKey, ConceptSetExpression[] conceptSetExpressionList) throws Exception {
    if (conceptSetExpressionList.length != 2) {
      throw new Exception("You must specify two concept set expressions in order to use this method.");
    }
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String vocabSchema = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    
    // Get the comparison script
    String sql_statement = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/compareConceptSets.sql");

    // Get the queries that represent the ConceptSetExpressions that were passed
    // into the function
    ConceptSetExpressionQueryBuilder builder = new ConceptSetExpressionQueryBuilder();
    String cs1Query = builder.buildExpressionQuery(conceptSetExpressionList[0]);
    String cs2Query = builder.buildExpressionQuery(conceptSetExpressionList[1]);

    // Insert the queries into the overall comparison script
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"cs1_expression", "cs2_expression"}, new String[]{cs1Query, cs2Query});
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"vocabulary_database_schema"}, new String[]{vocabSchema});
    sql_statement = SqlTranslate.translateSql(sql_statement, source.getSourceDialect());

    // Execute the query
    Collection<ConceptSetComparison> returnVal = getSourceJdbcTemplate(source).query(sql_statement, CONCEPT_SET_COMPARISON_ROW_MAPPER);

    return returnVal;
  }

  @Path("{sourceKey}/compare-arbitrary")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<ConceptSetComparison> compareConceptSetsCsv(final @PathParam("sourceKey") String sourceKey,
                                                                final CompareArbitraryDto dto) throws Exception {
    final ConceptSetExpression[] csExpressionList = dto.compareTargets;
    if (csExpressionList.length != 2) {
      throw new Exception("You must specify two concept set expressions in order to use this method.");
    }

    final Collection<ConceptSetComparison> returnVal = conceptSetCompareService.compareConceptSets(sourceKey, dto);

    // maps for items "not found in DB from input1", "not found in DB from input2"
    final Map<String, org.ohdsi.circe.vocabulary.Concept> input1Ex = ExpressionFileUtils.toExclusionMap(csExpressionList[0].items, returnVal);
    final Map<String, org.ohdsi.circe.vocabulary.Concept> input2ex = ExpressionFileUtils.toExclusionMap(csExpressionList[1].items, returnVal);

    // compare/combine exclusion maps and add the result to the output
    returnVal.addAll(ExpressionFileUtils.combine(input1Ex, input2ex));

    // concept names to display mismatches
    final Map<String, String> names = ExpressionFileUtils.toNamesMap(csExpressionList[0].items, csExpressionList[1].items);
    returnVal.forEach(item -> {
      final String name = names.get(ExpressionFileUtils.getKey(item));
      item.nameMismatch = name != null && !name.equals(item.conceptName);
    });

    return returnVal;
  }

  /**
   * Compares two concept set expressions to find which concepts are
   * shared or unique to each concept set.
   * 
   * @summary Compare concept sets (default vocabulary)
   * @param conceptSetExpressionList Expects a list of exactly 2 concept set expressions
   * @return A collection of concept set comparisons
   */
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
  
  /**
   * Optimizes a concept set expressions to find redundant concepts specified
   * in a concept set expression for the selected source key.
   * 
   * @summary Optimize concept set
   * @param sourceKey The source containing the vocabulary
   * @param conceptSetExpression The concept set expression to optimize
   * @return A concept set optimization
   */
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
    Hashtable<Integer, ConceptSetExpression.ConceptSetItem> allConceptSetItems = new Hashtable<>();
    ArrayList<Integer> includedConcepts = new ArrayList<>();
    ArrayList<Integer> descendantConcepts = new ArrayList<>();
    ArrayList<Integer> allOtherConcepts = new ArrayList<>();
    for(ConceptSetExpression.ConceptSetItem item : conceptSetExpression.items) {
        allConceptSetItems.put(item.concept.conceptId.intValue(), item);
        if (!item.isExcluded) {
            includedConcepts.add(item.concept.conceptId.intValue());
            if (item.includeDescendants) {
                descendantConcepts.add(item.concept.conceptId.intValue());
            }
        } else {
            allOtherConcepts.add(item.concept.conceptId.intValue());
        }
    }
    
    // If no descendant concepts are specified, initialize this field to use concept_id = 0 so the query will work properly
    if (descendantConcepts.isEmpty())
        descendantConcepts.add(0);
    
    String allConceptsList = includedConcepts.stream().map(Object::toString).collect(Collectors.joining(", "));
    String descendantConceptsList = descendantConcepts.stream().map(Object::toString).collect(Collectors.joining(", "));
    
    sql_statement = SqlRender.renderSql(sql_statement, new String[]{"allConcepts", "descendantConcepts", "cdm_database_schema"}, new String[]{allConceptsList, descendantConceptsList, tableQualifier});
    sql_statement = SqlTranslate.translateSql(sql_statement, source.getSourceDialect());

    // Execute the query to obtain a result set that contains the
    // most optimized version of the concept set. Then, using these results,
    // construct a new ConceptSetExpression object that only contains the
    // concepts that were identified as optimal to achieve the same definition
    ConceptSetOptimizationResult returnVal = new ConceptSetOptimizationResult();
    ArrayList<ConceptSetExpression.ConceptSetItem> optimzedExpressionItems = new ArrayList<>();
    ArrayList<ConceptSetExpression.ConceptSetItem> removedExpressionItems = new ArrayList<>();
    List<Map<String, Object>> rows = getSourceJdbcTemplate(source).queryForList(sql_statement);
    for (Map rs : rows) {
        Integer conceptId = Integer.parseInt(rs.get("concept_id").toString());
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
    for(Integer conceptId : allOtherConcepts) {
        ConceptSetExpression.ConceptSetItem csi = allConceptSetItems.get(conceptId);
        optimzedExpressionItems.add(csi);
    }
    returnVal.optimizedConceptSet.items = optimzedExpressionItems.toArray(new ConceptSetExpression.ConceptSetItem[optimzedExpressionItems.size()]);
    returnVal.removedConceptSet.items = removedExpressionItems.toArray(new ConceptSetExpression.ConceptSetItem[removedExpressionItems.size()]);
    
    return returnVal;
  }
  
  /**
   * Optimizes a concept set expressions to find redundant concepts specified
   * in a concept set expression.
   * 
   * @summary Optimize concept set (default vocabulary)
   * @param sourceKey The source containing the vocabulary
   * @param conceptSetExpression The concept set expression to optimize
   * @return A concept set optimization
   */
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
