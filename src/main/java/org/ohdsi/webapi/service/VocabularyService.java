package org.ohdsi.webapi.service;

import com.google.common.collect.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import org.ohdsi.webapi.model.*;
import org.ohdsi.webapi.service.vocabulary.*;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.source.SourceInfo;
import org.ohdsi.webapi.util.PageableUtils;
import org.ohdsi.webapi.util.PreparedSqlRender;
import org.ohdsi.webapi.util.PreparedStatementRenderer;
import org.ohdsi.webapi.vocabulary.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.ohdsi.webapi.util.SecurityUtils.whitelist;

/**
 * @author fdefalco
 */
@Path("vocabulary/")
@Component
public class VocabularyService extends AbstractDaoService {

  private static Hashtable<String, VocabularyInfo> vocabularyInfoCache = null;

  @Autowired
  private SourceService sourceService;

  @Autowired
  private CDMResultsService resultsService;

  @Value("${datasource.driverClassName}")
  private String driver;
  
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

  private static Map<String, String> INCLUDED_CONCEPTS_FIELD_MAP = ImmutableMap.of("STANDARD_CONCEPT_CAPTION", "STANDARD_CONCEPT",
          "INVALID_REASON_CAPTION", "INVALID_REASON");

  private static Map<String, List<String>> STANDARD_CONCEPT_VALUES_MAP = ImmutableMap.of("Non-Standard", ImmutableList.of("N"),
          "Standard", ImmutableList.of("S"), "Classification", ImmutableList.of("C"));

  private static Map<String, List<String>> INVALID_REASON_VALUES_MAP = ImmutableMap.of("Valid", ImmutableList.of("V"),
          "Invalid", ImmutableList.of("D", "U"));

  private static Map<String, Map<String, List<String>>> INCLUDED_CONCEPT_VALUES_MAP = ImmutableMap.of("STANDARD_CONCEPT_CAPTION", STANDARD_CONCEPT_VALUES_MAP,
          "INVALID_REASON_CAPTION", INVALID_REASON_VALUES_MAP);

  private static Set<String> INCLUDED_CONCEPTS_COUNTS_FIELDS = ImmutableSet.of("RECORD_COUNT", "DESCENDANT_RECORD_COUNT");

  private static Map<String, String> INCLUDED_CONCEPTS_COUNTS_SELECT = ImmutableMap.of("RECORD_COUNT", "ISNULL(max(c1.agg_count_value), 0) record_count",
          "DESCENDANT_RECORD_COUNT", "ISNULL(sum(c2.agg_count_value), 0) descendant_record_count");

  private static Map<String, Predicate<ConceptAncestors>> CONCEPT_COUNT_PREDICATE_MAP = ImmutableMap.of("RECORD_COUNT", concept -> concept.recordCount > 0,
          "DESCENDANT_RECORD_COUNT", concept -> concept.descendantRecordCount > 0);

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
   * @summary Calculates ancestors for the given descendants
   * 
   * @param ids concepts identifiers from concept set
   *                            
   * @return map {id -> ascendant id}
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
    for (final List<Long> ancestorsBatch : Lists.partition(ids.ancestors, limit > 0 ? limit : ids.ancestors.size())) {
      
        for (final List<Long> descendantsBatch : Lists.partition(ids.descendants, limit > 0 ? limit : ids.descendants.size())) {
          
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
			if (parameterLimit > 0 && identifiers.length > parameterLimit){
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
    PreparedStatementRenderer psr = prepareExecuteSourcecodeLookup(sourcecodes, source, null);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), this.rowMapper);
  }

  protected PreparedStatementRenderer prepareExecuteSourcecodeLookup(String[] sourcecodes, Source source, Function<String, String> queryModifier) {

    String sqlPath = "/resources/vocabulary/sql/lookupSourcecodes.sql";
    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String sql = ResourceHelper.GetResourceAsString(sqlPath);
    if (Objects.nonNull(queryModifier)) {
      sql = queryModifier.apply(sql);
    }
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sql, tqName, tqValue, "sourcecodes", sourcecodes);
    return psr;
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

  @Path("lookup/mappedPage")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PageResponse<Concept> lookupSourcecodesPage(ConceptSetExpressionPageRequest pageRequest) {
    String defaultSourceKey = getDefaultVocabularySourceKey();

    if (defaultSourceKey == null) {
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503
    }

    return lookupMappedSourcecodesPage(defaultSourceKey, pageRequest);
  }

  @Path("lookup/mapped/facets")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public FacetsResponse lookupMappedSourcecodesFacets(FacetsRequest request) {

    String defaultSourceKey = getDefaultVocabularySourceKey();
    if (defaultSourceKey == null) {
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE); // http 503
    }
    return lookupMappedSourcecodesFacets(defaultSourceKey, request);
  }

  @Path("{sourceKey}/lookup/mapped/facets")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public FacetsResponse lookupMappedSourcecodesFacets(@PathParam("sourceKey") String sourceKey, FacetsRequest request) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    long[] sourcecodes = lookupMappedConcepts(sourceKey, request.getExpression());
    FacetValuesStrategy strategy = new MappedLookupFacetValues(sourcecodes, source, getSourceJdbcTemplate(source));
    return doResolveFacets(source, request, strategy);
  }

  @Path("{sourceKey}/lookup/mappedPage")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public PageResponse<Concept> lookupMappedSourcecodesPage(@PathParam("sourceKey") String sourceKey, ConceptSetExpressionPageRequest pageRequest) {
    List<Concept> concepts;
    int totals, filtered;

    long[] sourcecodes = lookupMappedConcepts(sourceKey, pageRequest.getExpression());

    if (sourcecodes.length > 0) {
      int offset = pageRequest.getStart(), limit = pageRequest.getLength();
      String orderClause = PageableUtils.getOrderClause(pageRequest);
      String searchClause = PageableUtils.getSearchClause(pageRequest);
      List<String> where = StringUtils.isNotBlank(searchClause) ? Lists.newArrayList(searchClause) : new ArrayList<>();
      where.addAll(getConceptSetFilters(pageRequest));
      String whereClause = where.isEmpty() ? "" : "WHERE " + where.stream().collect(Collectors.joining(" AND "));

      Source source = getSourceRepository().findBySourceKey(sourceKey);
      Function<String, String> queryModifier = getWhereFunction(whereClause);
      StatementPrepareStrategy sps = new MappedLookupStrategy(sourcecodes);

      PreparedStatementRenderer psr = sps.prepareStatement(source,
              query -> "with rows as ( select ROW_NUMBER() over(ORDER BY " + orderClause + ") as row, Q.* FROM (" + query + ") Q " + whereClause + ") select top "
                      + limit + " * from rows where rows.row > " + offset + " order by rows.row;");

      JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);

      concepts = jdbcTemplate.query(psr.getSql(), psr.getSetter(), conceptRowMapper);

      psr = sps.prepareStatement(source, countFunction);
      totals = jdbcTemplate.query(psr.getSql(), psr.getSetter(), integerResultSetExtractor);

      psr = sps.prepareStatement(source, countFunction.andThen(queryModifier));
      filtered = jdbcTemplate.query(psr.getSql(), psr.getSetter(), integerResultSetExtractor);
    } else {
      concepts = new ArrayList<>();
      totals = 0;
      filtered = 0;
    }

    PageResponse<Concept> response = new PageResponse<>();
    response.setData(concepts);
    response.setDraw(pageRequest.getDraw());
    response.setRecordsTotal(totals);
    response.setRecordsFiltered(filtered);
    return response;
  }

  protected long[] lookupMappedConcepts(String sourceKey, ConceptSetExpression conceptSetExpression) {
    List<Long> identifiers = (List<Long>) resolveConceptSetExpression(sourceKey, conceptSetExpression);
    long[] idents = new long[identifiers.size()];
    for(int i = 0; i < identifiers.size(); i++){
      idents[i] = identifiers.get(i);
    }
    List<Long> lookupConcepts = executeIdentifierLookup(sourceKey, idents)
            .stream()
            .map(c -> c.conceptId)
            .collect(Collectors.toList());
    long[] sourcecodes = new long[lookupConcepts.size()];
    for(int i = 0; i < lookupConcepts.size(); i++) {
      sourcecodes[i] = lookupConcepts.get(i);
    }
    return sourcecodes;
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
    Source source = getSourceRepository().findBySourceKey(sourceKey);
		return executeMappedLookup(source, identifiers);
  }
	
	protected Collection<Concept> executeMappedLookup(Source source, long[] identifiers) {
    Collection<Concept> concepts = new ArrayList<>();
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
							? concepts : new ArrayList<>();				
		}
	}

  protected PreparedStatementRenderer prepareExecuteMappedLookup(long[] identifiers, Source source) {
    return new MappedLookupStrategy(identifiers).prepareStatement(source, null);
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
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, query, "vocabulary_database_schema", tableQualifier);
    String sqlPath = "/resources/vocabulary/sql/getMappedSourcecodes.sql";
    String[] search = new String[]{"identifiers", "CDM_schema"};
    String[] replace = new String[]{psr.getSql(), tableQualifier};
    psr = new PreparedStatementRenderer(source, sqlPath, search, replace, (String[]) null, null);
    return getSourceJdbcTemplate(source).query(psr.getSql(), this.rowMapper);
  }

  @Path("{sourceKey}/search")
  @POST
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Collection<Concept> executeSearch(@PathParam("sourceKey") String sourceKey, ConceptSearch search) {
    Tracker.trackActivity(ActivityType.Search, search.query);

    Source source = getSourceRepository().findBySourceKey(sourceKey);

    PreparedStatementRenderer psr = prepareExecuteSearch(search, source);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), rowMapper);
  }

  protected PreparedStatementRenderer prepareExecuteSearch(ConceptSearch search, Source source) {
    // escape for bracket
    search.query = search.query.replace("[", "[[]");

    String resourcePath = "/resources/vocabulary/sql/search.sql";
    String tqName = "CDM_schema";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    List<String> variableNameList = new ArrayList<>();
    List<Object> variableValueList = new ArrayList<>();

    String filters = "";
    if (search.domainId != null && search.domainId.length > 0) {
      filters += " AND DOMAIN_ID IN (@domainId)";
      variableNameList.add("domainId");
      variableValueList.add(search.domainId);
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

    variableNameList.add("query");
    variableValueList.add(search.query.toLowerCase());

    String[] searchNames = {tqName, "filters"};
    String[] replacementNames = {tqValue, filters};

    String[] variableNames = variableNameList.toArray(new String[variableNameList.size()]);
    Object[] variableValues = variableValueList.toArray(new Object[variableValueList.size()]);

    return new PreparedStatementRenderer(source, resourcePath, searchNames, replacementNames, variableNames, variableValues);

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
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    PreparedStatementRenderer psr = prepareExecuteSearchWithQuery(query, source);
    return getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), this.rowMapper);
  }

  protected PreparedStatementRenderer prepareExecuteSearchWithQuery(String query, Source source) {

    String resourcePath = "/resources/vocabulary/sql/search.sql";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    String[] searchStrings = {"CDM_schema", "filters"};
    String[] replacementStrings = {tqValue, ""};

    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, resourcePath, searchStrings, replacementStrings, "query", query.toLowerCase());
    return psr;
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
    String sqlPath = "/resources/vocabulary/sql/getConcept.sql";
    String tqValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, "CDM_schema", tqValue, "id", whitelist(id));

    Concept concept;
    try {
      concept = getSourceJdbcTemplate(source).queryForObject(psr.getSql(), psr.getOrderedParams(), this.rowMapper);
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

    return queryConceptSetExpression(sourceKey, conceptSetExpression, null);
  }

  private List<Long> queryConceptSetExpression(String sourceKey, ConceptSetExpression conceptSetExpression, Function<String, String> queryModifier) {
    Source source = getSourceRepository().findBySourceKey(sourceKey);
    StatementPrepareStrategy sps = new ConceptSetStrategy(conceptSetExpression);
    String query = sps.prepareStatement(source, queryModifier).getSql();
    final List<Long> identifiers = new ArrayList<>();
    getSourceJdbcTemplate(source).query(query, (PreparedStatementSetter) null, rs -> {
      identifiers.add(rs.getLong("CONCEPT_ID"));
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
  @Path("resolveConceptSetExpressionPage")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PageResponse<ConceptAncestors> resolveConceptSetExpression(ConceptSetExpressionPageRequest pageRequest) throws SQLException {
    String defaultSourceKey = getDefaultVocabularySourceKey();
    if (defaultSourceKey == null) {
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE);
    }

    return resolveConceptSetExpression(defaultSourceKey, pageRequest);
  }

  private Function<String, String> getOrderFunction(String orderClause) {

    return sql -> sql.replaceAll("select distinct", "select distinct ROW_NUMBER() over(ORDER BY " + orderClause + ") as rrow,");
  }

  private Function<String, String> getWhereFunction(String whereClause) {

    return sql -> sql + " " + whereClause + "\n";
  }

  private Function<String, String> countFunction = sql -> "select count(*) from ( " + sql + " ) Q";

  private RowMapper<Concept> conceptRowMapper = (rs, rowNum) -> {
    Concept concept = new Concept();
    concept.conceptId = rs.getLong("CONCEPT_ID");
    concept.domainId = rs.getString("DOMAIN_ID");
    concept.conceptClassId = rs.getString("CONCEPT_CLASS_ID");
    concept.conceptCode = rs.getString("CONCEPT_CODE");
    concept.conceptName = rs.getString("CONCEPT_NAME");
    concept.invalidReason = rs.getString("INVALID_REASON");
    concept.standardConcept = rs.getString("STANDARD_CONCEPT");
    concept.vocabularyId = rs.getString("VOCABULARY_ID");
    return concept;
  };

  private ResultSetExtractor<Integer> integerResultSetExtractor = rs -> rs.next() ? rs.getInt(1) : 0;

  @POST
  @Path("{sourceKey}/resolveConceptSetExpressionPage")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public PageResponse<ConceptAncestors> resolveConceptSetExpression(@PathParam("sourceKey") String sourceKey, ConceptSetExpressionPageRequest pageRequest) throws SQLException {

    int offset = pageRequest.getStart(), limit = pageRequest.getLength();
    String orderClause = PageableUtils.getOrderClause(pageRequest);
    String searchClause = PageableUtils.getSearchClause(pageRequest);
    List<String> where = StringUtils.isNotBlank(searchClause) ? Lists.newArrayList(searchClause) : new ArrayList<>();
    where.addAll(getConceptSetFilters(pageRequest));
    String whereClause = where.isEmpty() ? "" : "WHERE " + where.stream().collect(Collectors.joining(" AND "));

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);

    StatementPrepareStrategy sps = new ConceptSetStrategy(pageRequest.getExpression());
    Function<String, String> queryModifier = getOrderFunction(orderClause).andThen(ConceptSetFacetValues.conceptSetStatementFunction).andThen(getWhereFunction(whereClause));
    if (source.getSourceDialect().equals("impala")) {
      queryModifier = queryModifier.andThen(sql -> sql.replaceAll("distinct ROW_NUMBER\\(\\)", "ROW_NUMBER()"));
    }
    PreparedStatementRenderer psr = sps.prepareStatement(source, queryModifier);
    String query = psr.getSql();
    String queryPage = "with rrows as ( " + query + ") select top " + limit + " * from rrows where rrows.rrow > " + offset + " order by rrows.rrow;";
    queryPage = SqlTranslate.translateSql(queryPage, source.getSourceDialect());

    List<Concept> concepts = jdbcTemplate.query(queryPage, (PreparedStatementSetter) null, conceptRowMapper);

    int totals = countIncludedConceptSets(sourceKey, pageRequest.getExpression());
    String queryFiltered = "select count(*) from (" + query + ") Q;";
    int filtered = jdbcTemplate.query(queryFiltered, rs -> rs.next() ? rs.getInt(1) : 0);

    //Get ancestors
    Ids ancestorIds = new Ids();
    ancestorIds.descendants = concepts.stream().map(c -> c.conceptId).collect(Collectors.toList());
    ancestorIds.ancestors = Arrays.stream(pageRequest.getExpression().items).filter(i -> !i.isExcluded)
            .map(i -> i.concept.conceptId).collect(Collectors.toList());
    Map<Long, Concept> conceptMap = Arrays.stream(pageRequest.getExpression().items).collect(Collectors.toMap(item -> item.concept.conceptId,
            item -> item.concept));
    Map<Long, List<Long>> ascendants = calculateAscendants(sourceKey, ancestorIds);
    Collection<ConceptAncestors> conceptAncestors = concepts.stream().map(ConceptAncestors::new)
            .peek(c -> {
              c.ancestors = ascendants.get(c.conceptId).stream().map(conceptMap::get).collect(Collectors.toList());
              c.recordCount = 0L;
              c.descendantRecordCount = 0L;
            })
            .collect(Collectors.toList());

    //Get record counts
    String[] identifiers = concepts.stream().filter(c -> Objects.equals(c.GetStandardConcept(), "Standard") ||
            Objects.equals(c.GetStandardConcept(), "Classification")).map(c -> Long.toString(c.conceptId)).toArray(String[]::new);
    List<AbstractMap.SimpleEntry<Long, Long[]>> recordCounts = resultsService.getConceptRecordCount(sourceKey, identifiers);
    Map<Long, ConceptAncestors> conceptAncestorsMap = conceptAncestors.stream().collect(Collectors.toMap(c -> c.conceptId, c -> c));
    for(AbstractMap.SimpleEntry<Long, Long[]> count : recordCounts) {
      if (Objects.nonNull(count.getValue()) && conceptAncestorsMap.containsKey(count.getKey()) && count.getValue().length == 2) {
        ConceptAncestors concept = conceptAncestorsMap.get(count.getKey());
        concept.recordCount = count.getValue()[0];
        concept.descendantRecordCount = count.getValue()[1];
      }
    }

    PageResponse<ConceptAncestors> result = new PageResponse<>();
    result.setData(conceptAncestors);
    result.setDraw(pageRequest.getDraw());
    result.setRecordsTotal(totals);
    result.setRecordsFiltered(filtered);
    return result;
  }

  private List<String> getConceptSetFilters(PageRequest pageRequest) {
    List<String> result = new ArrayList<>();
    if (Objects.nonNull(pageRequest.getFilters())) {
      List<String> filters = pageRequest.getFilters().stream()
              .filter(f -> !INCLUDED_CONCEPTS_COUNTS_FIELDS.contains(f.getColumnName()))
              .peek(f -> {
                if (f.isComputed()) {
                  Map<String, List<String>> valuesMap = INCLUDED_CONCEPT_VALUES_MAP.getOrDefault(f.getColumnName(), Maps.newHashMap());
                  f.setValues(f.getValues()
                          .stream()
                          .map(v -> valuesMap.getOrDefault(v, Lists.newArrayList()))
                          .flatMap(Collection::stream)
                          .collect(Collectors.toList()));
                }
                f.setColumnName(INCLUDED_CONCEPTS_FIELD_MAP.getOrDefault(f.getColumnName(), f.getColumnName()));
              })
              .filter(f -> !f.getValues().isEmpty())
              .map(f -> sqlInClause(f.getColumnName(), f.getValues()))
              .collect(Collectors.toList());
      List<String> countFilters = pageRequest.getFilters().stream()
              .filter(f -> INCLUDED_CONCEPTS_COUNTS_FIELDS.contains(f.getColumnName()))
              .map(this::countClause)
              .collect(Collectors.toList());
      result.addAll(filters);
      result.addAll(countFilters);
    }
    return result;
  }

  private String countClause(PageRequest.Filter filter) {
    String sql = ResourceHelper.GetResourceAsString("/resources/vocabulary/sql/whereConceptRecordCount.sql")
            .replaceAll("@select_column", INCLUDED_CONCEPTS_COUNTS_SELECT.get(filter.getColumnName()));
    return filter.getValues().stream()
            .map(Boolean::valueOf)
            .map(v -> v ? "> 0" : "= 0")
            .map(condition -> " (" + sql.replaceAll("@condition", condition) + ") ")
            .collect(Collectors.joining(" OR "));
  }

  private String sqlInClause(String fieldName, List<String> values) {
    return fieldName + " IN (" + values.stream().map(v -> "'" + v + "'").collect(Collectors.joining(",")) + ")";
  }

  @POST
  @Path("{sourceKey}/included-concepts/count")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public Integer countIncludedConceptSets(@PathParam("sourceKey") String sourceKey, ConceptSetExpression conceptSetExpression) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    String query = new ConceptSetStrategy(conceptSetExpression).prepareStatement(source, sql -> "select count(*) from (" + sql + ") Q;").getSql();
    return getSourceJdbcTemplate(source).query(query, rs -> rs.next() ? rs.getInt(1) : 0);
  }

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

  @POST
  @Path("{sourceKey}/included-concepts/facets")
  @Produces(MediaType.APPLICATION_JSON)
  @Consumes(MediaType.APPLICATION_JSON)
  public FacetsResponse getIncludedConceptSetsFacets(@PathParam("sourceKey") String sourceKey, FacetsRequest facetsRequest) {

    Source source = getSourceRepository().findBySourceKey(sourceKey);
    FacetValuesStrategy strategy = new ConceptSetFacetValues(facetsRequest.getExpression(), source, getSourceJdbcTemplate(source));
    return doResolveFacets(source, facetsRequest, strategy);
  }

  protected FacetsResponse doResolveFacets(Source source, FacetsRequest facetsRequest, FacetValuesStrategy strategy) {

    Map<String, List<FacetValue>> facetValues = new TreeMap<>();
    for(FacetsRequest.FacetColumn facetColumn : facetsRequest.getColumns()) {
      List<FacetValue> values = new ArrayList<>();
      final String columnName = facetColumn.getColumnName();
      if (!facetColumn.isComputed()) {
        values = strategy.getFacetValues(columnName);
      } else {
        if ("STANDARD_CONCEPT_CAPTION".equals(columnName)) {
          values = strategy.getFacetValues("STANDARD_CONCEPT").stream()
                  .peek(fv -> {
                    Concept c = new Concept();
                    c.standardConcept = fv.getValue();
                    fv.setValue(c.GetStandardConcept());
                  }).collect(Collectors.toList());
        } else if ("INVALID_REASON_CAPTION".equals(columnName)) {
          values = strategy.getFacetValues("INVALID_REASON").stream()
                  .peek(fv -> {
                    Concept c = new Concept();
                    c.invalidReason = fv.getValue();
                    fv.setValue(c.GetInvalidReason());
                  }).collect(Collectors.toList());
        } else if ("RECORD_COUNT".equals(columnName) || "DESCENDANT_RECORD_COUNT".equals(columnName)) {
          Collection<Long> identifiers = resolveConceptSetExpression(source.getSourceKey(), facetsRequest.getExpression());
          values = getCountFacetValues(source, identifiers, columnName.toLowerCase());
        }
      }
      facetValues.put(facetColumn.getColumnName(), values);
    }
    FacetsResponse response = new FacetsResponse();
    response.setFacets(facetValues);
    return response;
  }

  private List<FacetValue> getCountFacetValues(Source source, Collection<Long> identifiers, String column) {
    List<FacetValue> values = new ArrayList<>();
    int totals = identifiers.size();
    String sqlPath = "/resources/vocabulary/sql/getConceptRecordCountFacet.sql";

    String resultTableQualifierName = "resultTableQualifier";
    String vocabularyTableQualifierName = "vocabularyTableQualifier";
    String resultTableQualifierValue = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String vocabularyTableQualifierValue = source.getTableQualifier(SourceDaimon.DaimonType.Vocabulary);

    String[] tableQualifierNames = {resultTableQualifierName, vocabularyTableQualifierName, "facetColumn"};
    String[] tableQualifierValues = {resultTableQualifierValue, vocabularyTableQualifierValue, column};

    Object[] results = identifiers.toArray();
    PreparedStatementRenderer psr = new PreparedStatementRenderer(source, sqlPath, tableQualifierNames, tableQualifierValues, "conceptIdentifiers", results);
    Integer count = getSourceJdbcTemplate(source).query(psr.getSql(), psr.getSetter(), rs -> rs.next() ? rs.getInt("count") : 0);
    if (count > 0) {
      values.add(new FacetValue(Boolean.TRUE.toString(), count));
    }
    if (count < totals) {
      values.add(new FacetValue(Boolean.FALSE.toString(), totals - count));
    }
    return values;
  }

  @POST
  @Path("included-concepts/facets")
  public FacetsResponse getIncludedConceptSetsFacets(FacetsRequest facetsRequest) {

    String defaultSourceKey = getDefaultVocabularySourceKey();
    if (Objects.isNull(defaultSourceKey)) {
      throw new WebApplicationException(new Exception("No vocabulary or cdm daimon was found in configured sources.  Search failed."), Response.Status.SERVICE_UNAVAILABLE);
    }
    return getIncludedConceptSetsFacets(defaultSourceKey, facetsRequest);
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
