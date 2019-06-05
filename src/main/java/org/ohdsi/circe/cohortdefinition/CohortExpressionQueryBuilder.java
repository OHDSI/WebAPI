//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.DateOffsetStrategy.DateField;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.Concept;
import org.ohdsi.circe.vocabulary.ConceptSetExpressionQueryBuilder;

public class CohortExpressionQueryBuilder implements IGetCriteriaSqlDispatcher, IGetEndStrategySqlDispatcher {
    private static final ConceptSetExpressionQueryBuilder conceptSetQueryBuilder = new ConceptSetExpressionQueryBuilder();
    private static final String CODESET_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/codesetQuery.sql");
    private static final String COHORT_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/generateCohort.sql");
    private static final String PRIMARY_EVENTS_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/primaryEventsQuery.sql");
    private static final String WINDOWED_CRITERIA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/windowedCriteria.sql");
    private static final String ADDITIONAL_CRITERIA_TEMPLATE;
    private static final String GROUP_QUERY_TEMPLATE;
    private static final String CONDITION_ERA_TEMPLATE;
    private static final String CONDITION_OCCURRENCE_TEMPLATE;
    private static final String DEATH_TEMPLATE;
    private static final String DEVICE_EXPOSURE_TEMPLATE;
    private static final String DOSE_ERA_TEMPLATE;
    private static final String DRUG_ERA_TEMPLATE;
    private static final String DRUG_EXPOSURE_TEMPLATE;
    private static final String TREATMENT_LINE_TEMPLATE;
    private static final String MEASUREMENT_TEMPLATE;
    private static final String OBSERVATION_TEMPLATE;
    private static final String OBSERVATION_PERIOD_TEMPLATE;
    private static final String PROCEDURE_OCCURRENCE_TEMPLATE;
    private static final String SPECIMEN_TEMPLATE;
    private static final String VISIT_OCCURRENCE_TEMPLATE;
    private static final String PRIMARY_CRITERIA_EVENTS_TABLE = "primary_events";
    private static final String INCLUSION_RULE_QUERY_TEMPLATE;
    private static final String CENSORING_QUERY_TEMPLATE;
    private static final String PAYER_PLAN_PERIOD_TEMPLATE;
    private static final String EVENT_TABLE_EXPRESSION_TEMPLATE;
    private static final String DEMOGRAPHIC_CRITERIA_QUERY_TEMPLATE;
    private static final String CODESET_JOIN_TEMPLATE = "JOIN #Codesets codesets on (@codesetClauses)";
    private static final String COHORT_INCLUSION_ANALYSIS_TEMPALTE;
    private static final String COHORT_CENSORED_STATS_TEMPLATE;
    private static final String DATE_OFFSET_STRATEGY_TEMPLATE;
    private static final String CUSTOM_ERA_STRATEGY_TEMPLATE;
    private static final String ERA_CONSTRUCTOR_TEMPLATE;

    public CohortExpressionQueryBuilder() {
    }

    private ArrayList<Long> getConceptIdsFromConcepts(Concept[] concepts) {
        ArrayList<Long> conceptIdList = new ArrayList();
        Concept[] var3 = concepts;
        int var4 = concepts.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Concept concept = var3[var5];
            conceptIdList.add(concept.conceptId);
        }

        return conceptIdList;
    }

    private String getOperator(String op) {
        byte var3 = -1;
        switch(op.hashCode()) {
            case 3244:
                if (op.equals("eq")) {
                    var3 = 2;
                }
                break;
            case 3309:
                if (op.equals("gt")) {
                    var3 = 4;
                }
                break;
            case 3464:
                if (op.equals("lt")) {
                    var3 = 0;
                }
                break;
            case 34957:
                if (op.equals("!eq")) {
                    var3 = 3;
                }
                break;
            case 102680:
                if (op.equals("gte")) {
                    var3 = 5;
                }
                break;
            case 107485:
                if (op.equals("lte")) {
                    var3 = 1;
                }
        }

        switch(var3) {
            case 0:
                return "<";
            case 1:
                return "<=";
            case 2:
                return "=";
            case 3:
                return "<>";
            case 4:
                return ">";
            case 5:
                return ">=";
            default:
                throw new RuntimeException("Unknown operator type: " + op);
        }
    }

    private String getOccurrenceOperator(int type) {
        switch(type) {
            case 0:
                return "=";
            case 1:
                return "<=";
            case 2:
                return ">=";
            default:
                return "??";
        }
    }

    private String getOperator(DateRange range) {
        return this.getOperator(range.op);
    }

    private String getOperator(NumericRange range) {
        return this.getOperator(range.op);
    }

    private String dateStringToSql(String date) {
        String[] dateParts = StringUtils.split(date, '-');
        return String.format("DATEFROMPARTS(%s, %s, %s)", dateParts[0], dateParts[1], dateParts[2]);
    }

    private String buildDateRangeClause(String sqlExpression, DateRange range) {
        String clause;
        if (range.op.endsWith("bt")) {
            clause = String.format("%s(%s >= %s and %s <= %s)", range.op.startsWith("!") ? "not " : "", sqlExpression, this.dateStringToSql(range.value), sqlExpression, this.dateStringToSql(range.extent));
        } else {
            clause = String.format("%s %s %s", sqlExpression, this.getOperator(range), this.dateStringToSql(range.value));
        }

        return clause;
    }

    private String buildNumericRangeClause(String sqlExpression, NumericRange range) {
        String clause;
        if (range.op.endsWith("bt")) {
            clause = String.format("%s(%s >= %d and %s <= %d)", range.op.startsWith("!") ? "not " : "", sqlExpression, range.value.intValue(), sqlExpression, range.extent.intValue());
        } else {
            clause = String.format("%s %s %d", sqlExpression, this.getOperator(range), range.value.intValue());
        }

        return clause;
    }

    private String buildNumericRangeClause(String sqlExpression, NumericRange range, String format) {
        String clause;
        if (range.op.endsWith("bt")) {
            clause = String.format("%s(%s >= %" + format + " and %s <= %" + format + ")", range.op.startsWith("!") ? "not " : "", sqlExpression, range.value.doubleValue(), sqlExpression, range.extent.doubleValue());
        } else {
            clause = String.format("%s %s %" + format, sqlExpression, this.getOperator(range), range.value.doubleValue());
        }

        return clause;
    }

    private String buildTextFilterClause(String sqlExpression, TextFilter filter) {
        String negation = filter.op.startsWith("!") ? "not" : "";
        String prefix = !filter.op.endsWith("endsWith") && !filter.op.endsWith("contains") ? "" : "%";
        String value = filter.text;
        String postfix = !filter.op.endsWith("startsWith") && !filter.op.endsWith("contains") ? "" : "%";
        return String.format("%s %s like '%s%s%s'", sqlExpression, negation, prefix, value, postfix);
    }

    private String wrapCriteriaQuery(String query, CriteriaGroup group) {
        String eventQuery = StringUtils.replace(EVENT_TABLE_EXPRESSION_TEMPLATE, "@eventQuery", query);
        String groupQuery = this.getCriteriaGroupQuery(group, String.format("(%s)", eventQuery));
        groupQuery = StringUtils.replace(groupQuery, "@indexId", "0");
        String wrappedQuery = String.format("select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.target_concept_id, PE.visit_occurrence_id FROM (\n%s\n) PE\nJOIN (\n%s) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id\n", query, groupQuery);
        return wrappedQuery;
    }

    public String getCodesetQuery(ConceptSet[] conceptSets) {
        String codesetQuery = CODESET_QUERY_TEMPLATE;
        ArrayList<String> codesetInserts = new ArrayList();
        if (conceptSets.length > 0) {
            ConceptSet[] var4 = conceptSets;
            int var5 = conceptSets.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                ConceptSet cs = var4[var6];
                String conceptExpressionQuery = conceptSetQueryBuilder.buildExpressionQuery(cs.expression);
                String conceptSetInsert = String.format("INSERT INTO #Codesets (codeset_id, concept_id)\nSELECT %d as codeset_id, c.concept_id FROM (%s) C;", cs.id, conceptExpressionQuery);
                codesetInserts.add(conceptSetInsert);
            }
        }

        codesetQuery = StringUtils.replace(codesetQuery, "@codesetInserts", StringUtils.join(codesetInserts, "\n"));
        return codesetQuery;
    }

    private String getCodesetJoinExpression(Integer standardCodesetId, String standardConceptColumn, Integer sourceCodesetId, String sourceConceptColumn) {
        String codsetJoinClause = "(%s = codesets.concept_id and codesets.codeset_id = %d)";
        String joinExpression = "";
        ArrayList<String> codesetClauses = new ArrayList();
        if (standardCodesetId != null) {
            codesetClauses.add(String.format("(%s = codesets.concept_id and codesets.codeset_id = %d)", standardConceptColumn, standardCodesetId));
        }

        if (sourceCodesetId != null) {
            codesetClauses.add(String.format("(%s = codesets.concept_id and codesets.codeset_id = %d)", sourceConceptColumn, sourceCodesetId));
        }

        if (codesetClauses.size() > 0) {
            joinExpression = StringUtils.replace("JOIN #Codesets codesets on (@codesetClauses)", "@codesetClauses", StringUtils.join(codesetClauses, " AND "));
        }

        return joinExpression;
    }

    private String getCensoringEventsQuery(Criteria[] censoringCriteria) {
        ArrayList<String> criteriaQueries = new ArrayList();
        Criteria[] var3 = censoringCriteria;
        int var4 = censoringCriteria.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            Criteria c = var3[var5];
            String criteriaQuery = c.accept(this);
            criteriaQueries.add(StringUtils.replace(CENSORING_QUERY_TEMPLATE, "@criteriaQuery", criteriaQuery));
        }

        return StringUtils.join(criteriaQueries, "\nUNION ALL\n");
    }

    public String getPrimaryEventsQuery(PrimaryCriteria primaryCriteria) {
        String query = PRIMARY_EVENTS_TEMPLATE;
        ArrayList<String> criteriaQueries = new ArrayList();
        Criteria[] var4 = primaryCriteria.criteriaList;
        int var5 = var4.length;

        for(int var6 = 0; var6 < var5; ++var6) {
            Criteria c = var4[var6];
            criteriaQueries.add(c.accept(this));
        }

        query = StringUtils.replace(query, "@criteriaQueries", StringUtils.join(criteriaQueries, "\nUNION ALL\n"));
        ArrayList<String> primaryEventsFilters = new ArrayList();
        primaryEventsFilters.add(String.format("DATEADD(day,%d,OP.OBSERVATION_PERIOD_START_DATE) <= E.START_DATE AND DATEADD(day,%d,E.START_DATE) <= OP.OBSERVATION_PERIOD_END_DATE", primaryCriteria.observationWindow.priorDays, primaryCriteria.observationWindow.postDays));
        query = StringUtils.replace(query, "@primaryEventsFilter", StringUtils.join(primaryEventsFilters, " AND "));
        query = StringUtils.replace(query, "@EventSort", primaryCriteria.primaryLimit.type != null && primaryCriteria.primaryLimit.type.equalsIgnoreCase("LAST") ? "DESC" : "ASC");
        query = StringUtils.replace(query, "@primaryEventLimit", !primaryCriteria.primaryLimit.type.equalsIgnoreCase("ALL") ? "WHERE P.ordinal = 1" : "");
        return query;
    }

    public String getCollapseConstructorQuery(CollapseSettings collapseSettings) {
        String query = ERA_CONSTRUCTOR_TEMPLATE;
        query = StringUtils.replace(query, "@eraGroup", "person_id");
        query = StringUtils.replace(query, "@eraconstructorpad", Integer.toString(collapseSettings.eraPad));
        return query;
    }

    public String getFinalCohortQuery(Period censorWindow) {
        String query = "select @target_cohort_id as cohort_definition_id, person_id, @start_date, @end_date \nFROM #final_cohort CO";
        String startDate = "start_date";
        String endDate = "end_date";
        if (censorWindow != null && (censorWindow.startDate != null || censorWindow.endDate != null)) {
            String censorEndDate;
            if (censorWindow.startDate != null) {
                censorEndDate = this.dateStringToSql(censorWindow.startDate);
                startDate = "CASE WHEN start_date > " + censorEndDate + " THEN start_date ELSE " + censorEndDate + " END";
            }

            if (censorWindow.endDate != null) {
                censorEndDate = this.dateStringToSql(censorWindow.endDate);
                endDate = "CASE WHEN end_date < " + censorEndDate + " THEN end_date ELSE " + censorEndDate + " END";
            }

            query = query + "\nWHERE @start_date <= @end_date";
        }

        query = StringUtils.replace(query, "@start_date", startDate);
        query = StringUtils.replace(query, "@end_date", endDate);
        return query;
    }

    private String getInclusionAnalysisQuery(String eventTable, int modeId) {
        String resultSql = COHORT_INCLUSION_ANALYSIS_TEMPALTE;
        resultSql = StringUtils.replace(resultSql, "@inclusionImpactMode", Integer.toString(modeId));
        resultSql = StringUtils.replace(resultSql, "@eventTable", eventTable);
        return resultSql;
    }

    public String buildExpressionQuery(CohortExpression expression, CohortExpressionQueryBuilder.BuildExpressionQueryOptions options) {
        String resultSql = COHORT_QUERY_TEMPLATE;
        String codesetQuery = this.getCodesetQuery(expression.conceptSets);
        resultSql = StringUtils.replace(resultSql, "@codesetQuery", codesetQuery);
        String primaryEventsQuery = this.getPrimaryEventsQuery(expression.primaryCriteria);
        resultSql = StringUtils.replace(resultSql, "@primaryEventsQuery", primaryEventsQuery);
        String additionalCriteriaQuery = "";
        if (expression.additionalCriteria != null && !expression.additionalCriteria.isEmpty()) {
            CriteriaGroup acGroup = expression.additionalCriteria;
            String acGroupQuery = this.getCriteriaGroupQuery(acGroup, "primary_events");
            acGroupQuery = StringUtils.replace(acGroupQuery, "@indexId", "0");
            additionalCriteriaQuery = "\nJOIN (\n" + acGroupQuery + ") AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id\n";
        }

        resultSql = StringUtils.replace(resultSql, "@additionalCriteriaQuery", additionalCriteriaQuery);
        resultSql = StringUtils.replace(resultSql, "@QualifiedEventSort", expression.qualifiedLimit.type != null && expression.qualifiedLimit.type.equalsIgnoreCase("LAST") ? "DESC" : "ASC");
        if (expression.additionalCriteria != null && expression.qualifiedLimit.type != null && !expression.qualifiedLimit.type.equalsIgnoreCase("ALL")) {
            resultSql = StringUtils.replace(resultSql, "@QualifiedLimitFilter", "WHERE QE.ordinal = 1");
        } else {
            resultSql = StringUtils.replace(resultSql, "@QualifiedLimitFilter", "");
        }

        ArrayList endDateSelects;
        if (expression.inclusionRules.size() > 0) {
            endDateSelects = new ArrayList();
            ArrayList<String> inclusionRuleTempTables = new ArrayList();

            for(int i = 0; i < expression.inclusionRules.size(); ++i) {
                CriteriaGroup cg = ((InclusionRule)expression.inclusionRules.get(i)).expression;
                String inclusionRuleInsert = this.getInclusionRuleQuery(cg);
                inclusionRuleInsert = StringUtils.replace(inclusionRuleInsert, "@inclusion_rule_id", "" + i);
                endDateSelects.add(inclusionRuleInsert);
                inclusionRuleTempTables.add(String.format("#Inclusion_%d", i));
            }

            String irTempUnion = (String)inclusionRuleTempTables.stream().map((d) -> {
                return String.format("select inclusion_rule_id, person_id, event_id from %s", d);
            }).collect(Collectors.joining("\nUNION ALL\n"));
            endDateSelects.add(String.format("SELECT inclusion_rule_id, person_id, event_id\nINTO #inclusion_events\nFROM (%s) I;", irTempUnion));
            endDateSelects.addAll((Collection)inclusionRuleTempTables.stream().map((d) -> {
                return String.format("TRUNCATE TABLE %s;\nDROP TABLE %s;\n", d, d);
            }).collect(Collectors.toList()));
            resultSql = StringUtils.replace(resultSql, "@inclusionCohortInserts", StringUtils.join(endDateSelects, "\n"));
        } else {
            resultSql = StringUtils.replace(resultSql, "@inclusionCohortInserts", "create table #inclusion_events (inclusion_rule_id bigint,\n\tperson_id bigint,\n\tevent_id bigint\n);");
        }

        resultSql = StringUtils.replace(resultSql, "@IncludedEventSort", expression.expressionLimit.type != null && expression.expressionLimit.type.equalsIgnoreCase("LAST") ? "DESC" : "ASC");
        if (expression.expressionLimit.type != null && !expression.expressionLimit.type.equalsIgnoreCase("ALL")) {
            resultSql = StringUtils.replace(resultSql, "@ResultLimitFilter", "WHERE Results.ordinal = 1");
        } else {
            resultSql = StringUtils.replace(resultSql, "@ResultLimitFilter", "");
        }

        resultSql = StringUtils.replace(resultSql, "@ruleTotal", String.valueOf(expression.inclusionRules.size()));
        endDateSelects = new ArrayList();
        if (!(expression.endStrategy instanceof DateOffsetStrategy)) {
            endDateSelects.add("-- By default, cohort exit at the event's op end date\nselect event_id, person_id, op_end_date as end_date from #included_events");
        }

        if (expression.endStrategy != null) {
            resultSql = StringUtils.replace(resultSql, "@strategy_ends_temp_tables", expression.endStrategy.accept(this, "#included_events"));
            resultSql = StringUtils.replace(resultSql, "@strategy_ends_cleanup", "TRUNCATE TABLE #strategy_ends;\nDROP TABLE #strategy_ends;\n");
            endDateSelects.add(String.format("-- End Date Strategy\n%s\n", "SELECT event_id, person_id, end_date from #strategy_ends"));
        } else {
            resultSql = StringUtils.replace(resultSql, "@strategy_ends_temp_tables", "");
            resultSql = StringUtils.replace(resultSql, "@strategy_ends_cleanup", "");
        }

        if (expression.censoringCriteria != null && expression.censoringCriteria.length > 0) {
            endDateSelects.add(String.format("-- Censor Events\n%s\n", this.getCensoringEventsQuery(expression.censoringCriteria)));
        }

        resultSql = StringUtils.replace(resultSql, "@finalCohortQuery", this.getFinalCohortQuery(expression.censorWindow));
        resultSql = StringUtils.replace(resultSql, "@cohort_end_unions", StringUtils.join(endDateSelects, "\nUNION ALL\n"));
        resultSql = StringUtils.replace(resultSql, "@eraconstructorpad", Integer.toString(expression.collapseSettings.eraPad));
        resultSql = StringUtils.replace(resultSql, "@inclusionImpactAnalysisByEventQuery", this.getInclusionAnalysisQuery("#qualified_events", 0));
        resultSql = StringUtils.replace(resultSql, "@inclusionImpactAnalysisByPersonQuery", this.getInclusionAnalysisQuery("#best_events", 1));
        resultSql = StringUtils.replace(resultSql, "@cohortCensoredStatsQuery", expression.censorWindow == null || StringUtils.isEmpty(expression.censorWindow.startDate) && StringUtils.isEmpty(expression.censorWindow.endDate) ? "" : COHORT_CENSORED_STATS_TEMPLATE);
        if (options != null) {
            if (options.cdmSchema != null) {
                resultSql = StringUtils.replace(resultSql, "@cdm_database_schema", options.cdmSchema);
            }

            if (options.targetTable != null) {
                resultSql = StringUtils.replace(resultSql, "@target_database_schema.@target_cohort_table", options.targetTable);
            }

            if (options.resultSchema != null) {
                resultSql = StringUtils.replace(resultSql, "@results_database_schema", options.resultSchema);
            }

            if (options.vocabularySchema != null) {
                resultSql = StringUtils.replace(resultSql, "@vocabulary_database_schema", options.vocabularySchema);
            } else if (options.cdmSchema != null) {
                resultSql = StringUtils.replace(resultSql, "@vocabulary_database_schema", options.cdmSchema);
            }

            if (options.cohortId != null) {
                resultSql = StringUtils.replace(resultSql, "@target_cohort_id", options.cohortId.toString());
            }

            resultSql = StringUtils.replace(resultSql, "@generateStats", options.generateStats ? "1" : "0");
        }

        return resultSql;
    }

    public String getCriteriaGroupQuery(CriteriaGroup group, String eventTable) {
        String query = GROUP_QUERY_TEMPLATE;
        ArrayList<String> additionalCriteriaQueries = new ArrayList();
        String joinType = "INNER";
        int indexId = 0;
        CorelatedCriteria[] var7 = group.criteriaList;
        int var8 = var7.length;

        int var9;
        String gQuery;
        for(var9 = 0; var9 < var8; ++var9) {
            CorelatedCriteria cc = var7[var9];
            gQuery = this.getCorelatedlCriteriaQuery(cc, eventTable);
            gQuery = StringUtils.replace(gQuery, "@indexId", "" + indexId);
            additionalCriteriaQueries.add(gQuery);
            ++indexId;
        }

        DemographicCriteria[] var12 = group.demographicCriteriaList;
        var8 = var12.length;

        for(var9 = 0; var9 < var8; ++var9) {
            DemographicCriteria dc = var12[var9];
            gQuery = this.getDemographicCriteriaQuery(dc, eventTable);
            gQuery = StringUtils.replace(gQuery, "@indexId", "" + indexId);
            additionalCriteriaQueries.add(gQuery);
            ++indexId;
        }

        CriteriaGroup[] var13 = group.groups;
        var8 = var13.length;

        for(var9 = 0; var9 < var8; ++var9) {
            CriteriaGroup g = var13[var9];
            gQuery = this.getCriteriaGroupQuery(g, eventTable);
            gQuery = StringUtils.replace(gQuery, "@indexId", "" + indexId);
            additionalCriteriaQueries.add(gQuery);
            ++indexId;
        }

        if (indexId > 0) {
            query = StringUtils.replace(query, "@criteriaQueries", StringUtils.join(additionalCriteriaQueries, "\nUNION ALL\n"));
            String occurrenceCountClause = "HAVING COUNT(index_id) ";
            if (group.type.equalsIgnoreCase("ALL")) {
                occurrenceCountClause = occurrenceCountClause + "= " + indexId;
            }

            if (group.type.equalsIgnoreCase("ANY")) {
                occurrenceCountClause = occurrenceCountClause + "> 0";
            }

            if (group.type.toUpperCase().startsWith("AT_")) {
                if (group.type.toUpperCase().endsWith("LEAST")) {
                    occurrenceCountClause = occurrenceCountClause + ">= " + group.count;
                } else {
                    occurrenceCountClause = occurrenceCountClause + "<= " + group.count;
                    joinType = "LEFT";
                }

                if (group.count == 0) {
                    joinType = "LEFT";
                }
            }

            query = StringUtils.replace(query, "@occurrenceCountClause", occurrenceCountClause);
            query = StringUtils.replace(query, "@joinType", joinType);
        } else {
            query = "-- Begin Criteria Group\n select @indexId as index_id, person_id, event_id FROM @eventTable\n-- End Criteria Group\n";
        }

        query = StringUtils.replace(query, "@eventTable", eventTable);
        return query;
    }

    private String getInclusionRuleQuery(CriteriaGroup inclusionRule) {
        String resultSql = INCLUSION_RULE_QUERY_TEMPLATE;
        String additionalCriteriaQuery = "\nJOIN (\n" + this.getCriteriaGroupQuery(inclusionRule, "#qualified_events") + ") AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id";
        additionalCriteriaQuery = StringUtils.replace(additionalCriteriaQuery, "@indexId", "0");
        resultSql = StringUtils.replace(resultSql, "@additionalCriteriaQuery", additionalCriteriaQuery);
        return resultSql;
    }

    public String getDemographicCriteriaQuery(DemographicCriteria criteria, String eventTable) {
        String query = DEMOGRAPHIC_CRITERIA_QUERY_TEMPLATE;
        query = StringUtils.replace(query, "@eventTable", eventTable);
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(E.start_date) - P.year_of_birth", criteria.age));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        if (criteria.race != null && criteria.race.length > 0) {
            whereClauses.add(String.format("P.race_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.race), ",")));
        }

        if (criteria.race != null && criteria.race.length > 0) {
            whereClauses.add(String.format("P.race_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.race), ",")));
        }

        if (criteria.ethnicity != null && criteria.ethnicity.length > 0) {
            whereClauses.add(String.format("P.ethnicity_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.ethnicity), ",")));
        }

        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("E.start_date", criteria.occurrenceStartDate));
        }

        if (criteria.occurrenceEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("E.end_date", criteria.occurrenceEndDate));
        }

        if (whereClauses.size() > 0) {
            query = StringUtils.replace(query, "@whereClause", "WHERE " + StringUtils.join(whereClauses, " AND "));
        } else {
            query = StringUtils.replace(query, "@whereClause", "");
        }

        return query;
    }

    public String getWindowedCriteriaQuery(String sqlTemplate, WindowedCriteria criteria, String eventTable) {
        boolean checkObservationPeriod = !criteria.ignoreObservationPeriod;
        String criteriaQuery = criteria.criteria.accept(this);
        String query = StringUtils.replace(sqlTemplate, "@criteriaQuery", criteriaQuery);
        query = StringUtils.replace(query, "@eventTable", eventTable);
        List<String> clauses = new ArrayList();
        if (checkObservationPeriod) {
            clauses.add("A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE");
        }

        Window startWindow = criteria.startWindow;
        String startIndexDateExpression = startWindow.useIndexEnd != null && startWindow.useIndexEnd ? "P.END_DATE" : "P.START_DATE";
        String startEventDateExpression = startWindow.useEventEnd != null && startWindow.useEventEnd ? "A.END_DATE" : "A.START_DATE";
        String startExpression;
        if (startWindow.start.days != null) {
            startExpression = String.format("DATEADD(day,%d,%s)", startWindow.start.coeff * startWindow.start.days, startIndexDateExpression);
        } else {
            startExpression = checkObservationPeriod ? (startWindow.start.coeff == -1 ? "P.OP_START_DATE" : "P.OP_END_DATE") : null;
        }

        if (startExpression != null) {
            clauses.add(String.format("%s >= %s", startEventDateExpression, startExpression));
        }

        String endExpression;
        if (startWindow.end.days != null) {
            endExpression = String.format("DATEADD(day,%d,%s)", startWindow.end.coeff * startWindow.end.days, startIndexDateExpression);
        } else {
            endExpression = checkObservationPeriod ? (startWindow.end.coeff == -1 ? "P.OP_START_DATE" : "P.OP_END_DATE") : null;
        }

        if (endExpression != null) {
            clauses.add(String.format("%s <= %s", startEventDateExpression, endExpression));
        }

        Window endWindow = criteria.endWindow;
        if (endWindow != null) {
            String endIndexDateExpression = endWindow.useIndexEnd != null && endWindow.useIndexEnd ? "P.END_DATE" : "P.START_DATE";
            String endEventDateExpression = endWindow.useEventEnd != null && !endWindow.useEventEnd ? "A.START_DATE" : "A.END_DATE";
            if (endWindow.start.days != null) {
                startExpression = String.format("DATEADD(day,%d,%s)", endWindow.start.coeff * endWindow.start.days, endIndexDateExpression);
            } else {
                startExpression = checkObservationPeriod ? (endWindow.start.coeff == -1 ? "P.OP_START_DATE" : "P.OP_END_DATE") : null;
            }

            if (startExpression != null) {
                clauses.add(String.format("%s >= %s", endEventDateExpression, startExpression));
            }

            if (endWindow.end.days != null) {
                endExpression = String.format("DATEADD(day,%d,%s)", endWindow.end.coeff * endWindow.end.days, endIndexDateExpression);
            } else {
                endExpression = checkObservationPeriod ? (endWindow.end.coeff == -1 ? "P.OP_START_DATE" : "P.OP_END_DATE") : null;
            }

            if (endExpression != null) {
                clauses.add(String.format("%s <= %s", endEventDateExpression, endExpression));
            }
        }

        boolean restrictVisit = criteria.restrictVisit;
        if (restrictVisit) {
            clauses.add("A.visit_occurrence_id = P.visit_occurrence_id");
        }

        query = StringUtils.replace(query, "@windowCriteria", StringUtils.join(clauses, " AND "));
        return query;
    }

    public String getWindowedCriteriaQuery(WindowedCriteria criteria, String eventTable) {
        String query = this.getWindowedCriteriaQuery(WINDOWED_CRITERIA_TEMPLATE, criteria, eventTable);
        query = StringUtils.replace(query, "@joinType", "INNER");
        return query;
    }

    public String getCorelatedlCriteriaQuery(CorelatedCriteria corelatedCriteria, String eventTable) {
        String query = ADDITIONAL_CRITERIA_TEMPLATE;
        query = this.getWindowedCriteriaQuery(query, corelatedCriteria, eventTable);
        String occurrenceCriteria = String.format("HAVING COUNT(%sA.TARGET_CONCEPT_ID) %s %d", corelatedCriteria.occurrence.isDistinct ? "DISTINCT " : "", this.getOccurrenceOperator(corelatedCriteria.occurrence.type), corelatedCriteria.occurrence.count);
        String joinType = corelatedCriteria.occurrence.type != 1 && corelatedCriteria.occurrence.count != 0 ? "INNER" : "LEFT";
        query = StringUtils.replace(query, "@joinType", joinType);
        query = StringUtils.replace(query, "@occurrenceCriteria", occurrenceCriteria);
        return query;
    }

    public String getCriteriaSql(ConditionEra criteria) {
        String query = CONDITION_ERA_TEMPLATE;
        String codesetClause = "";
        if (criteria.codesetId != null) {
            codesetClause = String.format("where ce.condition_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
        }

        query = StringUtils.replace(query, "@codesetClause", codesetClause);
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.ageAtStart != null || criteria.ageAtEnd != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY ce.person_id ORDER BY ce.condition_era_start_date, ce.condition_era_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.eraStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.condition_era_start_date", criteria.eraStartDate));
        }

        if (criteria.eraEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.condition_era_end_date", criteria.eraEndDate));
        }

        if (criteria.occurrenceCount != null) {
            whereClauses.add(this.buildNumericRangeClause("C.condition_occurrence_count", criteria.occurrenceCount));
        }

        if (criteria.eraLength != null) {
            whereClauses.add(this.buildNumericRangeClause("DATEDIFF(d,C.condition_era_start_date, C.condition_era_end_date)", criteria.eraLength));
        }

        if (criteria.ageAtStart != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.condition_era_start_date) - P.year_of_birth", criteria.ageAtStart));
        }

        if (criteria.ageAtEnd != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.condition_era_end_date) - P.year_of_birth", criteria.ageAtEnd));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(ConditionOccurrence criteria) {
        String query = CONDITION_OCCURRENCE_TEMPLATE;
        query = StringUtils.replace(query, "@codesetClause", this.getCodesetJoinExpression(criteria.codesetId, "co.condition_concept_id", criteria.conditionSourceConcept, "co.condition_source_concept_id"));
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.age != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY co.person_id ORDER BY co.condition_start_date, co.condition_occurrence_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.condition_start_date", criteria.occurrenceStartDate));
        }

        if (criteria.occurrenceEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.condition_end_date", criteria.occurrenceEndDate));
        }

        if (criteria.conditionType != null && criteria.conditionType.length > 0) {
            ArrayList<Long> conceptIds = this.getConceptIdsFromConcepts(criteria.conditionType);
            whereClauses.add(String.format("C.condition_type_concept_id %s in (%s)", criteria.conditionTypeExclude ? "not" : "", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.stopReason != null) {
            whereClauses.add(this.buildTextFilterClause("C.stop_reason", criteria.stopReason));
        }

        if (criteria.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.condition_start_date) - P.year_of_birth", criteria.age));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.providerSpecialty), ",")));
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.visitType), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(Death criteria) {
        String query = DEATH_TEMPLATE;
        query = StringUtils.replace(query, "@codesetClause", this.getCodesetJoinExpression(criteria.codesetId, "d.cause_concept_id", criteria.deathSourceConcept, "d.cause_source_concept_id"));
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.age != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.death_date", criteria.occurrenceStartDate));
        }

        if (criteria.deathType != null && criteria.deathType.length > 0) {
            ArrayList<Long> conceptIds = this.getConceptIdsFromConcepts(criteria.deathType);
            whereClauses.add(String.format("C.death_type_concept_id %s in (%s)", criteria.deathTypeExclude ? "not" : "", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.death_date) - P.year_of_birth", criteria.age));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(DeviceExposure criteria) {
        String query = DEVICE_EXPOSURE_TEMPLATE;
        query = StringUtils.replace(query, "@codesetClause", this.getCodesetJoinExpression(criteria.codesetId, "de.device_concept_id", criteria.deviceSourceConcept, "de.device_source_concept_id"));
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.age != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY de.person_id ORDER BY de.device_exposure_start_date, de.device_exposure_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.device_exposure_start_date", criteria.occurrenceStartDate));
        }

        if (criteria.occurrenceEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.device_exposure_end_date", criteria.occurrenceEndDate));
        }

        if (criteria.deviceType != null && criteria.deviceType.length > 0) {
            ArrayList<Long> conceptIds = this.getConceptIdsFromConcepts(criteria.deviceType);
            whereClauses.add(String.format("C.device_type_concept_id %s in (%s)", criteria.deviceTypeExclude ? "not" : "", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.uniqueDeviceId != null) {
            whereClauses.add(this.buildTextFilterClause("C.unique_device_id", criteria.uniqueDeviceId));
        }

        if (criteria.quantity != null) {
            whereClauses.add(this.buildNumericRangeClause("C.quantity", criteria.quantity));
        }

        if (criteria.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.device_exposure_start_date) - P.year_of_birth", criteria.age));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.providerSpecialty), ",")));
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.visitType), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(DoseEra criteria) {
        String query = DOSE_ERA_TEMPLATE;
        String codesetClause = "";
        if (criteria.codesetId != null) {
            codesetClause = String.format("where de.drug_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
        }

        query = StringUtils.replace(query, "@codesetClause", codesetClause);
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.ageAtStart != null || criteria.ageAtEnd != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY de.person_id ORDER BY de.dose_era_start_date, de.dose_era_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.eraStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.dose_era_start_date", criteria.eraStartDate));
        }

        if (criteria.eraEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.dose_era_end_date", criteria.eraEndDate));
        }

        if (criteria.unit != null && criteria.unit.length > 0) {
            whereClauses.add(String.format("c.unit_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.unit), ",")));
        }

        if (criteria.doseValue != null) {
            whereClauses.add(this.buildNumericRangeClause("c.dose_value", criteria.doseValue, ".4f"));
        }

        if (criteria.eraLength != null) {
            whereClauses.add(this.buildNumericRangeClause("DATEDIFF(d,C.dose_era_start_date, C.dose_era_end_date)", criteria.eraLength));
        }

        if (criteria.ageAtStart != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.dose_era_start_date) - P.year_of_birth", criteria.ageAtStart));
        }

        if (criteria.ageAtEnd != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.dose_era_end_date) - P.year_of_birth", criteria.ageAtEnd));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(DrugEra criteria) {
        String query = DRUG_ERA_TEMPLATE;
        String codesetClause = "";
        if (criteria.codesetId != null) {
            codesetClause = String.format("where de.drug_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
        }

        query = StringUtils.replace(query, "@codesetClause", codesetClause);
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.ageAtStart != null || criteria.ageAtEnd != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY de.person_id ORDER BY de.drug_era_start_date, de.drug_era_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.eraStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.drug_era_start_date", criteria.eraStartDate));
        }

        if (criteria.eraEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.drug_era_end_date", criteria.eraEndDate));
        }

        if (criteria.occurrenceCount != null) {
            whereClauses.add(this.buildNumericRangeClause("C.drug_exposure_count", criteria.occurrenceCount));
        }

        if (criteria.eraLength != null) {
            whereClauses.add(this.buildNumericRangeClause("DATEDIFF(d,C.drug_era_start_date, C.drug_era_end_date)", criteria.eraLength));
        }

        if (criteria.gapDays != null) {
            whereClauses.add(this.buildNumericRangeClause("C.gap_days", criteria.eraLength));
        }

        if (criteria.ageAtStart != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.drug_era_start_date) - P.year_of_birth", criteria.ageAtStart));
        }

        if (criteria.ageAtEnd != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.drug_era_end_date) - P.year_of_birth", criteria.ageAtEnd));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(DrugExposure criteria) {
        String query = DRUG_EXPOSURE_TEMPLATE;
        query = StringUtils.replace(query, "@codesetClause", this.getCodesetJoinExpression(criteria.codesetId, "de.drug_concept_id", criteria.drugSourceConcept, "de.drug_source_concept_id"));
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.age != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY de.person_id ORDER BY de.drug_exposure_start_date, de.drug_exposure_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.drug_exposure_start_date", criteria.occurrenceStartDate));
        }

        if (criteria.occurrenceEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.drug_exposure_end_date", criteria.occurrenceEndDate));
        }

        if (criteria.drugType != null && criteria.drugType.length > 0) {
            ArrayList<Long> conceptIds = this.getConceptIdsFromConcepts(criteria.drugType);
            whereClauses.add(String.format("C.drug_type_concept_id %s in (%s)", criteria.drugTypeExclude ? "not" : "", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.stopReason != null) {
            whereClauses.add(this.buildTextFilterClause("C.stop_reason", criteria.stopReason));
        }

        if (criteria.refills != null) {
            whereClauses.add(this.buildNumericRangeClause("C.refills", criteria.refills));
        }

        if (criteria.quantity != null) {
            whereClauses.add(this.buildNumericRangeClause("C.quantity", criteria.quantity, ".4f"));
        }

        if (criteria.daysSupply != null) {
            whereClauses.add(this.buildNumericRangeClause("C.days_supply", criteria.daysSupply));
        }

        if (criteria.routeConcept != null && criteria.routeConcept.length > 0) {
            whereClauses.add(String.format("C.route_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.routeConcept), ",")));
        }

        if (criteria.effectiveDrugDose != null) {
            whereClauses.add(this.buildNumericRangeClause("C.effective_drug_dose", criteria.effectiveDrugDose, ".4f"));
        }

        if (criteria.doseUnit != null && criteria.doseUnit.length > 0) {
            whereClauses.add(String.format("C.dose_unit_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.doseUnit), ",")));
        }

        if (criteria.lotNumber != null) {
            whereClauses.add(this.buildTextFilterClause("C.lot_number", criteria.lotNumber));
        }

        if (criteria.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.drug_exposure_start_date) - P.year_of_birth", criteria.age));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.providerSpecialty), ",")));
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.visitType), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(Measurement criteria) {
        String query = MEASUREMENT_TEMPLATE;
        query = StringUtils.replace(query, "@codesetClause", this.getCodesetJoinExpression(criteria.codesetId, "m.measurement_concept_id", criteria.measurementSourceConcept, "m.measurement_source_concept_id"));
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.age != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY m.person_id ORDER BY m.measurement_date, m.measurement_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.measurement_date", criteria.occurrenceStartDate));
        }

        ArrayList conceptIds;
        if (criteria.measurementType != null && criteria.measurementType.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.measurementType);
            whereClauses.add(String.format("C.measurement_type_concept_id %s in (%s)", criteria.measurementTypeExclude ? "not" : "", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.operator != null && criteria.operator.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.operator);
            whereClauses.add(String.format("C.operator_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.valueAsNumber != null) {
            whereClauses.add(this.buildNumericRangeClause("C.value_as_number", criteria.valueAsNumber, ".4f"));
        }

        if (criteria.valueAsConcept != null && criteria.valueAsConcept.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.valueAsConcept);
            whereClauses.add(String.format("C.value_as_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.unit != null && criteria.unit.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.unit);
            whereClauses.add(String.format("C.unit_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.rangeLow != null) {
            whereClauses.add(this.buildNumericRangeClause("C.range_low", criteria.rangeLow, ".4f"));
        }

        if (criteria.rangeHigh != null) {
            whereClauses.add(this.buildNumericRangeClause("C.range_high", criteria.rangeHigh, ".4f"));
        }

        if (criteria.rangeLowRatio != null) {
            whereClauses.add(this.buildNumericRangeClause("(C.value_as_number / NULLIF(C.range_low, 0))", criteria.rangeLowRatio, ".4f"));
        }

        if (criteria.rangeHighRatio != null) {
            whereClauses.add(this.buildNumericRangeClause("(C.value_as_number / NULLIF(C.range_high, 0))", criteria.rangeHighRatio, ".4f"));
        }

        if (criteria.abnormal != null && criteria.abnormal) {
            whereClauses.add("(C.value_as_number < C.range_low or C.value_as_number > C.range_high or C.value_as_concept_id in (4155142, 4155143))");
        }

        if (criteria.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.measurement_date) - P.year_of_birth", criteria.age));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.providerSpecialty), ",")));
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.visitType), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(Observation criteria) {
        String query = OBSERVATION_TEMPLATE;
        query = StringUtils.replace(query, "@codesetClause", this.getCodesetJoinExpression(criteria.codesetId, "o.observation_concept_id", criteria.observationSourceConcept, "o.observation_source_concept_id"));
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.age != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY o.person_id ORDER BY o.observation_date, o.observation_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.observation_date", criteria.occurrenceStartDate));
        }

        ArrayList conceptIds;
        if (criteria.observationType != null && criteria.observationType.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.observationType);
            whereClauses.add(String.format("C.observation_type_concept_id %s in (%s)", criteria.observationTypeExclude ? "not" : "", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.valueAsNumber != null) {
            whereClauses.add(this.buildNumericRangeClause("C.value_as_number", criteria.valueAsNumber, ".4f"));
        }

        if (criteria.valueAsString != null) {
            whereClauses.add(this.buildTextFilterClause("C.value_as_string", criteria.valueAsString));
        }

        if (criteria.valueAsConcept != null && criteria.valueAsConcept.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.valueAsConcept);
            whereClauses.add(String.format("C.value_as_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.qualifier != null && criteria.qualifier.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.qualifier);
            whereClauses.add(String.format("C.qualifier_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.unit != null && criteria.unit.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.unit);
            whereClauses.add(String.format("C.unit_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.observation_date) - P.year_of_birth", criteria.age));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.providerSpecialty), ",")));
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.visitType), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(ObservationPeriod criteria) {
        String query = OBSERVATION_PERIOD_TEMPLATE;
        String startDateExpression = "C.observation_period_start_date";
        String endDateExpression = "C.observation_period_end_date";
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.ageAtStart != null || criteria.ageAtEnd != null) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
        }

        if (criteria.userDefinedPeriod != null) {
            Period userDefinedPeriod = criteria.userDefinedPeriod;
            if (userDefinedPeriod.startDate != null) {
                startDateExpression = String.format("CAST('%s' as Date)", userDefinedPeriod.startDate);
                whereClauses.add(String.format("C.OBSERVATION_PERIOD_START_DATE <= %s and C.OBSERVATION_PERIOD_END_DATE >= %s", startDateExpression, startDateExpression));
            }

            if (userDefinedPeriod.endDate != null) {
                endDateExpression = String.format("CAST('%s' as Date)", userDefinedPeriod.endDate);
                whereClauses.add(String.format("C.OBSERVATION_PERIOD_START_DATE <= %s and C.OBSERVATION_PERIOD_END_DATE >= %s", endDateExpression, endDateExpression));
            }
        }

        query = StringUtils.replace(query, "@startDateExpression", startDateExpression);
        query = StringUtils.replace(query, "@endDateExpression", endDateExpression);
        if (criteria.periodStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.observation_period_start_date", criteria.periodStartDate));
        }

        if (criteria.periodEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.observation_period_end_date", criteria.periodEndDate));
        }

        if (criteria.periodType != null && criteria.periodType.length > 0) {
            ArrayList<Long> conceptIds = this.getConceptIdsFromConcepts(criteria.periodType);
            whereClauses.add(String.format("C.period_type_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.periodLength != null) {
            whereClauses.add(this.buildNumericRangeClause("DATEDIFF(d,C.observation_period_start_date, C.observation_period_end_date)", criteria.periodLength));
        }

        if (criteria.ageAtStart != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.observation_period_start_date) - P.year_of_birth", criteria.ageAtStart));
        }

        if (criteria.ageAtEnd != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.observation_period_end_date) - P.year_of_birth", criteria.ageAtEnd));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(PayerPlanPeriod criteria) {
        String query = PAYER_PLAN_PERIOD_TEMPLATE;
        String startDateExpression = "C.payer_plan_period_start_date";
        String endDateExpression = "C.payer_plan_period_end_date";
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.ageAtStart != null || criteria.ageAtEnd != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
        }

        if (criteria.userDefinedPeriod != null) {
            Period userDefinedPeriod = criteria.userDefinedPeriod;
            if (userDefinedPeriod.startDate != null) {
                startDateExpression = String.format("CAST('%s' as Date)", userDefinedPeriod.startDate);
                whereClauses.add(String.format("C.PAYER_PLAN_PERIOD_START_DATE <= %s and C.PAYER_PLAN_PERIOD_END_DATE >= %s", startDateExpression, startDateExpression));
            }

            if (userDefinedPeriod.endDate != null) {
                endDateExpression = String.format("CAST('%s' as Date)", userDefinedPeriod.endDate);
                whereClauses.add(String.format("C.PAYER_PLAN_PERIOD_START_DATE <= %s and C.PAYER_PLAN_PERIOD_END_DATE >= %s", endDateExpression, endDateExpression));
            }
        }

        query = StringUtils.replace(query, "@startDateExpression", startDateExpression);
        query = StringUtils.replace(query, "@endDateExpression", endDateExpression);
        if (criteria.periodStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.payer_plan_period_start_date", criteria.periodStartDate));
        }

        if (criteria.periodEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.payer_plan_period_end_date", criteria.periodEndDate));
        }

        if (criteria.periodLength != null) {
            whereClauses.add(this.buildNumericRangeClause("DATEDIFF(d,C.payer_plan_period_start_date, C.payer_plan_period_end_date)", criteria.periodLength));
        }

        if (criteria.ageAtStart != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.payer_plan_period_start_date) - P.year_of_birth", criteria.ageAtStart));
        }

        if (criteria.ageAtEnd != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.payer_plan_period_end_date) - P.year_of_birth", criteria.ageAtEnd));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            ArrayList<Long> conceptIds = this.getConceptIdsFromConcepts(criteria.gender);
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.payerConcept != null) {
            whereClauses.add(String.format("C.payer_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.payerConcept));
        }

        if (criteria.planConcept != null) {
            whereClauses.add(String.format("C.plan_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.planConcept));
        }

        if (criteria.sponsorConcept != null) {
            whereClauses.add(String.format("C.sponsor_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.sponsorConcept));
        }

        if (criteria.stopReasonConcept != null) {
            whereClauses.add(String.format("C.stop_reason_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.stopReasonConcept));
        }

        if (criteria.payerSourceConcept != null) {
            whereClauses.add(String.format("C.payer_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.payerSourceConcept));
        }

        if (criteria.planSourceConcept != null) {
            whereClauses.add(String.format("C.plan_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.planSourceConcept));
        }

        if (criteria.sponsorSourceConcept != null) {
            whereClauses.add(String.format("C.sponsor_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.sponsorSourceConcept));
        }

        if (criteria.stopReasonSourceConcept != null) {
            whereClauses.add(String.format("C.stop_reason_source_concept_id in (SELECT concept_id from #Codesets where codeset_id = %d)", criteria.stopReasonSourceConcept));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(ProcedureOccurrence criteria) {
        String query = PROCEDURE_OCCURRENCE_TEMPLATE;
        query = StringUtils.replace(query, "@codesetClause", this.getCodesetJoinExpression(criteria.codesetId, "po.procedure_concept_id", criteria.procedureSourceConcept, "po.procedure_source_concept_id"));
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.age != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.VISIT_OCCURRENCE V on C.visit_occurrence_id = V.visit_occurrence_id and C.person_id = V.person_id");
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY po.person_id ORDER BY po.procedure_date, po.procedure_occurrence_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.procedure_date", criteria.occurrenceStartDate));
        }

        ArrayList conceptIds;
        if (criteria.procedureType != null && criteria.procedureType.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.procedureType);
            whereClauses.add(String.format("C.procedure_type_concept_id %s in (%s)", criteria.procedureTypeExclude ? "not" : "", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.modifier != null && criteria.modifier.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.modifier);
            whereClauses.add(String.format("C.modifier_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.quantity != null) {
            whereClauses.add(this.buildNumericRangeClause("C.quantity", criteria.quantity));
        }

        if (criteria.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.procedure_date) - P.year_of_birth", criteria.age));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.providerSpecialty), ",")));
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            whereClauses.add(String.format("V.visit_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.visitType), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(Specimen criteria) {
        String query = SPECIMEN_TEMPLATE;
        String codesetClause = "";
        if (criteria.codesetId != null) {
            codesetClause = String.format("where s.specimen_concept_id in (SELECT concept_id from  #Codesets where codeset_id = %d)", criteria.codesetId);
        }

        query = StringUtils.replace(query, "@codesetClause", codesetClause);
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.age != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY s.person_id ORDER BY s.specimen_date, s.specimen_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.specimen_date", criteria.occurrenceStartDate));
        }

        ArrayList conceptIds;
        if (criteria.specimenType != null && criteria.specimenType.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.specimenType);
            whereClauses.add(String.format("C.specimen_type_concept_id %s in (%s)", criteria.specimenTypeExclude ? "not" : "", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.quantity != null) {
            whereClauses.add(this.buildNumericRangeClause("C.quantity", criteria.quantity, ".4f"));
        }

        if (criteria.unit != null && criteria.unit.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.unit);
            whereClauses.add(String.format("C.unit_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.anatomicSite != null && criteria.anatomicSite.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.anatomicSite);
            whereClauses.add(String.format("C.anatomic_site_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.diseaseStatus != null && criteria.diseaseStatus.length > 0) {
            conceptIds = this.getConceptIdsFromConcepts(criteria.diseaseStatus);
            whereClauses.add(String.format("C.disease_status_concept_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.sourceId != null) {
            whereClauses.add(this.buildTextFilterClause("C.specimen_source_id", criteria.sourceId));
        }

        if (criteria.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.specimen_date) - P.year_of_birth", criteria.age));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    public String getCriteriaSql(VisitOccurrence criteria) {
        String query = VISIT_OCCURRENCE_TEMPLATE;
        query = StringUtils.replace(query, "@codesetClause", this.getCodesetJoinExpression(criteria.codesetId, "vo.visit_concept_id", criteria.visitSourceConcept, "vo.visit_source_concept_id"));
        ArrayList<String> joinClauses = new ArrayList();
        if (criteria.age != null || criteria.gender != null && criteria.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        if (criteria.placeOfService != null && criteria.placeOfService.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.CARE_SITE CS on C.care_site_id = CS.care_site_id");
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            joinClauses.add("LEFT JOIN @cdm_database_schema.PROVIDER PR on C.provider_id = PR.provider_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (criteria.first != null && criteria.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY vo.person_id ORDER BY vo.visit_start_date, vo.visit_occurrence_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.visit_start_date", criteria.occurrenceStartDate));
        }

        if (criteria.occurrenceEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.visit_end_date", criteria.occurrenceEndDate));
        }

        if (criteria.visitType != null && criteria.visitType.length > 0) {
            ArrayList<Long> conceptIds = this.getConceptIdsFromConcepts(criteria.visitType);
            whereClauses.add(String.format("C.visit_type_concept_id %s in (%s)", criteria.visitTypeExclude ? "not" : "", StringUtils.join(conceptIds, ",")));
        }

        if (criteria.visitLength != null) {
            whereClauses.add(this.buildNumericRangeClause("DATEDIFF(d,C.visit_start_date, C.visit_end_date)", criteria.visitLength));
        }

        if (criteria.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.visit_start_date) - P.year_of_birth", criteria.age));
        }

        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        if (criteria.providerSpecialty != null && criteria.providerSpecialty.length > 0) {
            whereClauses.add(String.format("PR.specialty_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.providerSpecialty), ",")));
        }

        if (criteria.placeOfService != null && criteria.placeOfService.length > 0) {
            whereClauses.add(String.format("CS.place_of_service_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(criteria.placeOfService), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }

        return query;
    }

    @Override
    public String getCriteriaSql(TreatmentLine treatmentLine) {
        String query = TREATMENT_LINE_TEMPLATE;
        query = StringUtils.replace(query, "@codesetClause", this.getCodesetJoinExpression(treatmentLine.codesetId, "tl.drug_concept_id", null, null));
        ArrayList<String> joinClauses = new ArrayList();
        if (treatmentLine.age != null || treatmentLine.gender != null && treatmentLine.gender.length > 0) {
            joinClauses.add("JOIN @cdm_database_schema.PERSON P on C.person_id = P.person_id");
        }

        query = StringUtils.replace(query, "@joinClause", StringUtils.join(joinClauses, "\n"));
        ArrayList<String> whereClauses = new ArrayList();
        if (treatmentLine.first != null && treatmentLine.first) {
            whereClauses.add("C.ordinal = 1");
            query = StringUtils.replace(query, "@ordinalExpression", ", row_number() over (PARTITION BY tl.person_id ORDER BY tl.line_start_date, tl.treatment_line_id) as ordinal");
        } else {
            query = StringUtils.replace(query, "@ordinalExpression", "");
        }

        if (treatmentLine.treatmentLineType != null && treatmentLine.treatmentLineType.length > 0) {
            ArrayList<Long> conceptIds = this.getConceptIdsFromConcepts(treatmentLine.treatmentLineType);
            whereClauses.add(String.format("C.treatment_type_id in (%s)", StringUtils.join(conceptIds, ",")));
        }

        if (treatmentLine.treatmentLineStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.line_start_date", treatmentLine.treatmentLineStartDate));
        }

        if (treatmentLine.treatmentLineEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.line_end_date", treatmentLine.treatmentLineEndDate));
        }

        if (treatmentLine.treatmentLineDrugEraStartDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.drug_era_start_date", treatmentLine.treatmentLineDrugEraStartDate));
        }

        if (treatmentLine.treatmentLineDrugEraEndDate != null) {
            whereClauses.add(this.buildDateRangeClause("C.drug_era_end_date", treatmentLine.treatmentLineDrugEraEndDate));
        }

        if (treatmentLine.treatmentLineNumber != null) {
            whereClauses.add(this.buildNumericRangeClause("C.line_number", treatmentLine.treatmentLineNumber));
        }

        if (treatmentLine.totalCycleNumber != null) {
            whereClauses.add(this.buildNumericRangeClause("C.total_cycle_number", treatmentLine.totalCycleNumber));
        }

        if (treatmentLine.drugExposureCount != null) {
            whereClauses.add(this.buildNumericRangeClause("C.drug_exposure_count", treatmentLine.drugExposureCount));
        }

        if (treatmentLine.age != null) {
            whereClauses.add(this.buildNumericRangeClause("YEAR(C.line_start_date) - P.year_of_birth", treatmentLine.age));
        }

        if (treatmentLine.gender != null && treatmentLine.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(this.getConceptIdsFromConcepts(treatmentLine.gender), ",")));
        }

        String whereClause = "";
        if (whereClauses.size() > 0) {
            whereClause = "WHERE " + StringUtils.join(whereClauses, "\nAND ");
        }

        query = StringUtils.replace(query, "@whereClause", whereClause);
        if (treatmentLine.CorrelatedCriteria != null && !treatmentLine.CorrelatedCriteria.isEmpty()) {
            query = this.wrapCriteriaQuery(query, treatmentLine.CorrelatedCriteria);
        }

        return query;
    }

    private String getDateFieldForOffsetStrategy(DateField dateField) {
        switch(dateField) {
            case StartDate:
                return "start_date";
            case EndDate:
                return "end_date";
            default:
                return "start_date";
        }
    }

    public String getStrategySql(DateOffsetStrategy strat, String eventTable) {
        String strategySql = StringUtils.replace(DATE_OFFSET_STRATEGY_TEMPLATE, "@eventTable", eventTable);
        strategySql = StringUtils.replace(strategySql, "@offset", Integer.toString(strat.offset));
        strategySql = StringUtils.replace(strategySql, "@dateField", this.getDateFieldForOffsetStrategy(strat.dateField));
        return strategySql;
    }

    public String getStrategySql(CustomEraStrategy strat, String eventTable) {
        if (strat.drugCodesetId == null) {
            throw new RuntimeException("Drug Codeset ID can not be NULL.");
        } else {
            String strategySql = StringUtils.replace(CUSTOM_ERA_STRATEGY_TEMPLATE, "@eventTable", eventTable);
            strategySql = StringUtils.replace(strategySql, "@drugCodesetId", strat.drugCodesetId.toString());
            strategySql = StringUtils.replace(strategySql, "@gapDays", Integer.toString(strat.gapDays));
            strategySql = StringUtils.replace(strategySql, "@offset", Integer.toString(strat.offset));
            return strategySql;
        }
    }

    static {
        ADDITIONAL_CRITERIA_TEMPLATE = StringUtils.replace(ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/additionalCriteria.sql"), "@windowedCriteria", WINDOWED_CRITERIA_TEMPLATE);
        GROUP_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/groupQuery.sql");
        CONDITION_ERA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/conditionEra.sql");
        CONDITION_OCCURRENCE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/conditionOccurrence.sql");
        DEATH_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/death.sql");
        DEVICE_EXPOSURE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/deviceExposure.sql");
        DOSE_ERA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/doseEra.sql");
        DRUG_ERA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/drugEra.sql");
        DRUG_EXPOSURE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/drugExposure.sql");
        TREATMENT_LINE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/treatmentLine.sql");
        MEASUREMENT_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/measurement.sql");
        OBSERVATION_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/observation.sql");
        OBSERVATION_PERIOD_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/observationPeriod.sql");
        PROCEDURE_OCCURRENCE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/procedureOccurrence.sql");
        SPECIMEN_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/specimen.sql");
        VISIT_OCCURRENCE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/visitOccurrence.sql");
        INCLUSION_RULE_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/inclusionrule.sql");
        CENSORING_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/censoringInsert.sql");
        PAYER_PLAN_PERIOD_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/payerPlanPeriod.sql");
        EVENT_TABLE_EXPRESSION_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/eventTableExpression.sql");
        DEMOGRAPHIC_CRITERIA_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/demographicCriteria.sql");
        COHORT_INCLUSION_ANALYSIS_TEMPALTE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/cohortInclusionAnalysis.sql");
        COHORT_CENSORED_STATS_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/cohortCensoredStats.sql");
        DATE_OFFSET_STRATEGY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/dateOffsetStrategy.sql");
        CUSTOM_ERA_STRATEGY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/customEraStrategy.sql");
        ERA_CONSTRUCTOR_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/eraConstructor.sql");
    }

    public static class BuildExpressionQueryOptions {
        private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
        @JsonProperty("cohortId")
        public Integer cohortId;
        @JsonProperty("cdmSchema")
        public String cdmSchema;
        @JsonProperty("targetTable")
        public String targetTable;
        @JsonProperty("resultSchema")
        public String resultSchema;
        @JsonProperty("vocabularySchema")
        public String vocabularySchema;
        @JsonProperty("generateStats")
        public boolean generateStats;

        public BuildExpressionQueryOptions() {
        }

        public static CohortExpressionQueryBuilder.BuildExpressionQueryOptions fromJson(String json) {
            try {
                CohortExpressionQueryBuilder.BuildExpressionQueryOptions options = (CohortExpressionQueryBuilder.BuildExpressionQueryOptions)JSON_MAPPER.readValue(json, CohortExpressionQueryBuilder.BuildExpressionQueryOptions.class);
                return options;
            } catch (Exception var2) {
                throw new RuntimeException("Error parsing expression query options", var2);
            }
        }
    }
}
