/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.webapi.helper.ResourceHelper;
import org.ohdsi.webapi.vocabulary.Concept;
import org.ohdsi.webapi.vocabulary.ConceptSetExpressionQueryBuilder;

/**
 *
 * @author cknoll1
 */
public class CohortExpressionQueryBuilder implements IGetCriteriaSqlDispatcher, IGetEndStrategySqlDispatcher {

  private final static ConceptSetExpressionQueryBuilder conceptSetQueryBuilder = new ConceptSetExpressionQueryBuilder();
  private final static String CODESET_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/codesetQuery.sql");
  
  private final static String COHORT_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/generateCohort.sql");

  private final static String PRIMARY_EVENTS_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/primaryEventsQuery.sql");

  private final static String ADDITIONAL_CRITERIA_TEMMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/additionalCriteria.sql");
  private final static String GROUP_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/groupQuery.sql");
  
  private final static String CONDITION_ERA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/conditionEra.sql");
  private final static String CONDITION_OCCURRENCE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/conditionOccurrence.sql");
  private final static String DEATH_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/death.sql");
  private final static String DEVICE_EXPOSURE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/deviceExposure.sql");
  private final static String DOSE_ERA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/doseEra.sql");
  private final static String DRUG_ERA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/drugEra.sql");
  private final static String DRUG_EXPOSURE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/drugExposure.sql");
  private final static String MEASUREMENT_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/measurement.sql");;
  private final static String OBSERVATION_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/observation.sql");;
  private final static String OBSERVATION_PERIOD_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/observationPeriod.sql");;
  private final static String PROCEDURE_OCCURRENCE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/procedureOccurrence.sql");
  private final static String SPECIMEN_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/specimen.sql");
  private final static String VISIT_OCCURRENCE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/visitOccurrence.sql");
  private final static String PRIMARY_CRITERIA_EVENTS_TABLE = "#primary_events";
  private final static String INCLUSION_RULE_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/inclusionrule.sql");  

  private final static String DEMOGRAPHIC_CRITERIA_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/demographicCriteria.sql");
  
  // Strategy templates
  private final static String DATE_OFFSET_STRATEGY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/dateOffsetStrategy.sql");
  private final static String CUSTOM_ERA_STRATEGY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/customEraStrategy.sql");
  
  public static class BuildExpressionQueryOptions {
    @JsonProperty("cohortId")  
    public Integer cohortId;

    @JsonProperty("cdmSchema")  
    public String cdmSchema;

    @JsonProperty("targetTable")  
    public String targetTable;
    
    @JsonProperty("resultSchema")
    public String resultSchema;
    
    @JsonProperty("generateStats")
    public boolean generateStats;
  }  
  
  private ArrayList<Long> getConceptIdsFromConcepts(Concept[] concepts) {
    ArrayList<Long> conceptIdList = new ArrayList<>();
    for (Concept concept : concepts) {
      conceptIdList.add(concept.conceptId);
    }
    return conceptIdList;
  }

  private String getOperator(String op)
  {
    switch(op)
    {
      case "lt": return "<";
      case "lte" : return "<=";
      case "eq": return "=";
      case "!eq": return "<>";
      case "gt": return ">";
      case "gte": return ">=";
    }
    throw new RuntimeException("Unknown operator type: " + op);
  }
  
  private String getOccurrenceOperator(int type)
  {
    // Occurance check { id: 0, name: 'Exactly', id: 1, name: 'At Most' }, { id: 2, name: 'At Least' }
    switch (type)
    {
      case 0: return "=";
      case 1: return "<=";
      case 2: return ">=";
    }

    // recieved an unknown operator value
    return "??";
  }
  
  private String getOperator(DateRange range)
  {
    return getOperator(range.op);
  }
  
  private String getOperator(NumericRange range)
  {
    return getOperator(range.op);
  }
  
  private String dateStringToSql(String date)
  {
    String[] dateParts = StringUtils.split(date,'-');
    return String.format("DATEFROMPARTS(%s, %s, %s)", dateParts[0], dateParts[1], dateParts[2]);
  }
  
  private String buildDateRangeClause(String sqlExpression, DateRange range)
  {
    String clause;
    if (range.op.endsWith("bt")) // range with a 'between' op
    {
      clause = String.format("%s(%s >= %s and %s <= %s)",
          range.op.startsWith("!") ? "not " : "",
          sqlExpression,
          dateStringToSql(range.value),
          sqlExpression,
          dateStringToSql(range.extent));
    }
    else // single value range (less than/eq/greater than, etc)
    {
      clause = String.format("%s %s %s", sqlExpression, getOperator(range), dateStringToSql(range.value));
    }
    return clause;
  }
  
  // Assumes integer numeric range
  private String buildNumericRangeClause(String sqlExpression, NumericRange range)
  {
    String clause;
    if (range.op.endsWith("bt"))
    {
      clause = String.format("%s(%s >= %d and %s <= %d)",
        range.op.startsWith("!") ? "not " : "",
        sqlExpression,
        range.value.intValue(),
        sqlExpression,
        range.extent.intValue());
    }
    else
    {
      clause = String.format("%s %s %d", sqlExpression, getOperator(range), range.value.intValue());
    }
    return clause;
  }
 
  // assumes decimal range
  private String buildNumericRangeClause(String sqlExpression, NumericRange range, String format)
  {
    String clause;
    if (range.op.endsWith("bt"))
    {
      clause = String.format("%s(%s >= %" + format + " and %s <= %" + format + ")",
        range.op.startsWith("!") ? "not " : "",
        sqlExpression,
        range.value.doubleValue(),
        sqlExpression,
        range.extent.doubleValue());
    }
    else
    {
      clause = String.format("%s %s %" + format, sqlExpression, getOperator(range), range.value.doubleValue());
    }
    return clause;
  }

  
  private String buildTextFilterClause(String sqlExpression, TextFilter filter)
  {
      String negation = filter.op.startsWith("!") ? "not" : "";
      String prefix = filter.op.endsWith("endsWith") || filter.op.endsWith("contains") ? "%" : "";
      String value = filter.text;
      String postfix = filter.op.endsWith("startsWith") || filter.op.endsWith("contains") ? "%" : "";
      
      return String.format("%s %s like '%s%s%s'", sqlExpression, negation, prefix, value, postfix);
  }
  
  public String getCodesetQuery(ConceptSet[] conceptSets) {
    String codesetQuery = CODESET_QUERY_TEMPLATE;
    ArrayList<String> codesetInserts = new ArrayList<>();
    
    if (conceptSets.length > 0) {
      for (ConceptSet cs : conceptSets) {
        // construct main target codeset query
        String conceptExpressionQuery = conceptSetQueryBuilder.buildExpressionQuery(cs.expression);
        // attach the conceptSetId to the result query from the expession query builder
        String conceptSetInsert = String.format("INSERT INTO #Codesets (codeset_id, concept_id)\nSELECT %d as codeset_id, c.concept_id FROM (%s) C;", cs.id, conceptExpressionQuery);
        codesetInserts.add(conceptSetInsert);
      }
    }

    codesetQuery = StringUtils.replace(codesetQuery, "@codesetInserts", StringUtils.join(codesetInserts, "\n"));
    return codesetQuery;
  }
 
  public String getPrimaryEventsQuery(PrimaryCriteria primaryCriteria) {
    String query = PRIMARY_EVENTS_TEMPLATE;
    
    ArrayList<String> criteriaQueries = new ArrayList<>();
    
    for (Criteria c : primaryCriteria.criteriaList)
    {
      criteriaQueries.add(c.accept(this));
    }
    
    query = StringUtils.replace(query,"@criteriaQueries", StringUtils.join(criteriaQueries, "\nUNION\n"));
    
    ArrayList<String> primaryEventsFilters = new ArrayList<>();
    primaryEventsFilters.add(String.format(
        "DATEADD(day,%d,OP.OBSERVATION_PERIOD_START_DATE) <= P.START_DATE AND DATEADD(day,%d,P.START_DATE) <= OP.OBSERVATION_PERIOD_END_DATE",
        primaryCriteria.observationWindow.priorDays,
        primaryCriteria.observationWindow.postDays
      )
    );
    
    query = StringUtils.replace(query, "@EventSort", (primaryCriteria.primaryLimit.type != null && primaryCriteria.primaryLimit.type.equalsIgnoreCase("LAST")) ? "DESC" : "ASC");
    
    if (!primaryCriteria.primaryLimit.type.equalsIgnoreCase("ALL"))
    {
      primaryEventsFilters.add("P.ordinal = 1");
    }
    query = StringUtils.replace(query,"@primaryEventsFilter", StringUtils.join(primaryEventsFilters," AND "));
    
    return query;
  }
  
  public String buildExpressionQuery(CohortExpression expression, BuildExpressionQueryOptions options) {
    String resultSql = COHORT_QUERY_TEMPLATE;

    String codesetQuery = getCodesetQuery(expression.conceptSets);
    resultSql = StringUtils.replace(resultSql, "@codesetQuery", codesetQuery);

    String primaryEventsQuery = getPrimaryEventsQuery(expression.primaryCriteria);
    resultSql = StringUtils.replace(resultSql, "@primaryEventsQuery", primaryEventsQuery);
    
    String additionalCriteriaQuery = "";
    if (expression.additionalCriteria != null)
    {
      CriteriaGroup acGroup = expression.additionalCriteria;
      String acGroupQuery = this.getCriteriaGroupQuery(acGroup, this.PRIMARY_CRITERIA_EVENTS_TABLE);//acGroup.accept(this);
      acGroupQuery = StringUtils.replace(acGroupQuery,"@indexId", "" + 0);
      additionalCriteriaQuery = "\nJOIN (\n" + acGroupQuery + ") AC on AC.event_id = pe.event_id\n";
    }
    resultSql = StringUtils.replace(resultSql, "@additionalCriteriaQuery", additionalCriteriaQuery);

    resultSql = StringUtils.replace(resultSql, "@QualifiedEventSort", (expression.qualifiedLimit.type != null && expression.qualifiedLimit.type.equalsIgnoreCase("LAST")) ? "DESC" : "ASC");

    // Only apply qualified limit filter if additional criteria is specified.
    if (expression.additionalCriteria != null && expression.qualifiedLimit.type != null && !expression.qualifiedLimit.type.equalsIgnoreCase("ALL"))
    {
      resultSql = StringUtils.replace(resultSql, "@QualifiedLimitFilter","WHERE QE.ordinal = 1");
    }
    else
      resultSql = StringUtils.replace(resultSql, "@QualifiedLimitFilter","");    
    
    ArrayList<String> inclusionRuleInserts = new ArrayList<>();
    for (int i = 0; i < expression.inclusionRules.size(); i++)
    {
      CriteriaGroup cg = expression.inclusionRules.get(i).expression;
      String inclusionRuleInsert = getInclusionRuleQuery(cg);
      inclusionRuleInsert = StringUtils.replace(inclusionRuleInsert, "@inclusion_rule_id", "" +  i);
      inclusionRuleInserts.add(inclusionRuleInsert);
    }
    
    resultSql = StringUtils.replace(resultSql,"@inclusionCohortInserts", StringUtils.join(inclusionRuleInserts,"\n"));

    resultSql = StringUtils.replace(resultSql, "@IncludedEventSort", (expression.expressionLimit.type != null && expression.expressionLimit.type.equalsIgnoreCase("LAST")) ? "DESC" : "ASC");

    if (expression.expressionLimit.type != null && !expression.expressionLimit.type.equalsIgnoreCase("ALL"))
    {
      resultSql = StringUtils.replace(resultSql, "@ResultLimitFilter","WHERE Results.ordinal = 1");
    }
    else
      resultSql = StringUtils.replace(resultSql, "@ResultLimitFilter","");
    
    resultSql = StringUtils.replace(resultSql, "@ruleTotal", String.valueOf(expression.inclusionRules.size()));

    if (expression.endStrategy != null)
      resultSql = StringUtils.replace(resultSql, "@strategyInserts", expression.endStrategy.accept(this, "#included_events"));
    else
      resultSql = StringUtils.replace(resultSql, "@strategyInserts", "");
    
    if (options != null)
    {
      // replease query parameters with tokens
      if (options.cdmSchema != null)
        resultSql = StringUtils.replace(resultSql, "@cdm_database_schema", options.cdmSchema);
      if (options.targetTable != null)
        resultSql = StringUtils.replace(resultSql, "@target_database_schema.@target_cohort_table", options.targetTable);
      if (options.resultSchema != null)
        resultSql = StringUtils.replace(resultSql, "@results_database_schema", options.resultSchema);
      if (options.cohortId != null)
        resultSql = StringUtils.replace(resultSql, "@target_cohort_id", options.cohortId.toString());
       resultSql = StringUtils.replace(resultSql, "@generateStats", options.generateStats ? "1": "0");
    }
    return resultSql;
  }
  public String getCriteriaGroupQuery(CriteriaGroup group, String eventTable) {
    String query = GROUP_QUERY_TEMPLATE;
    ArrayList<String> additionalCriteriaQueries = new ArrayList<>();
    
    int indexId = 0;
    for(CorelatedCriteria cc : group.criteriaList)
    {
      String acQuery = this.getCorelatedlCriteriaQuery(cc, eventTable); //ac.accept(this);
      acQuery = StringUtils.replace(acQuery, "@indexId", "" + indexId);
      additionalCriteriaQueries.add(acQuery);
      indexId++;
    }
    
    for(DemographicCriteria dc : group.demographicCriteriaList)
    {
      String dcQuery = this.getDemographicCriteriaQuery(dc, eventTable); //ac.accept(this);
      dcQuery = StringUtils.replace(dcQuery, "@indexId", "" + indexId);
      additionalCriteriaQueries.add(dcQuery);
      indexId++;
    } 
    
    for(CriteriaGroup g : group.groups)
    {
      String gQuery = this.getCriteriaGroupQuery(g, eventTable); //g.accept(this);
      gQuery = StringUtils.replace(gQuery, "@indexId", "" + indexId);
      additionalCriteriaQueries.add(gQuery);  
      indexId++;
    }
    
    String intersectClause = "HAVING COUNT(index_id) ";
    
    if (group.type.equalsIgnoreCase("ALL")) // count must match number of criteria + sub-groups in group.
      intersectClause += "= " + indexId;
    
    if (group.type.equalsIgnoreCase("ANY")) // count must be > 0 for an 'ANY' criteria
      intersectClause += "> 0"; 
    
    if (group.type.toUpperCase().startsWith("AT_"))
    {
      if (group.type.toUpperCase().endsWith("LEAST"))
        intersectClause += ">= " + group.count;
      else
        intersectClause += "<= " + group.count;
    }
           
    query = StringUtils.replace(query, "@intersectClause", intersectClause);
    query = StringUtils.replace(query, "@criteriaQueries", StringUtils.join(additionalCriteriaQueries, "\nUNION\n"));
    
    return query;    
  }
  
  private String getInclusionRuleQuery(CriteriaGroup inclusionRule)
  {
    String resultSql = INCLUSION_RULE_QUERY_TEMPLATE;
    String additionalCriteriaQuery = "\nJOIN (\n" + getCriteriaGroupQuery(inclusionRule, "#qualified_events") + ") AC on AC.event_id = pe.event_id";
    additionalCriteriaQuery = StringUtils.replace(additionalCriteriaQuery,"@indexId", "" + 0);
    resultSql = StringUtils.replace(resultSql, "@additionalCriteriaQuery", additionalCriteriaQuery);
    return resultSql;
  }
  
  public String getDemographicCriteriaQuery(DemographicCriteria criteria, String eventTable)
  {
    String query = DEMOGRAPHIC_CRITERIA_QUERY_TEMPLATE;
    query = StringUtils.replace(query,"@eventTable",eventTable);
    
    ArrayList<String> whereClauses = new ArrayList<>();

    // Age
    if (criteria.age != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(E.start_date) - P.year_of_birth", criteria.age));
    }

    // Gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    // Race
    if (criteria.race != null && criteria.race.length > 0)
    {
      whereClauses.add(String.format("P.race_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.race),",")));
    }

    // Race
    if (criteria.race != null && criteria.race.length > 0)
    {
      whereClauses.add(String.format("P.race_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.race),",")));
    }

    // Ethnicity
    if (criteria.ethnicity != null && criteria.ethnicity.length > 0)
    {
      whereClauses.add(String.format("P.ethnicity_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.ethnicity),",")));
    }
    
    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("E.start_date",criteria.occurrenceStartDate));
    }

    // occurrenceEndDate
    if (criteria.occurrenceEndDate != null)
    {
      whereClauses.add(buildDateRangeClause("E.end_date",criteria.occurrenceEndDate));
    }

    if (whereClauses.size() > 0) {
      query = StringUtils.replace(query, "@whereClause", "WHERE " + StringUtils.join(whereClauses, " AND "));
    } else {
      query = StringUtils.replace(query, "@whereClause", "");
    }
    
    return query;
  }
  
  public String getCorelatedlCriteriaQuery(CorelatedCriteria corelatedCriteria, String eventTable)
  {
    String query = ADDITIONAL_CRITERIA_TEMMPLATE;
    
    String criteriaQuery = corelatedCriteria.criteria.accept(this);
    query = StringUtils.replace(query,"@criteriaQuery",criteriaQuery);
    query = StringUtils.replace(query,"@eventTable",eventTable);
    
    // build index date window expression
    String startExpression;
    String endExpression;
    ArrayList<String> clauses = new ArrayList<>();
    clauses.add("A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE");
    
    // StartWindow
    Window startWindow = corelatedCriteria.startWindow;
    if (startWindow.start.days != null)
      startExpression = String.format("DATEADD(day,%d,P.START_DATE)", startWindow.start.coeff * startWindow.start.days);
    else
      startExpression = startWindow.start.coeff == -1 ? "P.OP_START_DATE" : "P.OP_END_DATE";

    if (startWindow.end.days != null)
      endExpression = String.format("DATEADD(day,%d,P.START_DATE)", startWindow.end.coeff * startWindow.end.days);
    else
      endExpression = startWindow.end.coeff == -1 ? "P.OP_START_DATE" : "P.OP_END_DATE";
    
    clauses.add(String.format("A.START_DATE >= %s and A.START_DATE <= %s", startExpression, endExpression));
    
    // EndWindow
    Window endWindow = corelatedCriteria.endWindow;

    if (endWindow != null)
    {
      if (endWindow.start.days != null)
        startExpression = String.format("DATEADD(day,%d,P.START_DATE)", endWindow.start.coeff * endWindow.start.days);
      else
        startExpression = endWindow.start.coeff == -1 ? "P.OP_START_DATE" : "P.OP_END_DATE";

      if (endWindow.end.days != null)
        endExpression = String.format("DATEADD(day,%d,P.START_DATE)", endWindow.end.coeff * endWindow.end.days);
      else
        endExpression = endWindow.end.coeff == -1 ? "P.OP_START_DATE" : "P.OP_END_DATE";

      clauses.add(String.format("A.END_DATE >= %s AND A.END_DATE <= %s", startExpression, endExpression));    
    }
    query = StringUtils.replace(query,"@windowCriteria",StringUtils.join(clauses, " AND "));

    // Occurrence criteria
    String occurrenceCriteria = String.format(
      "HAVING COUNT(%sA.TARGET_CONCEPT_ID) %s %d",
      corelatedCriteria.occurrence.isDistinct ? "DISTINCT " : "",
      getOccurrenceOperator(corelatedCriteria.occurrence.type), 
      corelatedCriteria.occurrence.count
    );
    
    query = StringUtils.replace(query, "@occurrenceCriteria", occurrenceCriteria);

    return query;
  }

// <editor-fold defaultstate="collapsed" desc="ICriteriaSqlDispatcher implementation">
  
  @Override
  public String getCriteriaSql(ConditionEra criteria)
  {
    String query = CONDITION_ERA_TEMPLATE;
    
    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where ce.condition_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);
    
    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.ageAtStart != null || criteria.ageAtEnd != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");

    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    ArrayList<String> whereClauses = new ArrayList<>();
    
    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");

    // eraStartDate
    if (criteria.eraStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.condition_era_start_date",criteria.eraStartDate));
    }

    // eraEndDate
    if (criteria.eraEndDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.condition_era_end_date",criteria.eraEndDate));
    }
    
    // occurrenceCount
    if (criteria.occurrenceCount != null)
    {
      whereClauses.add(buildNumericRangeClause("C.condition_occurrence_count", criteria.occurrenceCount));
    }      

    // eraLength
    if (criteria.eraLength != null)
    {
      whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.condition_era_start_date, C.condition_era_end_date)", criteria.eraLength));
    }      

    // ageAtStart
    if (criteria.ageAtStart != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.condition_era_start_date) - P.year_of_birth", criteria.ageAtStart));
    }

    // ageAtEnd
    if (criteria.ageAtEnd != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.condition_era_end_date) - P.year_of_birth", criteria.ageAtEnd));
    }

    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    return query;
  }

  @Override
  public String getCriteriaSql(ConditionOccurrence criteria)
  {
    String query = CONDITION_OCCURRENCE_TEMPLATE;
    
    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where co.condition_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);
    
    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    if (criteria.visitType != null && criteria.visitType.length > 0)
      joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
      joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    ArrayList<String> whereClauses = new ArrayList<>();
    
    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");

    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.condition_start_date",criteria.occurrenceStartDate));
    }

    // occurrenceEndDate
    if (criteria.occurrenceEndDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.condition_end_date",criteria.occurrenceEndDate));
    }
    
    // conditionType
    if (criteria.conditionType != null && criteria.conditionType.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.conditionType);
      whereClauses.add(String.format("C.condition_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
    
    // Stop Reason
    if (criteria.stopReason != null)
    {
      whereClauses.add(buildTextFilterClause("C.stop_reason",criteria.stopReason));
    }
    
    // conditionSourceConcept
    if (criteria.conditionSourceConcept != null)
    {
      whereClauses.add(String.format("C.condition_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.conditionSourceConcept));
    }
    
    // age
    if (criteria.age != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.condition_start_date) - P.year_of_birth", criteria.age));
    }
    
    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
    {
      whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.providerSpecialty),",")));
    }
    
    // visitType
    if (criteria.visitType != null && criteria.visitType.length > 0)
    {
      whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.visitType),",")));
    }

    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    return query;
  }
    
  @Override
  public String getCriteriaSql(Death criteria)
  {
    String query = DEATH_TEMPLATE;

    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where d.cause_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);
    
    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    ArrayList<String> whereClauses = new ArrayList<>();
   
    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.death_date",criteria.occurrenceStartDate));
    }

    // deathType
    if (criteria.deathType != null && criteria.deathType.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.deathType);
      whereClauses.add(String.format("C.death_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // deathSourceConcept
    if (criteria.deathSourceConcept != null)
    {
      whereClauses.add(String.format("C.cause_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.deathSourceConcept));
    }    
    
    // age
    if (criteria.age != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.death_date) - P.year_of_birth", criteria.age));
    }
    
    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    return query;
  }
    
  @Override
  public String getCriteriaSql(DeviceExposure criteria)
  {
    String query = DEVICE_EXPOSURE_TEMPLATE;

    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where de.device_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);

    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    if (criteria.visitType != null && criteria.visitType.length > 0)
      joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
      joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    ArrayList<String> whereClauses = new ArrayList<>();
    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");
    
    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.device_exposure_start_date",criteria.occurrenceStartDate));
    }

    // occurrenceEndDate
    if (criteria.occurrenceEndDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.device_exposure_end_date",criteria.occurrenceEndDate));
    }

    // deviceType
    if (criteria.deviceType != null && criteria.deviceType.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.deviceType);
      whereClauses.add(String.format("C.device_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
    
    // uniqueDeviceId
    if (criteria.uniqueDeviceId != null)
    {
      whereClauses.add(buildTextFilterClause("C.unique_device_id",criteria.uniqueDeviceId));
    }

    // quantity
    if (criteria.quantity != null)
    {
      whereClauses.add(buildNumericRangeClause("C.quantity",criteria.quantity));
    }

    // deviceSourceConcept
    if (criteria.deviceSourceConcept != null)
    {
      whereClauses.add(String.format("C.device_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.deviceSourceConcept));
    }    
    
    // age
    if (criteria.age != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.device_exposure_start_date) - P.year_of_birth", criteria.age));
    }
    
    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
    {
      whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.providerSpecialty),",")));
    }
    
    // visitType
    if (criteria.visitType != null && criteria.visitType.length > 0)
    {
      whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.visitType),",")));
    }
    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    
    return query;
  }

  
  @Override
  public String getCriteriaSql(DoseEra criteria)
  {
    String query = DOSE_ERA_TEMPLATE;
    
    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where de.drug_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);

    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.ageAtStart != null || criteria.ageAtEnd != null) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    ArrayList<String> whereClauses = new ArrayList<>();

    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");

    // eraStartDate
    if (criteria.eraStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.dose_era_start_date",criteria.eraStartDate));
    }

    // eraEndDate
    if (criteria.eraEndDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.dose_era_end_date",criteria.eraEndDate));
    }

    // unit
    if (criteria.unit != null && criteria.unit.length > 0)
    {
      whereClauses.add(String.format("c.unit_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.unit),",")));
    }
    
    // doseValue
    if (criteria.doseValue != null)
    {
      whereClauses.add(buildNumericRangeClause("c.dose_value", criteria.doseValue, ".4f"));
    }      

    // eraLength
    if (criteria.eraLength != null)
    {
      whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.dose_era_start_date, C.dose_era_end_date)", criteria.eraLength));
    }      

    // ageAtStart
    if (criteria.ageAtStart != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.dose_era_start_date) - P.year_of_birth", criteria.ageAtStart));
    }

    // ageAtEnd
    if (criteria.ageAtEnd != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.dose_era_end_date) - P.year_of_birth", criteria.ageAtEnd));
    }

    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    return query;
  }
    
  @Override
  public String getCriteriaSql(DrugEra criteria)
  {
    String query = DRUG_ERA_TEMPLATE;
    
    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where de.drug_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);
    
    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.ageAtStart != null || criteria.ageAtEnd != null) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    ArrayList<String> whereClauses = new ArrayList<>();
    
    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");

    // eraStartDate
    if (criteria.eraStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.drug_era_start_date",criteria.eraStartDate));
    }

    // eraEndDate
    if (criteria.eraEndDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.drug_era_end_date",criteria.eraEndDate));
    }
    
    // occurrenceCount
    if (criteria.occurrenceCount != null)
    {
      whereClauses.add(buildNumericRangeClause("C.drug_exposure_count", criteria.occurrenceCount));
    }      

    // eraLength
    if (criteria.eraLength != null)
    {
      whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.drug_era_start_date, C.drug_era_end_date)", criteria.eraLength));
    }      

    // gapDays
    if (criteria.gapDays != null)
    {
      whereClauses.add(buildNumericRangeClause("C.gap_days", criteria.eraLength));
    }      
    
    // ageAtStart
    if (criteria.ageAtStart != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.drug_era_start_date) - P.year_of_birth", criteria.ageAtStart));
    }

    // ageAtEnd
    if (criteria.ageAtEnd != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.drug_era_end_date) - P.year_of_birth", criteria.ageAtEnd));
    }

    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    return query;
  }
  
  @Override
  public String getCriteriaSql(DrugExposure criteria)
  {
    String query = DRUG_EXPOSURE_TEMPLATE;

    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where de.drug_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);

    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    if (criteria.visitType != null && criteria.visitType.length > 0)
      joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
      joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    ArrayList<String> whereClauses = new ArrayList<>();
    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");
    
    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.drug_exposure_start_date",criteria.occurrenceStartDate));
    }

    // occurrenceEndDate
    if (criteria.occurrenceEndDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.drug_exposure_end_date",criteria.occurrenceEndDate));
    }

    // drugType
    if (criteria.drugType != null && criteria.drugType.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.drugType);
      whereClauses.add(String.format("C.drug_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
    
    // Stop Reason
    if (criteria.stopReason != null)
    {
      whereClauses.add(buildTextFilterClause("C.stop_reason",criteria.stopReason));
    }

    // refills
    if (criteria.refills != null)
    {
      whereClauses.add(buildNumericRangeClause("C.refills",criteria.refills));
    }

    // quantity
    if (criteria.quantity != null)
    {
      whereClauses.add(buildNumericRangeClause("C.quantity",criteria.quantity,".4f"));
    }

    // days supply
    if (criteria.daysSupply != null)
    {
      whereClauses.add(buildNumericRangeClause("C.days_supply",criteria.daysSupply));
    }

    // routeConcept
    if (criteria.routeConcept != null && criteria.routeConcept.length > 0)
    {
      whereClauses.add(String.format("C.route_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.routeConcept),",")));
    }
    
    // effectiveDrugDose
    if (criteria.effectiveDrugDose != null)
    {
      whereClauses.add(buildNumericRangeClause("C.effective_drug_dose",criteria.effectiveDrugDose,".4f"));
    }

    // doseUnit
    if (criteria.doseUnit != null && criteria.doseUnit.length > 0)
    {
      whereClauses.add(String.format("C.dose_unit_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.doseUnit),",")));
    }

    // LotNumber
    if (criteria.lotNumber != null)
    {
      whereClauses.add(buildTextFilterClause("C.lot_number", criteria.lotNumber));
    }
    
    // drugSourceConcept
    if (criteria.drugSourceConcept != null)
    {
      whereClauses.add(String.format("C.drug_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.drugSourceConcept));
    }    
    
    // age
    if (criteria.age != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.drug_exposure_start_date) - P.year_of_birth", criteria.age));
    }
    
    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
    {
      whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.providerSpecialty),",")));
    }
    
    // visitType
    if (criteria.visitType != null && criteria.visitType.length > 0)
    {
      whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.visitType),",")));
    }
    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    
    return query;
  }  
  
  @Override
  public String getCriteriaSql(Measurement criteria)
  {
    String query = MEASUREMENT_TEMPLATE;
    
    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where m.measurement_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);
    
    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    if (criteria.visitType != null && criteria.visitType.length > 0)
      joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
      joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));

    ArrayList<String> whereClauses = new ArrayList<>();
  
    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");

    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.measurement_date",criteria.occurrenceStartDate));
    }        
  
    // measurementType
    if (criteria.measurementType != null && criteria.measurementType.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.measurementType);
      whereClauses.add(String.format("C.measurement_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
    
    // operator
    if (criteria.operator != null && criteria.operator.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.operator);
      whereClauses.add(String.format("C.operator_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
    
    // valueAsNumber
    if (criteria.valueAsNumber != null)
    {
      whereClauses.add(buildNumericRangeClause("C.value_as_number",criteria.valueAsNumber,".4f"));
    }
    
    // valueAsConcept
    if (criteria.valueAsConcept != null && criteria.valueAsConcept.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.valueAsConcept);
      whereClauses.add(String.format("C.value_as_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }    
    
    // unit
    if (criteria.unit != null && criteria.unit.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.unit);
      whereClauses.add(String.format("C.unit_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
    
    // rangeLow
    if (criteria.rangeLow != null)
    {
      whereClauses.add(buildNumericRangeClause("C.range_low",criteria.rangeLow,".4f"));
    }

    // rangeHigh
    if (criteria.rangeHigh != null)
    {
      whereClauses.add(buildNumericRangeClause("C.range_high",criteria.rangeHigh,".4f"));
    }
    
    // rangeLowRatio
    if (criteria.rangeLowRatio != null)
    {
      whereClauses.add(buildNumericRangeClause("(C.value_as_number / C.range_low)",criteria.rangeLowRatio,".4f"));
    }

    // rangeHighRatio
    if (criteria.rangeHighRatio != null)
    {
      whereClauses.add(buildNumericRangeClause("(C.value_as_number / C.range_high)",criteria.rangeHighRatio,".4f"));
    }
    
    // abnormal
    if (criteria.abnormal != null && criteria.abnormal.booleanValue())
    {
      whereClauses.add("(C.value_as_number < C.range_low or C.value_as_number > C.range_high or C.value_as_concept_id in (4155142, 4155143))");
    }
    
    // measurementSourceConcept
    if (criteria.measurementSourceConcept != null)
    {
      whereClauses.add(String.format("C.measurement_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.measurementSourceConcept));
    }
    
    // age
    if (criteria.age != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.measurement_date) - P.year_of_birth", criteria.age));
    }
    
    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
    {
      whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.providerSpecialty),",")));
    }
    
    // visitType
    if (criteria.visitType != null && criteria.visitType.length > 0)
    {
      whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.visitType),",")));
    }
    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    
    return query;
  }
  
  @Override
  public String getCriteriaSql(Observation criteria)
  {
    String query = OBSERVATION_TEMPLATE;
    
    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where o.observation_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);

    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    if (criteria.visitType != null && criteria.visitType.length > 0)
      joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
      joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    ArrayList<String> whereClauses = new ArrayList<>();
  
    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");

    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.observation_date",criteria.occurrenceStartDate));
    }        
  
    // measurementType
    if (criteria.observationType != null && criteria.observationType.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.observationType);
      whereClauses.add(String.format("C.observation_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
       
    // valueAsNumber
    if (criteria.valueAsNumber != null)
    {
      whereClauses.add(buildNumericRangeClause("C.value_as_number",criteria.valueAsNumber,".4f"));
    }
    
    // valueAsString
    if (criteria.valueAsString != null)
    {
      whereClauses.add(buildTextFilterClause("C.value_as_string",criteria.valueAsString));
    }

    // valueAsConcept
    if (criteria.valueAsConcept != null && criteria.valueAsConcept.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.valueAsConcept);
      whereClauses.add(String.format("C.value_as_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }    
    
    // qualifier
    if (criteria.qualifier != null && criteria.qualifier.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.qualifier);
      whereClauses.add(String.format("C.qualifier_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
    
    // unit
    if (criteria.unit != null && criteria.unit.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.unit);
      whereClauses.add(String.format("C.unit_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
       
    // observationSourceConcept
    if (criteria.observationSourceConcept != null)
    {
      whereClauses.add(String.format("C.observation_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.observationSourceConcept));
    }
    
    // age
    if (criteria.age != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.observation_date) - P.year_of_birth", criteria.age));
    }
    
    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
    {
      whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.providerSpecialty),",")));
    }
    
    // visitType
    if (criteria.visitType != null && criteria.visitType.length > 0)
    {
      whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.visitType),",")));
    }
    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    
    return query;
  }  

  @Override
  public String getCriteriaSql(ObservationPeriod criteria)
  {
    String query = OBSERVATION_PERIOD_TEMPLATE;

    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.ageAtStart != null || criteria.ageAtEnd != null) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));

    ArrayList<String> whereClauses = new ArrayList<>();

    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");
    
    // periodStartDate
    if (criteria.periodStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.observation_period_start_date",criteria.periodStartDate));
    }        

    // periodEndDate
    if (criteria.periodEndDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.observation_period_end_date",criteria.periodEndDate));
    }        
    
    // periodType
    if (criteria.periodType != null && criteria.periodType.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.periodType);
      whereClauses.add(String.format("C.period_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
    
    // periodLength
    if (criteria.periodLength != null)
    {
      whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.observation_period_start_date, C.observation_period_end_date)", criteria.periodLength));
    }      

    // ageAtStart
    if (criteria.ageAtStart != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.observation_period_start_date) - P.year_of_birth", criteria.ageAtStart));
    }

    // ageAtEnd
    if (criteria.ageAtEnd != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.observation_period_end_date) - P.year_of_birth", criteria.ageAtEnd));
    }
    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    
    return query;
  }

  @Override
  public String getCriteriaSql(ProcedureOccurrence criteria)
  {
    String query = PROCEDURE_OCCURRENCE_TEMPLATE;
    
    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where po.procedure_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);

    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    if (criteria.visitType != null && criteria.visitType.length > 0)
      joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
      joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    ArrayList<String> whereClauses = new ArrayList<>();
    
    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");

    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.procedure_date",criteria.occurrenceStartDate));
    }    
    
    // procedureType
    if (criteria.procedureType != null && criteria.procedureType.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.procedureType);
      whereClauses.add(String.format("C.procedure_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
    
    // modifier
    if (criteria.modifier != null && criteria.modifier.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.modifier);
      whereClauses.add(String.format("C.modifier_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // quantity
    if (criteria.quantity != null)
    {
      whereClauses.add(buildNumericRangeClause("C.quantity",criteria.quantity));
    }
    
    // procedureSourceConcept
    if (criteria.procedureSourceConcept != null)
    {
      whereClauses.add(String.format("C.procedure_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.procedureSourceConcept));
    }
    
    // age
    if (criteria.age != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.procedure_date) - P.year_of_birth", criteria.age));
    }
    
    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
    {
      whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.providerSpecialty),",")));
    }
    
    // visitType
    if (criteria.visitType != null && criteria.visitType.length > 0)
    {
      whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.visitType),",")));
    }

    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    
    return query;
  }
  
  @Override
  public String getCriteriaSql(Specimen criteria) 
  {
    String query = SPECIMEN_TEMPLATE;
    
    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where s.specimen_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);
    
    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    ArrayList<String> whereClauses = new ArrayList<>();
    
    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");

    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.specimen_date",criteria.occurrenceStartDate));
    }    
    
    // specimenType
    if (criteria.specimenType != null && criteria.specimenType.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.specimenType);
      whereClauses.add(String.format("C.specimen_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // quantity
    if (criteria.quantity != null)
    {
      whereClauses.add(buildNumericRangeClause("C.quantity", criteria.quantity, ".4f"));
    }

    // unit
    if (criteria.unit != null && criteria.unit.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.unit);
      whereClauses.add(String.format("C.unit_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // anatomicSite
    if (criteria.anatomicSite != null && criteria.anatomicSite.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.anatomicSite);
      whereClauses.add(String.format("C.anatomic_site_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // diseaseStatus
    if (criteria.diseaseStatus != null && criteria.diseaseStatus.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.diseaseStatus);
      whereClauses.add(String.format("C.disease_status_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }

    // sourceId
    if (criteria.sourceId != null)
    {
      whereClauses.add(buildTextFilterClause("C.specimen_source_id",criteria.sourceId));
    }

    // age
    if (criteria.age != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.specimen_date) - P.year_of_birth", criteria.age));
    }
    
    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }

    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    
    return query;
  }

  @Override
  public String getCriteriaSql(VisitOccurrence criteria) 
  {
    String query = VISIT_OCCURRENCE_TEMPLATE;
    
    String codesetClause = "";
    if (criteria.codesetId != null)
    {
      codesetClause = String.format("where vo.visit_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
    }
    query = StringUtils.replace(query, "@codesetClause",codesetClause);
    
    ArrayList<String> joinClauses = new ArrayList<>();
    
    if (criteria.age != null || (criteria.gender != null && criteria.gender.length > 0)) // join to PERSON
      joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
    if (criteria.placeOfService != null && criteria.placeOfService.length > 0)
      joinClauses.add("JOIN @cdm_database_schema.CARE_SITE CS on C.care_site_id = CS.care_site_id");
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
      joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
    query = StringUtils.replace(query,"@joinClause", StringUtils.join(joinClauses,"\n"));
    
    
    
    ArrayList<String> whereClauses = new ArrayList<>();

    // first
    if (criteria.first != null && criteria.first == true)
      whereClauses.add("C.ordinal = 1");

    // occurrenceStartDate
    if (criteria.occurrenceStartDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.visit_start_date",criteria.occurrenceStartDate));
    }

    // occurrenceEndDate
    if (criteria.occurrenceEndDate != null)
    {
      whereClauses.add(buildDateRangeClause("C.visit_end_date",criteria.occurrenceEndDate));
    }
    
    // visitType
    if (criteria.visitType != null && criteria.visitType.length > 0)
    {
      ArrayList<Long> conceptIds = getConceptIdsFromConcepts(criteria.visitType);
      whereClauses.add(String.format("C.visit_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
    }
    
    // visitSourceConcept
    if (criteria.visitSourceConcept != null)
    {
      whereClauses.add(String.format("C.visit_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.visitSourceConcept));
    }
    
    // visitLength
    if (criteria.visitLength != null)
    {
      whereClauses.add(buildNumericRangeClause("DATEDIFF(d,C.visit_start_date, C.visit_end_date)", criteria.visitLength));
    }

    // age
    if (criteria.age != null)
    {
      whereClauses.add(buildNumericRangeClause("YEAR(C.visit_start_date) - P.year_of_birth", criteria.age));
    }
    
    // gender
    if (criteria.gender != null && criteria.gender.length > 0)
    {
      whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender),",")));
    }
    
    // providerSpecialty
    if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0)
    {
      whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.providerSpecialty),",")));
    }

    // placeOfService
    if (criteria.placeOfService != null && criteria.placeOfService.length > 0)
    {
      whereClauses.add(String.format("CS.place_of_service_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.placeOfService),",")));
    }
    
    String whereClause = "";
    if (whereClauses.size() > 0)
      whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
    query = StringUtils.replace(query, "@whereClause",whereClause);
    
    
    return query;
  }
  
// </editor-fold>

// <editor-fold defaultstate="collapsed" desc="IEndStrategyDispatcher implementation">

  private String getDateFieldForOffsetStrategy (DateOffsetStrategy.DateField dateField) {
    switch (dateField) {
      case StartDate: 
        return "start_date";
      case EndDate:
        return "end_date";
    }
    return "start_date";
  }
  
  @Override
  public String getStrategySql(DateOffsetStrategy strat, String eventTable) 
  {
    String insertSql = "-- Date Offset Strategy\nINSERT INTO #cohort_ends (event_id,  person_id, end_date)\n@dateOffsetStrategySql;";
    
    String strategySql = StringUtils.replace(DATE_OFFSET_STRATEGY_TEMPLATE, "@eventTable",eventTable);
    strategySql = StringUtils.replace(strategySql, "@offset",Integer.toString(strat.offset));
    strategySql = StringUtils.replace(strategySql, "@dateField",getDateFieldForOffsetStrategy(strat.dateField));
   
    insertSql = StringUtils.replace(insertSql, "@dateOffsetStrategySql",strategySql);
    return insertSql;
  }

  @Override
  public String getStrategySql(CustomEraStrategy strat, String eventTable) 
  {
    if (strat.drugCodesetId == null)
      throw new RuntimeException("Drug Codeset ID can not be NULL.");
    
    String insertSql = "-- Custom Era Strategy\nINSERT INTO #cohort_ends (event_id,  person_id, end_date)\n@customEraStrategySql;";

    String strategySql = StringUtils.replace(CUSTOM_ERA_STRATEGY_TEMPLATE, "@eventTable",eventTable);
    strategySql = StringUtils.replace(strategySql, "@drugCodesetId",strat.drugCodesetId.toString());
    strategySql = StringUtils.replace(strategySql, "@gapDays",Integer.toString(strat.gapDays));
    strategySql = StringUtils.replace(strategySql, "@offset",Integer.toString(strat.offset));
    
    insertSql = StringUtils.replace(insertSql, "@customEraStrategySql",strategySql);
    
    return insertSql;    
  }
  
  
// </editor-fold>
  
}
