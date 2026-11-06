package org.ohdsi.webapi.mcp;

import static org.ohdsi.webapi.mcp.ToolDefinition.booleanParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.integerParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.objectParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.stringParam;
import static org.ohdsi.webapi.mcp.ToolDefinition.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.ohdsi.circe.check.Checker;
import org.ohdsi.circe.check.Warning;
import org.ohdsi.circe.check.warnings.BaseWarning;
import org.ohdsi.circe.cohortdefinition.CohortExpression;
import org.ohdsi.circe.cohortdefinition.CohortExpressionQueryBuilder.BuildExpressionQueryOptions;
import org.ohdsi.circe.cohortdefinition.ConceptSet;
import org.ohdsi.circe.vocabulary.ConceptSetExpression;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.vocabulary.Concept;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionService;
import org.ohdsi.webapi.cohortdefinition.ExpressionType;
import org.ohdsi.webapi.cohortdefinition.dto.CohortDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortGenerationInfoDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortRawDTO;
import org.ohdsi.webapi.cohortdefinition.InclusionRuleReport;
import org.ohdsi.webapi.job.JobExecutionResource;
import org.ohdsi.webapi.mcp.ToolDefinition.Rationale;
import org.ohdsi.webapi.mcp.audit.McpAuditEntry.EntityType;
import org.ohdsi.webapi.service.VocabularyService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon.DaimonType;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * STEP 3-5 tools: compose cohort definitions, validate them and their SQL, generate them on a CDM
 * source and read back the counts.
 */
@Component
@ConditionalOnProperty(prefix = "spring.ai.mcp.server", name = "enabled", havingValue = "true")
public class CohortMcpTools implements McpToolProvider {

    private final CohortDefinitionService cohortService;
    private final VocabularyService vocabularyService;
    private final SourceService sourceService;
    private final VocabularyMcpTools vocabularyTools;
    private final ObjectMapper mapper;

    public CohortMcpTools(CohortDefinitionService cohortService, VocabularyService vocabularyService,
            SourceService sourceService, VocabularyMcpTools vocabularyTools, ObjectMapper mapper) {
        this.cohortService = cohortService;
        this.vocabularyService = vocabularyService;
        this.sourceService = sourceService;
        this.vocabularyTools = vocabularyTools;
        this.mapper = mapper;
    }

    @Override
    public List<ToolDefinition> tools() {
        return List.of(
                tool("create_cohort_definition")
                        .title("Create cohort definition")
                        .description("Saves a new cohort definition from an OHDSI (Circe) cohort expression. Every "
                                + "concept in the expression's ConceptSets is checked against the source vocabulary "
                                + "first; the call is refused if any id is unknown or its name does not match.")
                        .step(ReasoningStep.STEP_3_COHORT_CRITERIA_DEFINITION)
                        .rationale(Rationale.REQUIRED)
                        .requiredParam("name", stringParam("Cohort definition name; must be unique in WebAPI."))
                        .param("description", stringParam("Plain-language summary of the phenotype logic."))
                        .requiredParam("expression", objectParam("The Circe cohort expression (ConceptSets, "
                                + "PrimaryCriteria, InclusionRules, EndStrategy, ...), as JSON."))
                        .requiredParam("source_key", stringParam("Vocabulary source used to verify the concepts."))
                        .entity(EntityType.COHORT_DEFINITION, "cohort_definition_id")
                        .handler(this::create)
                        .build(),
                tool("update_cohort_definition")
                        .title("Update cohort definition")
                        .description("Replaces the expression (and optionally name/description) of a cohort "
                                + "definition. The previous version is kept in the definition's version history. "
                                + "Concepts are verified as in create_cohort_definition.")
                        .step(ReasoningStep.STEP_3_COHORT_CRITERIA_DEFINITION)
                        .rationale(Rationale.REQUIRED)
                        .requiredParam("cohort_definition_id", integerParam("The cohort definition to change."))
                        .requiredParam("expression", objectParam("The complete new Circe cohort expression."))
                        .requiredParam("source_key", stringParam("Vocabulary source used to verify the concepts."))
                        .param("name", stringParam("New name; unchanged if omitted."))
                        .param("description", stringParam("New description; unchanged if omitted."))
                        .entity(EntityType.COHORT_DEFINITION, "cohort_definition_id")
                        .handler(this::update)
                        .build(),
                tool("get_cohort_definition")
                        .title("Get cohort definition")
                        .description("Returns a cohort definition's name, description and Circe expression.")
                        .step(ReasoningStep.STEP_3_COHORT_CRITERIA_DEFINITION)
                        .readOnly()
                        .requiredParam("cohort_definition_id", integerParam("The cohort definition id."))
                        .handler(this::get)
                        .build(),
                tool("validate_cohort_definition")
                        .title("Validate cohort definition and SQL")
                        .description("Validates a cohort expression (pass 'expression', or 'cohort_definition_id' for "
                                + "a saved one): runs the Circe logic checker, verifies every concept against the "
                                + "source vocabulary, builds the cohort SQL and translates it to the source's SQL "
                                + "dialect (or 'target_dialect'). Translation proves SqlRender can produce SQL for the "
                                + "dialect; the SQL is not executed. valid is false if any check reports an error.")
                        .step(ReasoningStep.STEP_4_SQL_VALIDATION)
                        .readOnly()
                        .param("cohort_definition_id", integerParam("A saved cohort definition to validate."))
                        .param("expression", objectParam("An unsaved Circe cohort expression to validate."))
                        .requiredParam("source_key", stringParam("Source whose vocabulary, schemas and dialect to use."))
                        .param("target_dialect", stringParam("SqlRender dialect to translate to, e.g. 'postgresql', "
                                + "'sql server', 'oracle', 'redshift', 'spark', 'snowflake', 'bigquery'. Defaults to "
                                + "the source's dialect."))
                        .param("include_sql", booleanParam("Return the translated SQL. Default false."))
                        .entity(EntityType.COHORT_DEFINITION, "cohort_definition_id")
                        .handler(this::validate)
                        .build(),
                tool("generate_cohort")
                        .title("Generate cohort")
                        .description("Queues generation of a saved cohort definition on a CDM source. This runs "
                                + "the cohort SQL against the CDM and replaces earlier results for that source. Poll "
                                + "get_cohort_generation_status for the outcome.")
                        .step(ReasoningStep.STEP_5_GENERATION_VERIFICATION)
                        .rationale(Rationale.OPTIONAL)
                        .requiredParam("cohort_definition_id", integerParam("The cohort definition to generate."))
                        .requiredParam("source_key", stringParam("CDM source to generate on."))
                        .entity(EntityType.COHORT_DEFINITION, "cohort_definition_id")
                        .handler(this::generate)
                        .build(),
                tool("get_cohort_generation_status")
                        .title("Get cohort generation status")
                        .description("Returns the latest generation of a cohort definition on a source: status, "
                                + "person and record counts, failure message, and once complete the inclusion-rule "
                                + "attrition (how many people each rule keeps) for plausibility review.")
                        .step(ReasoningStep.STEP_5_GENERATION_VERIFICATION)
                        .readOnly()
                        .requiredParam("cohort_definition_id", integerParam("The cohort definition id."))
                        .requiredParam("source_key", stringParam("CDM source the cohort was generated on."))
                        .handler(this::status)
                        .build());
    }

    // ---- STEP 3 -------------------------------------------------------------------------

    private Map<String, Object> create(ToolArguments args) {
        String sourceKey = vocabularyTools.requireSource(args);
        CohortExpression expression = parseExpression(args.raw("expression"));
        requireVerifiedConcepts(sourceKey, expression);

        CohortDTO dto = new CohortDTO();
        dto.setName(args.string("name"));
        dto.setDescription(args.optString("description"));
        dto.setExpressionType(ExpressionType.SIMPLE_EXPRESSION);
        dto.setExpression(expression);
        CohortDTO created = cohortService.createCohortDefinition(dto);
        return saved(created);
    }

    private Map<String, Object> update(ToolArguments args) {
        int id = args.intValue("cohort_definition_id");
        String sourceKey = vocabularyTools.requireSource(args);
        CohortExpression expression = parseExpression(args.raw("expression"));
        requireVerifiedConcepts(sourceKey, expression);

        CohortRawDTO current = cohortService.getCohortDefinitionRaw(id); // enforces read access
        CohortDTO dto = new CohortDTO();
        dto.setName(args.has("name") ? args.string("name") : current.getName());
        dto.setDescription(args.has("description") ? args.optString("description") : current.getDescription());
        dto.setExpressionType(ExpressionType.SIMPLE_EXPRESSION);
        dto.setExpression(expression);
        return saved(cohortService.saveCohortDefinition(id, dto));
    }

    private Map<String, Object> get(ToolArguments args) {
        CohortRawDTO raw = cohortService.getCohortDefinitionRaw(args.intValue("cohort_definition_id"));
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("cohort_definition_id", raw.getId());
        out.put("name", raw.getName());
        out.put("description", raw.getDescription());
        out.put("expression_type", raw.getExpressionType());
        out.put("expression", readTree(raw.getExpression()));
        return out;
    }

    private Map<String, Object> saved(CohortDTO dto) {
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("cohort_definition_id", dto.getId());
        out.put("name", dto.getName());
        out.put("concept_set_count", dto.getExpression() == null || dto.getExpression().conceptSets == null
                ? 0 : dto.getExpression().conceptSets.length);
        out.put("checker_warnings", check(dto.getExpression()));
        return out;
    }

    // ---- STEP 4 -------------------------------------------------------------------------

    private Map<String, Object> validate(ToolArguments args) {
        String sourceKey = vocabularyTools.requireSource(args);
        Source source = sourceService.findBySourceKey(sourceKey);
        Integer cohortId = args.has("cohort_definition_id") ? args.intValue("cohort_definition_id") : null;
        CohortExpression expression;
        if (args.has("expression")) {
            expression = parseExpression(args.raw("expression"));
        } else if (cohortId != null) {
            expression = parseExpression(cohortService.getCohortDefinitionRaw(cohortId).getExpression());
        } else {
            throw new ToolContractException("Pass 'expression' or 'cohort_definition_id'");
        }

        List<Map<String, Object>> warnings = check(expression);
        List<String> conceptIssues = conceptIssues(sourceKey, expression);
        boolean hasCheckerError = warnings.stream().anyMatch(w -> "CRITICAL".equals(w.get("severity")));

        String dialect = args.has("target_dialect") ? args.string("target_dialect") : source.getSourceDialect();
        Map<String, Object> sql = new LinkedHashMap<>();
        sql.put("dialect", dialect);
        String translated = null;
        try {
            BuildExpressionQueryOptions options = new BuildExpressionQueryOptions();
            options.cohortId = cohortId != null ? cohortId : 0;
            options.cdmSchema = source.getTableQualifierOrNull(DaimonType.CDM);
            options.vocabularySchema = source.getTableQualifierOrNull(DaimonType.Vocabulary);
            options.resultSchema = source.getTableQualifierOrNull(DaimonType.Results);
            options.targetTable = options.resultSchema != null ? options.resultSchema + ".cohort" : null;
            options.generateStats = true;
            CohortDefinitionService.GenerateSqlRequest request = new CohortDefinitionService.GenerateSqlRequest();
            request.expression = expression;
            request.options = options;
            String templateSql = cohortService.generateSql(request).templateSql;
            translated = SqlTranslate.translateSql(templateSql, dialect);
            sql.put("translated", true);
            sql.put("length", translated.length());
        } catch (RuntimeException e) {
            sql.put("translated", false);
            sql.put("error", e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
        if (translated != null && args.optBool("include_sql", false)) {
            sql.put("sql", translated);
        }

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("valid", !hasCheckerError && conceptIssues.isEmpty() && Boolean.TRUE.equals(sql.get("translated")));
        out.put("checker_warnings", warnings);
        out.put("concept_issues", conceptIssues);
        out.put("sql", sql);
        return out;
    }

    private List<Map<String, Object>> check(CohortExpression expression) {
        List<Map<String, Object>> out = new ArrayList<>();
        if (expression == null) {
            return out;
        }
        for (Warning w : new Checker().check(expression)) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("severity", w instanceof BaseWarning bw && bw.getSeverity() != null ? bw.getSeverity().name() : null);
            m.put("message", w.toMessage());
            out.add(m);
        }
        return out;
    }

    // ---- STEP 5 -------------------------------------------------------------------------

    private Map<String, Object> generate(ToolArguments args) {
        int id = args.intValue("cohort_definition_id");
        String sourceKey = vocabularyTools.requireSource(args);
        JobExecutionResource job = cohortService.generateCohort(id, sourceKey, false);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("cohort_definition_id", id);
        out.put("source_key", sourceKey);
        out.put("job_execution_id", job.getExecutionId());
        out.put("status", job.getStatus());
        return out;
    }

    private Map<String, Object> status(ToolArguments args) {
        int id = args.intValue("cohort_definition_id");
        String sourceKey = vocabularyTools.requireSource(args);
        Source source = sourceService.findBySourceKey(sourceKey);
        CohortGenerationInfoDTO info = cohortService.getInfo(id).stream()
                .filter(i -> i.getId() != null && source.getSourceId() == i.getId().getSourceId())
                .findFirst()
                .orElse(null);

        Map<String, Object> out = new LinkedHashMap<>();
        out.put("cohort_definition_id", id);
        out.put("source_key", sourceKey);
        if (info == null) {
            out.put("status", "NOT_GENERATED");
            return out;
        }
        out.put("status", info.getStatus());
        out.put("is_valid", info.getIsValid());
        out.put("is_canceled", info.getIsCanceled());
        out.put("start_time", info.getStartTime());
        out.put("execution_duration_ms", info.getExecutionDuration());
        out.put("person_count", info.getPersonCount());
        out.put("record_count", info.getRecordCount());
        out.put("fail_message", info.getFailMessage());
        if (info.getIsValid() && "COMPLETE".equals(String.valueOf(info.getStatus()))) {
            InclusionRuleReport report = cohortService.getInclusionRuleReport(id, sourceKey, 0);
            Map<String, Object> attrition = new LinkedHashMap<>();
            if (report.summary != null) {
                attrition.put("base_count", report.summary.baseCount);
                attrition.put("final_count", report.summary.finalCount);
                attrition.put("lost_count", report.summary.lostCount);
                attrition.put("percent_matched", report.summary.percentMatched);
            }
            List<Map<String, Object>> rules = new ArrayList<>();
            if (report.inclusionRuleStats != null) {
                for (InclusionRuleReport.InclusionRuleStatistic s : report.inclusionRuleStats) {
                    Map<String, Object> r = new LinkedHashMap<>();
                    r.put("rule_index", s.id);
                    r.put("name", s.name);
                    r.put("count_satisfying", s.countSatisfying);
                    r.put("percent_satisfying", s.percentSatisfying);
                    r.put("percent_excluded", s.percentExcluded);
                    rules.add(r);
                }
            }
            attrition.put("inclusion_rules", rules);
            out.put("attrition_all_events", attrition);
        }
        return out;
    }

    // ---- helpers ------------------------------------------------------------------------

    CohortExpression parseExpression(Object raw) {
        if (raw == null) {
            throw new ToolContractException("'expression' is required");
        }
        String json;
        try {
            json = raw instanceof String s ? s : mapper.writeValueAsString(raw);
        } catch (JsonProcessingException e) {
            throw new ToolContractException("'expression' is not valid JSON: " + e.getOriginalMessage());
        }
        try {
            CohortExpression expression = CohortExpression.fromJson(json);
            if (expression == null) {
                throw new ToolContractException("'expression' is empty");
            }
            return expression;
        } catch (RuntimeException e) {
            if (e instanceof ToolContractException) {
                throw e;
            }
            throw new ToolContractException("'expression' is not a valid Circe cohort expression: " + e.getMessage());
        }
    }

    private void requireVerifiedConcepts(String sourceKey, CohortExpression expression) {
        List<String> issues = conceptIssues(sourceKey, expression);
        if (!issues.isEmpty()) {
            throw new ToolContractException("The expression uses concepts that could not be verified against source '"
                    + sourceKey + "'; nothing was saved. " + String.join(" ", issues));
        }
    }

    /** Every concept the expression's concept sets name, checked for existence and name against the vocabulary. */
    List<String> conceptIssues(String sourceKey, CohortExpression expression) {
        List<String> issues = new ArrayList<>();
        Map<Long, String> claimed = new HashMap<>();
        Set<Long> ids = new LinkedHashSet<>();
        if (expression.conceptSets != null) {
            for (ConceptSet cs : expression.conceptSets) {
                if (cs.expression == null || cs.expression.items == null) {
                    continue;
                }
                for (ConceptSetExpression.ConceptSetItem item : cs.expression.items) {
                    if (item.concept == null || item.concept.conceptId == null) {
                        issues.add("Concept set " + cs.id + " ('" + cs.name + "') has an item without a conceptId.");
                        continue;
                    }
                    ids.add(item.concept.conceptId);
                    claimed.putIfAbsent(item.concept.conceptId, item.concept.conceptName);
                }
            }
        }
        if (ids.isEmpty()) {
            return issues;
        }
        Collection<Concept> found = vocabularyService.executeIdentifierLookup(sourceKey,
                ids.stream().mapToLong(Long::longValue).toArray());
        Map<Long, Concept> byId = new HashMap<>();
        for (Concept c : found) {
            byId.put(c.conceptId, c);
        }
        for (Long id : ids) {
            Concept actual = byId.get(id);
            String name = claimed.get(id);
            if (actual == null) {
                issues.add("Concept " + id + " does not exist in the vocabulary.");
            } else if (name == null || name.isBlank()) {
                issues.add("Concept " + id + " has no CONCEPT_NAME; it is '" + actual.conceptName + "'.");
            } else if (!ConceptViews.sameName(name, actual.conceptName)) {
                issues.add("Concept " + id + " is '" + actual.conceptName + "', not '" + name + "'.");
            }
        }
        return issues;
    }

    private Object readTree(String json) {
        if (json == null) {
            return null;
        }
        try {
            return mapper.readTree(json);
        } catch (JsonProcessingException e) {
            return json;
        }
    }
}
