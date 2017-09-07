/*
 * Copyright 2015 Observational Health Data Sciences and Informatics [OHDSI.org].
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
package org.ohdsi.webapi.ircalc;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.circe.cohortdefinition.CriteriaGroup;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.ConceptSetExpressionQueryBuilder;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class IRAnalysisQueryBuilder {
  
  private final static ConceptSetExpressionQueryBuilder conceptSetQueryBuilder = new ConceptSetExpressionQueryBuilder();
  private final static CohortExpressionQueryBuilder cohortExpressionQueryBuilder = new CohortExpressionQueryBuilder();
  
  private final static String PERFORM_ANALYSIS_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/incidencerate/sql/performAnalysis.sql"); 
  private final static String STRATA_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/incidencerate/sql/strata.sql");  
  
  public static class BuildExpressionQueryOptions {
    @JsonProperty("cdmSchema")  
    public String cdmSchema;

    @JsonProperty("resultsSchema")  
    public String resultsSchema;
    
  } 

  private String getStrataQuery(CriteriaGroup strataCriteria)
  {
    String resultSql = STRATA_QUERY_TEMPLATE;
    String additionalCriteriaQuery = "\nJOIN (\n" + cohortExpressionQueryBuilder.getCriteriaGroupQuery(strataCriteria, "#analysis_events") + ") AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id";
    additionalCriteriaQuery = StringUtils.replace(additionalCriteriaQuery,"@indexId", "" + 0);
    resultSql = StringUtils.replace(resultSql, "@additionalCriteriaQuery", additionalCriteriaQuery);
    return resultSql;
  }
  
  public String buildAnalysisQuery(IncidenceRateAnalysis analyisis, BuildExpressionQueryOptions options) {
    String resultSql = PERFORM_ANALYSIS_QUERY_TEMPLATE;
    IncidenceRateAnalysisExpression analysisExpression;

    try
    {
      ObjectMapper mapper = new ObjectMapper();
      analysisExpression = mapper.readValue(analyisis.getDetails().getExpression(), IncidenceRateAnalysisExpression.class);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    
    // everything deserialized successfully

    // target and outcome statements for analysis
    ArrayList<String> cohortIdStatements = new ArrayList<>();
    for (int targetId : analysisExpression.targetIds) {
      cohortIdStatements.add(String.format("SELECT %d as cohort_id, 0 as is_outcome", targetId));
    }

    for (int outcomeId : analysisExpression.outcomeIds) {
      cohortIdStatements.add(String.format("SELECT %d as cohort_id, 1 as is_outcome", outcomeId));
    }
    
    resultSql = StringUtils.replace(resultSql,"@cohortInserts", StringUtils.join(cohortIdStatements,"\nUNION\n"));

    // apply adjustments
    
    String adjustmentExpression = "DATEADD(day,%d,%s)";
    
    String adjustedStart = String.format(adjustmentExpression, 
            analysisExpression.timeAtRisk.start.offset,
            analysisExpression.timeAtRisk.start.dateField == FieldOffset.DateField.StartDate ? "cohort_start_date" : "cohort_end_date");    
    resultSql = StringUtils.replace(resultSql,"@adjustedStart", adjustedStart);

    String adjustedEnd = String.format(adjustmentExpression, 
            analysisExpression.timeAtRisk.end.offset,
            analysisExpression.timeAtRisk.end.dateField == FieldOffset.DateField.StartDate ? "cohort_start_date" : "cohort_end_date");    
    resultSql = StringUtils.replace(resultSql,"@adjustedEnd", adjustedEnd);
    
    // apply study window WHERE clauses
    ArrayList<String> studyWindowClauses = new ArrayList<>();
    if (analysisExpression.studyWindow != null)
    {
      if (analysisExpression.studyWindow.startDate != null && analysisExpression.studyWindow.startDate.length() > 0)
        studyWindowClauses.add(String.format("t.cohort_start_date >= '%s'", analysisExpression.studyWindow.startDate));
      if (analysisExpression.studyWindow.endDate != null && analysisExpression.studyWindow.endDate.length() > 0)
        studyWindowClauses.add(String.format("t.cohort_start_date <= '%s'", analysisExpression.studyWindow.endDate));
    }
    if (studyWindowClauses.size() > 0)
      resultSql = StringUtils.replace(resultSql, "@cohortDataFilter", "AND " + StringUtils.join(studyWindowClauses," AND "));
    else
      resultSql = StringUtils.replace(resultSql, "@cohortDataFilter", "");
    
    // add end dates if study window end is defined
    if (analysisExpression.studyWindow != null && analysisExpression.studyWindow.endDate != null && analysisExpression.studyWindow.endDate.length() > 0)
    {
      StringBuilder endDatesQuery = new StringBuilder(String.format("UNION\nselect combos.target_id, combos.outcome_id, t.subject_id, t.cohort_start_date, '%s' as followup_end, 0 as is_case", analysisExpression.studyWindow.endDate));
      endDatesQuery.append("\nFROM cteCohortCombos combos");
      endDatesQuery.append("\nJOIN  cteCohortData t on combos.target_id = t.target_id and combos.outcome_id = t.outcome_id");
      
      resultSql = StringUtils.replace(resultSql, "@EndDateUnions", endDatesQuery.toString());
    }
    else
      resultSql = StringUtils.replace(resultSql, "@EndDateUnions", "");
    
    String codesetQuery = cohortExpressionQueryBuilder.getCodesetQuery(analysisExpression.conceptSets);
    resultSql = StringUtils.replace(resultSql, "@codesetQuery", codesetQuery);
    
    ArrayList<String> strataInsert = new ArrayList<>();
    for (int i = 0; i < analysisExpression.strata.size(); i++)
    {
      CriteriaGroup cg = analysisExpression.strata.get(i).expression;
      String stratumInsert = getStrataQuery(cg);
      stratumInsert = StringUtils.replace(stratumInsert, "@strata_sequence", "" +  i);
      strataInsert.add(stratumInsert);
    }
    
    resultSql = StringUtils.replace(resultSql,"@strataCohortInserts", StringUtils.join(strataInsert,"\n"));
    
    if (options != null)
    {
      // replease query parameters with tokens
      resultSql = StringUtils.replace(resultSql, "@cdm_database_schema", options.cdmSchema);
      resultSql = StringUtils.replace(resultSql, "@results_database_schema", options.resultsSchema);
    }
    
    resultSql = StringUtils.replace(resultSql, "@analysisId", analyisis.getId().toString());

    return resultSql;
  }
 }
