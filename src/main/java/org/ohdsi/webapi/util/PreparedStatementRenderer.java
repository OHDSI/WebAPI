package org.ohdsi.webapi.util;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang.ArrayUtils;
import org.ohdsi.circe.helper.ResourceHelper;
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
public class PreparedStatementRenderer {

  private String sql;
  private String sourceDialect = "sql server";
  private List<Object> orderedParamsList;
  private String targetDialect = "sql server";

  public List<Object> getOrderedParamsList() {

    return orderedParamsList;
  }

  public Object[] getOrderedParams() {

    if (orderedParamsList == null || orderedParamsList.isEmpty()) {
      return new Object[0];
    }
    return getOrderedParamsList().toArray(new Object[getOrderedParamsList().size()]);
  }

  public void setTargetDialect(String targetDialect) {

    this.targetDialect = targetDialect;
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

    validateArguments(source, sqlResource, searchRegexes, replacementStrings, sourceDialect, sqlVariableNames, sqlVariableValues);
    /// this part does the heavy lifting, the calling classes can get needed items through getters
    sql = PreparedSqlRender.removeSqlComments(sql);

    updateSqlWithVariableSearchAndReplace(searchRegexes, replacementStrings);
    paramValueMap = buildParamValueMap(sqlVariableNames, sqlVariableValues);

    this.orderedParamsList = PreparedSqlRender.getOrderedListOfParameterValues(paramValueMap, sql);
    buildPreparedStatementSetter();
    sql = PreparedSqlRender.fixPreparedStatementSql(sql, paramValueMap);
    String targetDialect = source != null ? source.getSourceDialect() : this.targetDialect;
    sql = SqlTranslate.translateSql(sql, targetDialect, sessionId, null);
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

    preparedStatementSetter = new PreparedStatementSetter() {
      @Override
      public void setValues(PreparedStatement ps) throws SQLException {

        for (int i = 0; i < orderedParamsList.size(); i++) {
          ps.setObject(i + 1, orderedParamsList.get(i));
        }
      }
    };
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

    return sql;
  }

  public PreparedStatementSetter getSetter() {

    return preparedStatementSetter;
  }

  private Object convertPrimitiveArraysToWrapperArrays(Object value) {

    if (value == null) return value;
    if (!value.getClass().isArray()) return value;


    if (value instanceof boolean[]) return ArrayUtils.toObject((boolean[]) value);
    if (value instanceof byte[]) return ArrayUtils.toObject((byte[]) value);
    if (value instanceof char[]) return ArrayUtils.toObject((char[]) value);
    if (value instanceof char[]) return ArrayUtils.toObject((double[]) value);
    if (value instanceof char[]) return ArrayUtils.toObject((float[]) value);
    if (value instanceof char[]) return ArrayUtils.toObject((int[]) value);
    if (value instanceof long[]) return ArrayUtils.toObject((long[]) value);
    if (value instanceof char[]) return ArrayUtils.toObject((short[]) value);
    return value;
  }


}
