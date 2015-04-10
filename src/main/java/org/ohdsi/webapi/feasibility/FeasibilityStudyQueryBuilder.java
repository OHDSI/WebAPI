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
package org.ohdsi.webapi.feasibility;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.cohortdefinition.CohortExpression;
import org.ohdsi.webapi.cohortdefinition.CohortExpressionQueryBuilder;
import org.ohdsi.webapi.cohortdefinition.CriteriaGroup;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.service.FeasibilityService;
import org.ohdsi.webapi.vocabulary.ConceptSetExpressionQueryBuilder;

/**
 *
 * @author Chris Knoll <cknoll@ohdsi.org>
 */
public class FeasibilityStudyQueryBuilder {
  
  private final static ConceptSetExpressionQueryBuilder conceptSetQueryBuilder = new ConceptSetExpressionQueryBuilder();
  private final static CohortExpressionQueryBuilder cohortExpressionQueryBuilder = new CohortExpressionQueryBuilder();
  
  private final static String PERFORM_FEASIBILITY_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/feasibility/sql/performFeasibilityStudy.sql"); 
  private final static String INDEX_COHORT_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/feasibility/sql/indexCohort.sql"); 
  private final static String INCLUSION_RULE_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/feasibility/sql/inclusionrule.sql");  
  
  public static class BuildExpressionQueryOptions {
    @JsonProperty("cdmSchema")  
    public String cdmSchema;

    @JsonProperty("cohortTable")  
    public String cohortTable;
  } 
  
  private String getInclusionRuleQuery(CriteriaGroup inclusionRule)
  {
    String resultSql = INCLUSION_RULE_QUERY_TEMPLATE;
    String additionalCriteriaQuery = "\nINTERSECT\n" + cohortExpressionQueryBuilder.visit(inclusionRule);
    resultSql = StringUtils.replace(resultSql, "@additionalCriteriaQuery", additionalCriteriaQuery);
    return resultSql;
  }
  
  public String buildSimulateQuery(FeasibilityStudy study, BuildExpressionQueryOptions options) {
    String resultSql = PERFORM_FEASIBILITY_QUERY_TEMPLATE;
    CohortExpression indexRule;
    ArrayList<CriteriaGroup> inclusionRules = new ArrayList<>();

    try
    {
      ObjectMapper mapper = new ObjectMapper();
      indexRule = mapper.readValue(study.getIndexRule().getDetails().getExpression(), CohortExpression.class);
      for (InclusionRule inclusionRule : study.getInclusionRules())
      {
        inclusionRules.add(mapper.readValue(inclusionRule.getExpression(), CriteriaGroup.class));
      }
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
    
    // everything deserialized successfully
    
    String codesetQuery = cohortExpressionQueryBuilder.getCodesetQuery(indexRule.conceptSets);
    resultSql = StringUtils.replace(resultSql, "@codesetQuery", codesetQuery);
    
    String indexCohortQuery = INDEX_COHORT_QUERY_TEMPLATE;
    indexCohortQuery = StringUtils.replace(indexCohortQuery, "@indexCohortId", "" + study.getIndexRule().getId());
    resultSql = StringUtils.replace(resultSql, "@indexCohortQuery", indexCohortQuery);
    
    ArrayList<String> inclusionRuleInserts = new ArrayList<>();
    for (int i = 0; i < inclusionRules.size(); i++)
    {
      CriteriaGroup cg = inclusionRules.get(i);
      String inclusionRuleInsert = getInclusionRuleQuery(cg);
      inclusionRuleInsert = StringUtils.replace(inclusionRuleInsert, "@inclusion_rule_id", "" +  i);
      inclusionRuleInserts.add(inclusionRuleInsert);
    }
    
    resultSql = StringUtils.replace(resultSql,"@inclusionInserts", StringUtils.join(inclusionRuleInserts,"\n"));
    
    if (options != null)
    {
      // replease query parameters with tokens
      resultSql = StringUtils.replace(resultSql, "@CDM_schema", options.cdmSchema);
      resultSql = StringUtils.replace(resultSql, "@cohortTable", options.cohortTable);
    }
    
    resultSql = StringUtils.replace(resultSql, "@resultCohortId", study.getResultRule().getId().toString());
    resultSql = StringUtils.replace(resultSql, "@studyId", study.getId().toString());

    return resultSql;
  }
}
