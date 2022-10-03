package org.ohdsi.webapi.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableList;
import com.odysseusinc.arachne.commons.types.DBMSType;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.BigQuerySparkTranslate;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.source.Source;
import org.springframework.jdbc.core.PreparedStatementSetter;

/**
 * This class encapsulates the conversion of Sql to a java.sql.PreparedStatement format and sets its ordered arguments in a PreparedStatementSetter object.
 * The conversion of the Sql to Prepared Statement format is necessary to eliminate SQL Injection risk.  The PreparedStatement setter object is necessary
 * to execute prepared statements using the Spring JdbcTemplate framework.
 *
 * @author DNS   SHELLB (Brett Shelley)
 */
public class PreparedStatementRenderer implements ParameterizedSqlProvider {

  private Source source;
  private String sql;
  private String sourceDialect = "sql server";
  private String tempSchema = null;
  private List<Object> orderedParamsList;
  private String targetDialect = "sql server";
	private String sessionId;
	

  public List<Object> getOrderedParamsList() {

    return orderedParamsList;
  }

  public Object[] getOrderedParams() {

    if (orderedParamsList == null || orderedParamsList.isEmpty()) {
      return new Object[0];
    }
    return getOrderedParamsList().toArray(new Object[getOrderedParamsList().size()]);
  }

  private PreparedStatementSetter preparedStatementSetter;

  private Map<String, Object> paramValueMap = new HashMap<String, Object>();


  /**
   * constructs and generated prepared statement sql and a PreparedStatementSetter based on the supplied arguments.
   *
   * @param source             the Source object needed to translate resulting SQL into the targeted dialect
   * @param sqlResource        the path to a sqlResource or the sql itself.  The value will normally be the path to the sql within the classpath. However, if the sql is "hard-coded" and not loaded from a ".sql" file,
   *                           then this argument will be treated as sql and not as a path reference to sql.  If the sqlResource argument does not end with ".sql", then the sqlResource argument will be treated as literal sql.
   * @param searchRegexes      expressions that are to be replaced by a simple String.replaceAll operation. This would normally be the tableQualifierName marker like 'OMOP_schema'. However, these searchRegexes also opens up the
   *                           possibility of inserting dynamically generated sql into a sql resource before it gets processed.
   * @param replacementStrings the corresponding value to replace the searchRegex with. This is normally the tableQualiferValue like 'omop_v5' or some other schema.
   * @param sqlVariableNames   the names of the variables in the base sql
   * @param sqlVariableValues  the values of the variable names in the base sql
   * @param sessionId          the session id for the SqlTranslate methods
   * @throws IllegalArgumentException if the arguments are not valid
   */
  public PreparedStatementRenderer(Source source, String sqlResource, String[] searchRegexes, String[] replacementStrings, String[] sqlVariableNames, Object[] sqlVariableValues, String sessionId) {

    super();

    this.source = source;

    validateArguments(source, sqlResource, searchRegexes, replacementStrings, sourceDialect, sqlVariableNames, sqlVariableValues);
    /// this part does the heavy lifting, the calling classes can get needed items through getters
		sql = PreparedSqlRender.removeSqlComments(sql);

    updateSqlWithVariableSearchAndReplace(searchRegexes, replacementStrings);
    paramValueMap = buildParamValueMap(sqlVariableNames, sqlVariableValues);

    this.orderedParamsList = PreparedSqlRender.getOrderedListOfParameterValues(paramValueMap, sql);
    // NOTE:
    // Look below
    this.orderedParamsList = this.orderedParamsList.stream().filter(Objects::nonNull).collect(Collectors.toList());

    buildPreparedStatementSetter();
    sql = PreparedSqlRender.fixPreparedStatementSql(
      sql,
      paramValueMap,
      // NOTE:
      // Current version of BigQuery driver has issues when NULLs are provided as variables for prepared statements (throws NPE)
      // That's why in case of NULLs we paste them directly into code.
      // And since:
      // - queries processed through "PreparedStatementRenderer" are mainly one-off
      // - sometimes SQL is translated in advance, therefore source is not passed into the constructor
      // we apply the approach to all dialects
      object -> object == null ? "NULL" : "?"
    );

		if (source != null) {
			this.targetDialect = source.getSourceDialect();
			try {
				this.tempSchema = SourceUtils.getTempQualifier(source);
			}
			catch (Exception e) {
				this.tempSchema = null;
			}
		}

		this.sessionId = sessionId;
  }

  public PreparedStatementRenderer(Source source, String sqlResource, String[] searchRegexes, String[] replacementStrings, String sessionId) {

    this(source, sqlResource, searchRegexes, replacementStrings, null, null, sessionId);
  }

  public PreparedStatementRenderer(Source source, String resourcePath, String searchRegex, String replacementString) {

    this(source, resourcePath, new String[]{searchRegex}, new String[]{replacementString}, new String[0], new Object[0], null);
  }

  public PreparedStatementRenderer(Source source, String resourcePath, String[] searchRegexes, String[] replacementStrings, String[] sqlVariableNames, Object[] sqlVariableValues) {

    this(source, resourcePath, searchRegexes, replacementStrings, sqlVariableNames, sqlVariableValues, null);
  }

  public PreparedStatementRenderer(Source source, String resourcePath, String[] searchRegexes, String[] replacementStrings, String sqlVariableName, Object sqlVariableValue) {

    this(source, resourcePath, searchRegexes, replacementStrings, new String[]{sqlVariableName}, new Object[]{sqlVariableValue}, null);
  }

  public PreparedStatementRenderer(Source source, String resourcePath, String searchRegex, String replacementString, String[] sqlVariableNames, Object[] sqlVariableValues) {

    this(source, resourcePath, new String[]{searchRegex}, new String[]{replacementString}, sqlVariableNames, sqlVariableValues, null);
  }

  public PreparedStatementRenderer(Source source, String resourcePath, String searchRegex, String replacementString, String sqlVariableName, Object sqlVariableValue) {

    this(source, resourcePath, new String[]{searchRegex}, new String[]{replacementString}, new String[]{sqlVariableName}, new Object[]{sqlVariableValue}, null);
  }

  public PreparedStatementRenderer(Source source, String resourcePath, String searchRegex, String replacementString, String sqlVariableName, Object sqlVariableValue, String sessionId) {

    this(source, resourcePath, new String[]{searchRegex}, new String[]{replacementString}, new String[]{sqlVariableName}, new Object[]{sqlVariableValue}, sessionId);
  }

  public PreparedStatementRenderer(Source source, String resourcePath, String searchRegex, String replacementString, String[] sqlVariableNames, Object[] sqlVariableValues, String sessionId) {

    this(source, resourcePath, new String[]{searchRegex}, new String[]{replacementString}, sqlVariableNames, sqlVariableValues, sessionId);
  }


  private final void updateSqlWithVariableSearchAndReplace(String[] searchRegexes, String[] replacementStrings) {
    /// a simple search and replace works for tableQualifierNames and values
    if (searchRegexes != null && replacementStrings != null) {
      for (int i = 0; i < searchRegexes.length; i++) {
        String searchRegex = searchRegexes[i];
        String replacement = replacementStrings[i];

        if (searchRegex != null && !searchRegex.trim().isEmpty()) {
          String regexToReplace = null;
          if (searchRegex.startsWith("/*")) {
            regexToReplace = Pattern.quote(searchRegex);
          } else {
            regexToReplace = Pattern.quote(searchRegex.startsWith("@") ? searchRegex : ("@" + searchRegex));
          }

          if (sql.contains("--")) {
            String stringToReplace = searchRegexes[i];
            if (!stringToReplace.startsWith("@")) stringToReplace = "@" + stringToReplace;
            sql = sql.replaceAll(stringToReplace, replacement);

          } else {
            sql = sql.replaceAll(regexToReplace, replacement);
          }
        }
      }
    }
  }


  final void validateArguments(Source source, String sqlResource, String[] tableQualifierNames, String[] tableQualifierValues, String sourceDialect, String[] sqlVariableNames, Object[] sqlVariableValues) {

    validateAndLoadSql(sqlResource);


    if (tableQualifierNames != null && tableQualifierValues != null) {
      if (tableQualifierNames.length != tableQualifierValues.length) {
        throw new IllegalArgumentException("'tableQualifierNames' array argument must have the same length of 'tableQualifierValues' array argument");
      }


      for (int i = 0; i < tableQualifierNames.length; i++) {
        String tableQualifierName = tableQualifierNames[i];
        String tableQualifierValue = tableQualifierValues[i];

        if (tableQualifierName != null && !tableQualifierName.trim().isEmpty()) {
          if (tableQualifierValue == null) {
            throw new IllegalArgumentException("'tableQualifierValue' argument cannot be null or empty string when 'tableQualifierName' argument has been specified");
          }
        }
      }
    }

    if (sqlVariableNames == null && sqlVariableValues != null) {
      throw new IllegalArgumentException("'sqlVariableNames' argument is null");
    }
    if (sqlVariableValues == null && sqlVariableNames != null) {
      throw new IllegalArgumentException("'sqlVariableValues' argument is null");
    }


    paramValueMap = buildParamValueMap(sqlVariableNames, sqlVariableValues);

  }


  final void validateAndLoadSql(String sqlResource) {

    if (sqlResource == null) {
      String message = "'sqlResource' argument cannot be null; 'sqlResource' argument is needed to load sql from classpath";
      throw new IllegalArgumentException(message);
    }
    if (sqlResource.trim().isEmpty()) {
      String message = "'sqlResource' argument cannot be null; 'sqlResource' argument is needed to load sql from classpath";
      throw new IllegalArgumentException(message);
    }

    /// determine if sql is a resource or sql directly
    if (sqlResource.toLowerCase(Locale.ENGLISH).endsWith(".sql")) {
      sql = ResourceHelper.GetResourceAsString(sqlResource);
      if (sql == null || sql.trim().isEmpty())
        throw new RuntimeException("sql string could not be loaded from 'sqlResource' argument");
    } else {
      /// we assume the sql is not a resource reference, but is actually the Sql string itself
      sql = sqlResource;
    }
  }


  final void buildPreparedStatementSetter() {

    preparedStatementSetter = new OrderedPreparedStatementSetter(orderedParamsList);
  }

  /**
   * returns a map containing an equal number of parameters and values.
   *
   * @param parameters
   * @param values
   * @return
   */
  final Map<String, Object> buildParamValueMap(String[] parameters, Object[] values) {

    Map<String, Object> result = new HashMap<>();
    if (parameters == null || values == null) return result;

    if (parameters.length != values.length) {
      String message = "Arrays sizes do not match: parameters length of " + parameters.length + " is not equal to values length of " + values.length;
      throw new IllegalArgumentException(message);
    }

    for (int i = 0; i < parameters.length; i++) {
      String parameter = parameters[i];
      Object value = values[i];

      if (parameter == null && value != null) {
        throw new IllegalArgumentException("null value found in 'parameters' argument");
      } else if (parameter != null && parameter.trim().isEmpty() && value != null)  ///fortify
      {
        throw new IllegalArgumentException("empty string parameter value found in 'parameters' argument");
      }
//			else if ( parameter!=null && !parameter.trim().isEmpty() && value==null )
//			{
//				throw new IllegalArgumentException("null value found in 'values' argument");
//			}
      result.put(parameter, convertPrimitiveArraysToWrapperArrays(value));
    }
    return result;
  }

  public String getSql() {
    if (targetDialect.equals("spark")) {
      try {
        sql = BigQuerySparkTranslate.sparkHandleInsert(sql, source.getSourceConnection());
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
    return SqlTranslate.translateSingleStatementSql(sql, targetDialect, sessionId, tempSchema);
  }

  public PreparedStatementSetter getSetter() {

    return preparedStatementSetter;
  }

  private Object convertPrimitiveArraysToWrapperArrays(Object value) {

    if (value == null) return null;
    if (!value.getClass().isArray()) return value;


    if (value instanceof boolean[]) return ArrayUtils.toObject((boolean[]) value);
    if (value instanceof byte[]) return ArrayUtils.toObject((byte[]) value);
    if (value instanceof char[]) return ArrayUtils.toObject((char[]) value);
    if (value instanceof double[]) return ArrayUtils.toObject((double[]) value);
    if (value instanceof float[]) return ArrayUtils.toObject((float[]) value);
    if (value instanceof int[]) return ArrayUtils.toObject((int[]) value);
    if (value instanceof long[]) return ArrayUtils.toObject((long[]) value);
    if (value instanceof short[]) return ArrayUtils.toObject((short[]) value);
    return value;
  }

  public String generateDebugSql(String sql, String[] searchRegexes, String[] replacementStrings, String[] sqlVariableNames, Object[] sqlVariableValues) {
    String[] vars = Stream.concat(Stream.of(searchRegexes), Stream.of(sqlVariableNames)).toArray(String[]::new);
    String[] vals = Stream.concat(Stream.of(replacementStrings), 
        Stream.of(sqlVariableValues)).map((v) -> { 
          Object obj = convertPrimitiveArraysToWrapperArrays(v);
          String result = String.valueOf(obj);
          if (obj instanceof String[]) {
            result = "'" + StringUtils.join((Object[])v,"','") + "'";
          } else if (obj instanceof Object[]) {
            result = StringUtils.join((Object[])obj,",");
          }
          return result; 
        })
        .toArray(size -> new String[size]);
    return SqlRender.renderSql(sql, vars, vals);
  }
}
