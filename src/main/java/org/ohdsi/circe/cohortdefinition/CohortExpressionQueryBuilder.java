package org.ohdsi.circe.cohortdefinition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.StringUtils;
import org.ohdsi.circe.cohortdefinition.builder.TreatmentLineBuilder;
import org.ohdsi.circe.cohortdefinition.builders.BuilderOptions;
import org.ohdsi.circe.cohortdefinition.builders.CriteriaSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.ConditionEraSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.ConditionOccurrenceSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.DeathSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.DeviceExposureSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.DoseEraSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.DrugEraSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.DrugExposureSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.LocationRegionSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.MeasurementSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.ObservationPeriodSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.ObservationSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.PayerPlanPeriodSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.ProcedureOccurrenceSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.SpecimenSqlBuilder;
import org.ohdsi.circe.cohortdefinition.builders.VisitOccurrenceSqlBuilder;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.circe.vocabulary.ConceptSetExpressionQueryBuilder;

import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildDateRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.buildNumericRangeClause;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.dateStringToSql;
import static org.ohdsi.circe.cohortdefinition.builders.BuilderUtils.getConceptIdsFromConcepts;
import org.ohdsi.circe.cohortdefinition.builders.CriteriaColumn;

/**
 *
 * @author cknoll1
 */
public class CohortExpressionQueryBuilder implements IGetCriteriaSqlDispatcher, IGetEndStrategySqlDispatcher {

    private final static ConceptSetExpressionQueryBuilder conceptSetQueryBuilder = new ConceptSetExpressionQueryBuilder();
    private final static String CODESET_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/codesetQuery.sql");

    private final static String COHORT_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/generateCohort.sql");

    private final static String PRIMARY_EVENTS_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/primaryEventsQuery.sql");

    private final static String WINDOWED_CRITERIA_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/windowedCriteria.sql");
    private final static String ADDITIONAL_CRITERIA_INNER_TEMPLATE = StringUtils.replace(ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/additionalCriteriaInclude.sql"), "@windowedCriteria", WINDOWED_CRITERIA_TEMPLATE);
    private final static String ADDITIONAL_CRITERIA_LEFT_TEMPLATE = StringUtils.replace(ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/additionalCriteriaExclude.sql"), "@windowedCriteria", WINDOWED_CRITERIA_TEMPLATE);
    private final static String GROUP_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/groupQuery.sql");

    private final static String PRIMARY_CRITERIA_EVENTS_TABLE = "primary_events";
    private final static String INCLUSION_RULE_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/inclusionrule.sql");
    private final static String INCLUSION_RULE_TEMP_TABLE_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/inclusionRuleTempTable.sql");
    private final static String CENSORING_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/censoringInsert.sql");

    private final static String EVENT_TABLE_EXPRESSION_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/eventTableExpression.sql");
    private final static String DEMOGRAPHIC_CRITERIA_QUERY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/demographicCriteria.sql");

    private final static String COHORT_INCLUSION_ANALYSIS_TEMPALTE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/cohortInclusionAnalysis.sql");
    private final static String COHORT_CENSORED_STATS_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/cohortCensoredStats.sql");

    // Strategy templates
    private final static String DATE_OFFSET_STRATEGY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/dateOffsetStrategy.sql");
    private final static String CUSTOM_ERA_STRATEGY_TEMPLATE = ResourceHelper.GetResourceAsString("/resources/cohortdefinition/sql/customEraStrategy.sql");

    private final static String DEFAULT_DRUG_EXPOSURE_END_DATE_EXPRESSION = "COALESCE(DRUG_EXPOSURE_END_DATE, DATEADD(day,DAYS_SUPPLY,DRUG_EXPOSURE_START_DATE), DATEADD(day,1,DRUG_EXPOSURE_START_DATE))";

    // Builders
    private final static ConditionOccurrenceSqlBuilder<ConditionOccurrence> conditionOccurrenceSqlBuilder = new ConditionOccurrenceSqlBuilder<>();
    private final static DeathSqlBuilder<Death> deathSqlBuilder = new DeathSqlBuilder<>();
    private final static DeviceExposureSqlBuilder<DeviceExposure> deviceExposureSqlBuilder = new DeviceExposureSqlBuilder<>();
    private final static DoseEraSqlBuilder<DoseEra> doseEraSqlBuilder = new DoseEraSqlBuilder<>();
    private final static DrugEraSqlBuilder<DrugEra> drugEraSqlBuilder = new DrugEraSqlBuilder<>();
    private final static DrugExposureSqlBuilder<DrugExposure> drugExposureSqlBuilder = new DrugExposureSqlBuilder<>();
    private final static LocationRegionSqlBuilder<LocationRegion> locationRegionSqlBuilder = new LocationRegionSqlBuilder<>();
    private final static MeasurementSqlBuilder<Measurement> measurementSqlBuilder = new MeasurementSqlBuilder<>();
    private final static ObservationPeriodSqlBuilder<ObservationPeriod> observationPeriodSqlBuilder = new ObservationPeriodSqlBuilder<>();
    private final static ObservationSqlBuilder<Observation> observationSqlBuilder = new ObservationSqlBuilder<>();
    private final static PayerPlanPeriodSqlBuilder<PayerPlanPeriod> payerPlanPeriodSqlBuilder = new PayerPlanPeriodSqlBuilder<>();
    private final static ProcedureOccurrenceSqlBuilder<ProcedureOccurrence> procedureOccurrenceSqlBuilder = new ProcedureOccurrenceSqlBuilder<>();
    private final static SpecimenSqlBuilder<Specimen> specimenSqlBuilder = new SpecimenSqlBuilder<>();
    private final static VisitOccurrenceSqlBuilder<VisitOccurrence> visitOccurrenceSqlBuilder = new VisitOccurrenceSqlBuilder<>();
    private final static ConditionEraSqlBuilder<ConditionEra> conditionEraSqlBuilder = new ConditionEraSqlBuilder<>();
    private final static TreatmentLineBuilder<TreatmentLine> treatmentLineBuilder = new TreatmentLineBuilder<>();
    private final static String DEFAULT_COHORT_ID_FIELD_NAME = "cohort_definition_id";

    public static class BuildExpressionQueryOptions {

        private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

        @JsonProperty("cohortIdFieldName")
        public String cohortIdFieldName;

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

        public static CohortExpressionQueryBuilder.BuildExpressionQueryOptions fromJson(String json) {
            try {
                CohortExpressionQueryBuilder.BuildExpressionQueryOptions options
                        = JSON_MAPPER.readValue(json, CohortExpressionQueryBuilder.BuildExpressionQueryOptions.class);
                return options;
            } catch (Exception e) {
                throw new RuntimeException("Error parsing expression query options", e);
            }
        }

    }

    private String getOccurrenceOperator(int type) {
        // Occurance check { id: 0, name: 'Exactly', id: 1, name: 'At Most' }, { id: 2, name: 'At Least' }
        switch (type) {
            case 0:
                return "=";
            case 1:
                return "<=";
            case 2:
                return ">=";
        }

        throw new RuntimeException(String.format("Invalid occurrene operator recieved: type=%d.",type));

    }

    private String getAdditionalColumns(List<CriteriaColumn> columns, String prefix) {
        return String.join(",", columns.stream().map((column) -> { return prefix + column.columnName();}).collect(Collectors.toList()));
    }

    private String wrapCriteriaQuery(String query, CriteriaGroup group) {
        String eventQuery = StringUtils.replace(EVENT_TABLE_EXPRESSION_TEMPLATE, "@eventQuery", query);
        String groupQuery = this.getCriteriaGroupQuery(group, String.format("(%s)", eventQuery));
        groupQuery = StringUtils.replace(groupQuery, "@indexId", "" + 0);
        String wrappedQuery = String.format(
                "select PE.person_id, PE.event_id, PE.start_date, PE.end_date, PE.visit_occurrence_id, PE.sort_date FROM (\n%s\n) PE\nJOIN (\n%s) AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id\n",
                query, groupQuery);
        return wrappedQuery;
    }

    public String getCodesetQuery(ConceptSet[] conceptSets) {

        if (conceptSets == null || conceptSets.length <= 0) {
            return StringUtils.replace(
                    CODESET_QUERY_TEMPLATE,
                    "@codesetInserts",
                    StringUtils.EMPTY
            );
        }

        String unionSelectsQuery = Arrays.stream(conceptSets)
                .map(cs -> String.format("SELECT %d as codeset_id, c.concept_id FROM (%s) C", cs.id, conceptSetQueryBuilder.buildExpressionQuery(cs.expression)))
                .collect(Collectors.joining(" UNION ALL \n"));

        String queryWithInsert = StringUtils.replace(
                CODESET_QUERY_TEMPLATE,
                "@codesetInserts",
                "INSERT INTO #Codesets (codeset_id, concept_id)\n" + unionSelectsQuery
        );
        return queryWithInsert + ";";

    }

    private String getCensoringEventsQuery(Criteria[] censoringCriteria) {
        ArrayList<String> criteriaQueries = new ArrayList<>();
        for (Criteria c : censoringCriteria) {
            String criteriaQuery = c.accept(this);
            criteriaQueries.add(StringUtils.replace(CENSORING_QUERY_TEMPLATE, "@criteriaQuery", criteriaQuery));
        }

        return StringUtils.join(criteriaQueries, "\nUNION ALL\n");
    }

    public String getPrimaryEventsQuery(PrimaryCriteria primaryCriteria) {
        String query = PRIMARY_EVENTS_TEMPLATE;

        ArrayList<String> criteriaQueries = new ArrayList<>();

        for (Criteria c : primaryCriteria.criteriaList) {
            criteriaQueries.add(c.accept(this));
        }

        query = StringUtils.replace(query, "@criteriaQueries", StringUtils.join(criteriaQueries, "\nUNION ALL\n"));

        ArrayList<String> primaryEventsFilters = new ArrayList<>();
        primaryEventsFilters.add(String.format(
                "DATEADD(day,%d,OP.OBSERVATION_PERIOD_START_DATE) <= E.START_DATE AND DATEADD(day,%d,E.START_DATE) <= OP.OBSERVATION_PERIOD_END_DATE",
                primaryCriteria.observationWindow.priorDays,
                primaryCriteria.observationWindow.postDays
                )
        );

        query = StringUtils.replace(query, "@primaryEventsFilter", StringUtils.join(primaryEventsFilters, " AND "));

        query = StringUtils.replace(query, "@EventSort", (primaryCriteria.primaryLimit.type != null && primaryCriteria.primaryLimit.type.equalsIgnoreCase("LAST")) ? "DESC" : "ASC");
        query = StringUtils.replace(query, "@primaryEventLimit", (!primaryCriteria.primaryLimit.type.equalsIgnoreCase("ALL") ? "WHERE P.ordinal = 1" : ""));

        return query;
    }

    public String getFinalCohortQuery(Period censorWindow) {

        String query = "select @target_cohort_id as @cohort_id_field_name, person_id, @start_date, @end_date \n"
                + "FROM #final_cohort CO";

        String startDate = "start_date";
        String endDate = "end_date";

        if (censorWindow != null && (censorWindow.startDate != null || censorWindow.endDate != null)) {
            if (censorWindow.startDate != null) {
                String censorStartDate = dateStringToSql(censorWindow.startDate);
                startDate = "CASE WHEN start_date > " + censorStartDate + " THEN start_date ELSE " + censorStartDate + " END";
            }
            if (censorWindow.endDate != null) {
                String censorEndDate = dateStringToSql(censorWindow.endDate);
                endDate = "CASE WHEN end_date < " + censorEndDate + " THEN end_date ELSE " + censorEndDate + " END";
            }
            query += "\nWHERE @start_date <= @end_date";
        }

        query = StringUtils.replace(query, "@start_date", startDate);
        query = StringUtils.replace(query, "@end_date", endDate);

        return query;
    }

    private String getInclusionRuleTableSql(CohortExpression expression) {
        String EMPTY_TABLE = "CREATE TABLE #inclusion_rules (rule_sequence int);";
        if (expression.inclusionRules.size() == 0 ) return EMPTY_TABLE;

        String UNION_TEMPLATE = "SELECT CAST(%d as int) as rule_sequence";
        List<String> unionList = IntStream.range(0,expression.inclusionRules.size())
                .mapToObj(i -> (String)String.format(UNION_TEMPLATE, i))
                .collect(Collectors.toList());

        return StringUtils.replace(INCLUSION_RULE_TEMP_TABLE_TEMPLATE, "@inclusionRuleUnions", StringUtils.join(unionList, " UNION ALL "));
    }
    private String getInclusionAnalysisQuery(String eventTable, int modeId) {
        String resultSql = COHORT_INCLUSION_ANALYSIS_TEMPALTE;
        resultSql = StringUtils.replace(resultSql, "@inclusionImpactMode", Integer.toString(modeId));
        resultSql = StringUtils.replace(resultSql, "@eventTable", eventTable);
        return resultSql;
    }

    public String buildExpressionQuery(String expression, BuildExpressionQueryOptions options) {
        return this.buildExpressionQuery(CohortExpression.fromJson(expression), options);
    }

    public String buildExpressionQuery(CohortExpression expression, BuildExpressionQueryOptions options) {
        String resultSql = COHORT_QUERY_TEMPLATE;

        String codesetQuery = getCodesetQuery(expression.conceptSets);
        resultSql = StringUtils.replace(resultSql, "@codesetQuery", codesetQuery);

        String primaryEventsQuery = getPrimaryEventsQuery(expression.primaryCriteria);
        resultSql = StringUtils.replace(resultSql, "@primaryEventsQuery", primaryEventsQuery);

        String additionalCriteriaQuery = "";
        if (expression.additionalCriteria != null && !expression.additionalCriteria.isEmpty()) {
            CriteriaGroup acGroup = expression.additionalCriteria;
            String acGroupQuery = this.getCriteriaGroupQuery(acGroup, PRIMARY_CRITERIA_EVENTS_TABLE);//acGroup.accept(this);
            acGroupQuery = StringUtils.replace(acGroupQuery, "@indexId", "" + 0);
            additionalCriteriaQuery = "\nJOIN (\n" + acGroupQuery + ") AC on AC.person_id = pe.person_id and AC.event_id = pe.event_id\n";
        }
        resultSql = StringUtils.replace(resultSql, "@additionalCriteriaQuery", additionalCriteriaQuery);

        resultSql = StringUtils.replace(resultSql, "@QualifiedEventSort", (expression.qualifiedLimit.type != null && expression.qualifiedLimit.type.equalsIgnoreCase("LAST")) ? "DESC" : "ASC");

        // Only apply qualified limit filter if additional criteria is specified.
        if (expression.additionalCriteria != null && expression.qualifiedLimit.type != null && !expression.qualifiedLimit.type.equalsIgnoreCase("ALL")) {
            resultSql = StringUtils.replace(resultSql, "@QualifiedLimitFilter", "WHERE QE.ordinal = 1");
        } else {
            resultSql = StringUtils.replace(resultSql, "@QualifiedLimitFilter", "");
        }

        if (expression.inclusionRules.size() > 0) {
            ArrayList<String> inclusionRuleInserts = new ArrayList<>();
            ArrayList<String> inclusionRuleTempTables = new ArrayList<>();

            for (int i = 0; i < expression.inclusionRules.size(); i++) {
                CriteriaGroup cg = expression.inclusionRules.get(i).expression;
                String inclusionRuleInsert = getInclusionRuleQuery(cg);
                inclusionRuleInsert = StringUtils.replace(inclusionRuleInsert, "@inclusion_rule_id", "" + i);
                inclusionRuleInserts.add(inclusionRuleInsert);
                inclusionRuleTempTables.add(String.format("#Inclusion_%d", i));
            }

            String irTempUnion = inclusionRuleTempTables.stream()
                    .map(d -> String.format("select inclusion_rule_id, person_id, event_id from %s", d))
                    .collect(Collectors.joining("\nUNION ALL\n"));

            inclusionRuleInserts.add(String.format("SELECT inclusion_rule_id, person_id, event_id\nINTO #inclusion_events\nFROM (%s) I;", irTempUnion));

            inclusionRuleInserts.addAll(inclusionRuleTempTables.stream()
                    .map(d -> String.format("TRUNCATE TABLE %s;\nDROP TABLE %s;\n", d, d))
                    .collect(Collectors.toList())
            );
            resultSql = StringUtils.replace(resultSql, "@inclusionCohortInserts", StringUtils.join(inclusionRuleInserts, "\n"));
        } else {
            resultSql = StringUtils.replace(resultSql, "@inclusionCohortInserts", "create table #inclusion_events (inclusion_rule_id bigint,\n\tperson_id bigint,\n\tevent_id bigint\n);");
        }

        resultSql = StringUtils.replace(resultSql, "@IncludedEventSort", (expression.expressionLimit.type != null && expression.expressionLimit.type.equalsIgnoreCase("LAST")) ? "DESC" : "ASC");

        if (expression.expressionLimit.type != null && !expression.expressionLimit.type.equalsIgnoreCase("ALL")) {
            resultSql = StringUtils.replace(resultSql, "@ResultLimitFilter", "WHERE Results.ordinal = 1");
        } else {
            resultSql = StringUtils.replace(resultSql, "@ResultLimitFilter", "");
        }

        resultSql = StringUtils.replace(resultSql, "@ruleTotal", String.valueOf(expression.inclusionRules.size()));

        ArrayList<String> endDateSelects = new ArrayList<>();

        if (!(expression.endStrategy instanceof DateOffsetStrategy)) {
            endDateSelects.add("-- By default, cohort exit at the event's op end date\nselect event_id, person_id, op_end_date as end_date from #included_events");
        }

        if (expression.endStrategy != null) {
            // replace @strategy_ends placeholders with temp table creation and cleanup scripts.
            resultSql = StringUtils.replace(resultSql, "@strategy_ends_temp_tables", expression.endStrategy.accept(this, "#included_events"));
            resultSql = StringUtils.replace(resultSql, "@strategy_ends_cleanup", "TRUNCATE TABLE #strategy_ends;\nDROP TABLE #strategy_ends;\n");
            endDateSelects.add(String.format("-- End Date Strategy\n%s\n", "SELECT event_id, person_id, end_date from #strategy_ends"));
        } else {
            // replace @trategy_ends placeholders with empty string
            resultSql = StringUtils.replace(resultSql, "@strategy_ends_temp_tables", "");
            resultSql = StringUtils.replace(resultSql, "@strategy_ends_cleanup", "");
        }

        if (expression.censoringCriteria != null && expression.censoringCriteria.length > 0) {
            endDateSelects.add(String.format("-- Censor Events\n%s\n", getCensoringEventsQuery(expression.censoringCriteria)));
        }

        resultSql = StringUtils.replace(resultSql, "@finalCohortQuery", getFinalCohortQuery(expression.censorWindow));

        resultSql = StringUtils.replace(resultSql, "@cohort_end_unions", StringUtils.join(endDateSelects, "\nUNION ALL\n"));

        resultSql = StringUtils.replace(resultSql, "@eraconstructorpad", Integer.toString(expression.collapseSettings.eraPad));

        resultSql = StringUtils.replace(resultSql, "@inclusionRuleTable", getInclusionRuleTableSql(expression));
        resultSql = StringUtils.replace(resultSql, "@inclusionImpactAnalysisByEventQuery", getInclusionAnalysisQuery("#qualified_events", 0));
        resultSql = StringUtils.replace(resultSql, "@inclusionImpactAnalysisByPersonQuery", getInclusionAnalysisQuery("#best_events", 1));

        resultSql = StringUtils.replace(resultSql, "@cohortCensoredStatsQuery",
                (expression.censorWindow != null && (!StringUtils.isEmpty(expression.censorWindow.startDate) || !StringUtils.isEmpty(expression.censorWindow.endDate)))
                        ? COHORT_CENSORED_STATS_TEMPLATE
                        : "");

        if (options != null) {
            // replease query parameters with tokens
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

            if (options.cohortIdFieldName != null) {
                resultSql = StringUtils.replaceAll(resultSql, "@cohort_id_field_name", options.cohortIdFieldName.toString());
            } else {
                resultSql = StringUtils.replaceAll(resultSql, "@cohort_id_field_name", DEFAULT_COHORT_ID_FIELD_NAME);
            }
        } else {
            resultSql = StringUtils.replaceAll(resultSql, "@cohort_id_field_name", DEFAULT_COHORT_ID_FIELD_NAME);
        }
        return resultSql;
    }

    public String getCriteriaGroupQuery(CriteriaGroup group, String eventTable) {
        String query = GROUP_QUERY_TEMPLATE;
        ArrayList<String> additionalCriteriaQueries = new ArrayList<>();
        String joinType = "INNER";

        int indexId = 0;
        for (CorelatedCriteria cc : group.criteriaList) {
            String acQuery = this.getCorelatedlCriteriaQuery(cc, eventTable); //ac.accept(this);
            acQuery = StringUtils.replace(acQuery, "@indexId", "" + indexId);
            additionalCriteriaQueries.add(acQuery);
            indexId++;
        }

        for (DemographicCriteria dc : group.demographicCriteriaList) {
            String dcQuery = this.getDemographicCriteriaQuery(dc, eventTable); //ac.accept(this);
            dcQuery = StringUtils.replace(dcQuery, "@indexId", "" + indexId);
            additionalCriteriaQueries.add(dcQuery);
            indexId++;
        }

        for (CriteriaGroup g : group.groups) {
            String gQuery = this.getCriteriaGroupQuery(g, eventTable); //g.accept(this);
            gQuery = StringUtils.replace(gQuery, "@indexId", "" + indexId);
            additionalCriteriaQueries.add(gQuery);
            indexId++;
        }

        if (!group.isEmpty())
        {
            query = StringUtils.replace(query, "@criteriaQueries", StringUtils.join(additionalCriteriaQueries, "\nUNION ALL\n"));

            String occurrenceCountClause = "HAVING COUNT(index_id) ";
            if (group.type.equalsIgnoreCase("ALL")) // count must match number of criteria + sub-groups in group.
            {
                occurrenceCountClause += "= " + indexId;
            }

            if (group.type.equalsIgnoreCase("ANY")) // count must be > 0 for an 'ANY' criteria
            {
                occurrenceCountClause += "> 0";
            }

            if (group.type.toUpperCase().startsWith("AT_")) {
                if (group.type.toUpperCase().endsWith("LEAST")) { // AT_LEAST
                    occurrenceCountClause += ">= " + group.count;
                } else { // AT_MOST, which includes zero
                    occurrenceCountClause += "<= " + group.count;
                    joinType = "LEFT";
                }

                if (group.count == 0) { //if you are looking for a zero count within an AT_LEAST/AT_MOST, you need to do a left join
                    joinType = "LEFT";
                }
            }

            query = StringUtils.replace(query, "@occurrenceCountClause", occurrenceCountClause);
            query = StringUtils.replace(query, "@joinType", joinType);
        } else // query group is empty so replace group query with a friendly default
        {
            query = "-- Begin Criteria Group\n select @indexId as index_id, person_id, event_id FROM @eventTable\n-- End Criteria Group\n";
        }

        query = StringUtils.replace(query, "@eventTable", eventTable);

        return query;
    }

    private String getInclusionRuleQuery(CriteriaGroup inclusionRule) {
        String resultSql = INCLUSION_RULE_QUERY_TEMPLATE;
        String additionalCriteriaQuery = "\nJOIN (\n" + getCriteriaGroupQuery(inclusionRule, "#qualified_events") + ") AC on AC.person_id = pe.person_id AND AC.event_id = pe.event_id";
        additionalCriteriaQuery = StringUtils.replace(additionalCriteriaQuery, "@indexId", "" + 0);
        resultSql = StringUtils.replace(resultSql, "@additionalCriteriaQuery", additionalCriteriaQuery);
        return resultSql;
    }

    public String getDemographicCriteriaQuery(DemographicCriteria criteria, String eventTable) {
        String query = DEMOGRAPHIC_CRITERIA_QUERY_TEMPLATE;
        query = StringUtils.replace(query, "@eventTable", eventTable);

        ArrayList<String> whereClauses = new ArrayList<>();

        // Age
        if (criteria.age != null) {
            whereClauses.add(buildNumericRangeClause("YEAR(E.start_date) - P.year_of_birth", criteria.age));
        }

        // Gender
        if (criteria.gender != null && criteria.gender.length > 0) {
            whereClauses.add(String.format("P.gender_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.gender), ",")));
        }

        // Race
        if (criteria.race != null && criteria.race.length > 0) {
            whereClauses.add(String.format("P.race_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.race), ",")));
        }

        // Race
        if (criteria.race != null && criteria.race.length > 0) {
            whereClauses.add(String.format("P.race_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.race), ",")));
        }

        // Ethnicity
        if (criteria.ethnicity != null && criteria.ethnicity.length > 0) {
            whereClauses.add(String.format("P.ethnicity_concept_id in (%s)", StringUtils.join(getConceptIdsFromConcepts(criteria.ethnicity), ",")));
        }

        // occurrenceStartDate
        if (criteria.occurrenceStartDate != null) {
            whereClauses.add(buildDateRangeClause("E.start_date", criteria.occurrenceStartDate));
        }

        // occurrenceEndDate
        if (criteria.occurrenceEndDate != null) {
            whereClauses.add(buildDateRangeClause("E.end_date", criteria.occurrenceEndDate));
        }

        if (whereClauses.size() > 0) {
            query = StringUtils.replace(query, "@whereClause", "WHERE " + StringUtils.join(whereClauses, " AND "));
        } else {
            query = StringUtils.replace(query, "@whereClause", "");
        }

        return query;
    }

    public String getWindowedCriteriaQuery(String sqlTemplate, WindowedCriteria criteria, String eventTable, BuilderOptions options) {

        String query = sqlTemplate;
        boolean checkObservationPeriod = !criteria.ignoreObservationPeriod;

        String criteriaQuery = criteria.criteria.accept(this, options);
        query = StringUtils.replace(query, "@criteriaQuery", criteriaQuery);
        query = StringUtils.replace(query, "@eventTable", eventTable);
        if (options != null && options.additionalColumns.size() > 0) {
            query = StringUtils.replace(query, "@additionalColumns", ", " + getAdditionalColumns(options.additionalColumns, "A."));
        } else {
            query = StringUtils.replace(query, "@additionalColumns", "");
        }

        // build index date window expression
        String startExpression;
        String endExpression;
        List<String> clauses = new ArrayList<>();
        if (checkObservationPeriod) {
            clauses.add("A.START_DATE >= P.OP_START_DATE AND A.START_DATE <= P.OP_END_DATE");
        }

        // StartWindow
        Window startWindow = criteria.startWindow;
        String startIndexDateExpression = (startWindow.useIndexEnd != null && startWindow.useIndexEnd) ? "P.END_DATE" : "P.START_DATE";
        String startEventDateExpression = (startWindow.useEventEnd != null && startWindow.useEventEnd) ? "A.END_DATE" : "A.START_DATE";
        if (startWindow.start.days != null) {
            startExpression = String.format("DATEADD(day,%d,%s)", startWindow.start.coeff * startWindow.start.days, startIndexDateExpression);
        } else {
            startExpression = checkObservationPeriod ? (startWindow.start.coeff == -1 ? "P.OP_START_DATE" : "P.OP_END_DATE") : null;
        }

        if (startExpression != null) {
            clauses.add(String.format("%s >= %s", startEventDateExpression, startExpression));
        }

        if (startWindow.end.days != null) {
            endExpression = String.format("DATEADD(day,%d,%s)", startWindow.end.coeff * startWindow.end.days, startIndexDateExpression);
        } else {
            endExpression = checkObservationPeriod ? (startWindow.end.coeff == -1 ? "P.OP_START_DATE" : "P.OP_END_DATE") : null;
        }

        if (endExpression != null) {
            clauses.add(String.format("%s <= %s", startEventDateExpression, endExpression));
        }

        // EndWindow
        Window endWindow = criteria.endWindow;

        if (endWindow != null) {
            String endIndexDateExpression = (endWindow.useIndexEnd != null && endWindow.useIndexEnd) ? "P.END_DATE" : "P.START_DATE";
            // for backwards compatability, having a null endWindow.useIndexEnd means they SHOULD use the index end date.
            String endEventDateExpression = (endWindow.useEventEnd == null || endWindow.useEventEnd) ? "A.END_DATE" : "A.START_DATE";
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

        // RestrictVisit
        boolean restrictVisit = criteria.restrictVisit;
        if (restrictVisit) {
            clauses.add("A.visit_occurrence_id = P.visit_occurrence_id");
        }

        query = StringUtils.replace(query, "@windowCriteria", clauses.size() > 0 ? " AND " + StringUtils.join(clauses, " AND ") : "");

        return query;
    }

    public String getWindowedCriteriaQuery(WindowedCriteria criteria, String eventTable) {
        String query = getWindowedCriteriaQuery(WINDOWED_CRITERIA_TEMPLATE, criteria, eventTable, null);
        return query;
    }

    public String getWindowedCriteriaQuery(WindowedCriteria criteria, String eventTable, BuilderOptions options) {
        String query = getWindowedCriteriaQuery(WINDOWED_CRITERIA_TEMPLATE, criteria, eventTable, options);
        return query;
    }

    public String getCorelatedlCriteriaQuery(CorelatedCriteria corelatedCriteria, String eventTable) {

        // pick the appropraite query template that is optimized for include (at least 1) or exclude (allow 0)
        String query = (corelatedCriteria.occurrence.type == Occurrence.AT_MOST || corelatedCriteria.occurrence.count == 0) ? ADDITIONAL_CRITERIA_LEFT_TEMPLATE : ADDITIONAL_CRITERIA_INNER_TEMPLATE;

        String countColumnExpression = "cc.event_id";

        BuilderOptions builderOptions = new BuilderOptions();
        if (corelatedCriteria.occurrence.isDistinct) {
            if (corelatedCriteria.occurrence.countColumn == null) { // backwards compatability:  default column uses domain_concept_id
                builderOptions.additionalColumns.add(CriteriaColumn.DOMAIN_CONCEPT);
                countColumnExpression = String.format("cc.%s", CriteriaColumn.DOMAIN_CONCEPT.columnName());
            } else {
                builderOptions.additionalColumns.add(corelatedCriteria.occurrence.countColumn);
                countColumnExpression = String.format("cc.%s", corelatedCriteria.occurrence.countColumn.columnName());

            }

        }
        query = getWindowedCriteriaQuery(query, corelatedCriteria, eventTable, builderOptions);

        // Occurrence criteria
        String occurrenceCriteria = String.format(
                "HAVING COUNT(%s%s) %s %d",
                corelatedCriteria.occurrence.isDistinct ? "DISTINCT " : "",
                countColumnExpression,
                getOccurrenceOperator(corelatedCriteria.occurrence.type),
                corelatedCriteria.occurrence.count
        );

        query = StringUtils.replace(query, "@occurrenceCriteria", occurrenceCriteria);

        return query;
    }

// <editor-fold defaultstate="collapsed" desc="ICriteriaSqlDispatcher implementation">

    protected <T extends Criteria> String getCriteriaSql(CriteriaSqlBuilder<T> builder, T criteria, BuilderOptions options) {
        String query = builder.getCriteriaSql(criteria, options);
        return processCorrelatedCriteria(query, criteria);
    }

    protected <T extends Criteria> String getCriteriaSql(CriteriaSqlBuilder<T> builder, T criteria) {
        return this.getCriteriaSql(builder, criteria, null);
    }

    protected String processCorrelatedCriteria(String query, Criteria criteria) {
        if (criteria.CorrelatedCriteria != null && !criteria.CorrelatedCriteria.isEmpty()) {
            query = wrapCriteriaQuery(query, criteria.CorrelatedCriteria);
        }
        return query;
    }

    @Override
    public String getCriteriaSql(ConditionEra criteria, BuilderOptions options) {
        return getCriteriaSql(conditionEraSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(ConditionOccurrence criteria, BuilderOptions options) {
        return getCriteriaSql(conditionOccurrenceSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(Death criteria, BuilderOptions options) {
        return getCriteriaSql(deathSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(DeviceExposure criteria, BuilderOptions options) {
        return getCriteriaSql(deviceExposureSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(DoseEra criteria, BuilderOptions options) {
        return getCriteriaSql(doseEraSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(DrugEra criteria, BuilderOptions options) {
        return getCriteriaSql(drugEraSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(DrugExposure criteria, BuilderOptions options) {
        return getCriteriaSql(drugExposureSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(Measurement criteria, BuilderOptions options) {
        return getCriteriaSql(measurementSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(Observation criteria, BuilderOptions options) {
        return getCriteriaSql(observationSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(ObservationPeriod criteria, BuilderOptions options) {
        return getCriteriaSql(observationPeriodSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(PayerPlanPeriod criteria, BuilderOptions options) {
        return getCriteriaSql(payerPlanPeriodSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(ProcedureOccurrence criteria, BuilderOptions options) {
        return getCriteriaSql(procedureOccurrenceSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(Specimen criteria, BuilderOptions options) {
        return getCriteriaSql(specimenSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(VisitOccurrence criteria, BuilderOptions options) {
        return getCriteriaSql(visitOccurrenceSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(LocationRegion criteria, BuilderOptions options) {
        return getCriteriaSql(locationRegionSqlBuilder, criteria, options);
    }

    @Override
    public String getCriteriaSql(TreatmentLine criteria, BuilderOptions options) {
        return getCriteriaSql(treatmentLineBuilder, criteria, options);
    }

    // </editor-fold>
// <editor-fold defaultstate="collapsed" desc="IEndStrategyDispatcher implementation">
    private String getDateFieldForOffsetStrategy(DateOffsetStrategy.DateField dateField) {
        switch (dateField) {
            case StartDate:
                return "start_date";
            case EndDate:
                return "end_date";
        }
        return "start_date";
    }

    @Override
    public String getStrategySql(DateOffsetStrategy strat, String eventTable) {
        String strategySql = StringUtils.replace(DATE_OFFSET_STRATEGY_TEMPLATE, "@eventTable", eventTable);
        strategySql = StringUtils.replace(strategySql, "@offset", Integer.toString(strat.offset));
        strategySql = StringUtils.replace(strategySql, "@dateField", getDateFieldForOffsetStrategy(strat.dateField));

        return strategySql;
    }

    @Override
    public String getStrategySql(CustomEraStrategy strat, String eventTable) {

        if (strat.drugCodesetId == null) {
            throw new RuntimeException("Drug Codeset ID can not be NULL.");
        }

        String drugExposureEndDateExpression = DEFAULT_DRUG_EXPOSURE_END_DATE_EXPRESSION;
        if (strat.daysSupplyOverride != null) {
            drugExposureEndDateExpression = String.format("DATEADD(day,%d,DRUG_EXPOSURE_START_DATE)", strat.daysSupplyOverride);
        }
        String strategySql = StringUtils.replace(CUSTOM_ERA_STRATEGY_TEMPLATE, "@eventTable", eventTable);
        strategySql = StringUtils.replace(strategySql, "@drugCodesetId", strat.drugCodesetId.toString());
        strategySql = StringUtils.replace(strategySql, "@gapDays", Integer.toString(strat.gapDays));
        strategySql = StringUtils.replace(strategySql, "@offset", Integer.toString(strat.offset));
        strategySql = StringUtils.replace(strategySql, "@drugExposureEndDateExpression", drugExposureEndDateExpression);

        return strategySql;
    }

// </editor-fold>
}