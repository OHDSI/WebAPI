/*
 * Copyright 2017 Observational Health Data Sciences and Informatics [OHDSI.org].
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.SessionUtils;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
@Path("/study/")
@Component
public class StudyService extends AbstractDaoService  {

  private String QUERY_CATAGORICAL_STATS  = ResourceHelper.GetResourceAsString("/resources/study/sql/queryCatagoricalStats.sql");
  private String QUERY_CONTINUOUS_STATS  = ResourceHelper.GetResourceAsString("/resources/study/sql/queryContinuousStats.sql");

  public static class StudyStatistics {
    public List<CatagoricalStat> catagorical = new ArrayList<>();
    public List<ContinuousStat> continuous = new ArrayList<>();
  }
  
  public static class CatagoricalStat {
    public long covariateId;
    public String covariateName;
    public long analysisId;
    public String analysisName;
    public String domainId;
    public String timeWindow;
    public long conceptId;
    public long countValue;
    public BigDecimal statValue;
  }

  public static class ContinuousStat {
    public long covariateId;
    public String covariateName;
    public long analysisId;
    public String analysisName;
    public String domainId;
    public String timeWindow;
    public long conceptId;
    public long countValue;
    public BigDecimal avgValue;
    public BigDecimal stdevValue;
    public long minValue;
    public long p10Value;
    public long p25Value;
    public long medianValue;
    public long p75Value;
    public long p90Value;
    public long maxValue;
  }
  
  private List<String> buildCriteriaClauses (String searchTerm, List<String> analysisIds, List<String> timeWindows) {
    ArrayList<String> clauses = new ArrayList<>();
    
    if (searchTerm != null && searchTerm.length() > 0)
    {
      clauses.add(String.format("lower(ar1.covariate_name) like '%s'",searchTerm));
    }
    
    if (analysisIds != null && analysisIds.size() > 0)
    {
      ArrayList<String> ids = new ArrayList<>();
      ArrayList<String> ranges = new ArrayList<>();
      
      analysisIds.stream().map((analysisIdExpr) -> analysisIdExpr.split(":")).forEachOrdered((parsedIds) -> {
        if (parsedIds.length > 1) {
          ranges.add(String.format("(ar1.analysis_id >= %s and ar1.analysis_id <= %s)", parsedIds[0], parsedIds[1]));
        } else {
          ids.add(parsedIds[0]);
        }
      });
      
      String idClause = "";
      if (ids.size() > 0)
        idClause = String.format("ar1.analysis_id in (%s)", StringUtils.join(ids, ","));
      
      if (ranges.size() > 0)
        idClause += (idClause.length() > 0 ? " OR " : "") + StringUtils.join(ranges, " OR ");
      
      clauses.add("(" + idClause + ")");
    }
  
    if (timeWindows != null && timeWindows.size() > 0) {
      ArrayList<String> timeWindowClauses = new ArrayList<>();
      timeWindows.forEach((timeWindow) -> {
        timeWindowClauses.add(String.format("ar1.time_window = '%s'", timeWindow));
      });
      clauses.add("(" + StringUtils.join(timeWindowClauses, " OR ") + ")" );
    }
    
    return clauses;
  }
  
  
  @GET
  @Path("{studyId}/results/{cohortId}/{sourceKey}")
  @Produces(MediaType.APPLICATION_JSON)
  public StudyStatistics getStudyStatistics(
          @PathParam("studyId") final int studyId, 
          @PathParam("cohortId") final int cohortId, 
          @PathParam("sourceKey") final String sourceKey,
          @QueryParam("searchTerm") final String searchTerm,
          @QueryParam("analysisId") final List<String> analysisIds,
          @QueryParam("timeWindow") final List<String> timeWindows
  ) {  
    StudyStatistics result = new StudyStatistics();
    String translatedSql;
    
    Source source = this.getSourceRepository().findBySourceKey(sourceKey);
    String resultsTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.Results);
    String cdmTableQualifier = source.getTableQualifier(SourceDaimon.DaimonType.CDM);
    List<String> criteriaClauses = buildCriteriaClauses(searchTerm, analysisIds, timeWindows);
    
    String catagoricalQuery = SqlRender.renderSql(
            QUERY_CATAGORICAL_STATS, 
            new String[] {"cdm_database_schema", "results_database_schema", "cohort_definition_id", "criteria_clauses"},
            new String[] {cdmTableQualifier, resultsTableQualifier, Integer.toString(cohortId), criteriaClauses.isEmpty() ? "" : " AND\n" + StringUtils.join(criteriaClauses, "\n AND ")}
    );
    
    translatedSql = SqlTranslate.translateSql(catagoricalQuery, "sql server", source.getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);
    List<CatagoricalStat> catagoricalStats =  this.getSourceJdbcTemplate(source).query(translatedSql, (rs,rowNum) -> { 
      CatagoricalStat mappedRow = new CatagoricalStat() {{
        covariateId = rs.getLong("covariate_id");
        covariateName = rs.getString("covariate_name");
        analysisId = rs.getLong("analysis_id");
        analysisName = rs.getString("analysis_name");
        domainId = rs.getString("domain_id");
        timeWindow = rs.getString("time_window");
        conceptId = rs.getLong("concept_id");
        countValue = rs.getLong("count_value");
        statValue = new BigDecimal(rs.getDouble("stat_value")).setScale(5, RoundingMode.DOWN);
      }};
      return mappedRow;
    });    
    
    
    String continuousQuery = SqlRender.renderSql(
            QUERY_CONTINUOUS_STATS, 
            new String[] {"cdm_database_schema", "results_database_schema", "cohort_definition_id", "criteria_clauses"},
            new String[] {cdmTableQualifier, resultsTableQualifier, Integer.toString(cohortId), criteriaClauses.isEmpty() ? "" : " AND\n" +StringUtils.join(criteriaClauses, "\n AND ")}
    );
    
    translatedSql = SqlTranslate.translateSql(continuousQuery, "sql server", source.getSourceDialect(), SessionUtils.sessionId(), resultsTableQualifier);
    List<ContinuousStat> continuousStats =  this.getSourceJdbcTemplate(source).query(translatedSql, (rs,rowNum) -> { 
      ContinuousStat mappedRow = new ContinuousStat() {{
        covariateId = rs.getLong("covariate_id");
        covariateName = rs.getString("covariate_name");
        analysisId = rs.getLong("analysis_id");
        analysisName = rs.getString("analysis_name");
        domainId = rs.getString("domain_id");
        timeWindow = rs.getString("time_window");
        conceptId = rs.getLong("concept_id");
        countValue = rs.getLong("count_value");
        avgValue = new BigDecimal(rs.getDouble("avg_value")).setScale(5,RoundingMode.DOWN);
        stdevValue = new BigDecimal(rs.getDouble("stdev_value")).setScale(5,RoundingMode.DOWN);
        minValue = rs.getLong("min_value");
        p10Value = rs.getLong("p10_value");
        p25Value = rs.getLong("p25_value");
        medianValue = rs.getLong("median_value");
        p75Value = rs.getLong("p75_value");
        p90Value = rs.getLong("p90_value");
        maxValue = rs.getLong("max_value");
      }};
      return mappedRow;
    });    
    
    result.catagorical = catagoricalStats;
    result.continuous = continuousStats;
    
    return result;
  }
}
