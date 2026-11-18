package org.ohdsi.webapi.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.junit.Before;
import org.junit.Test;
import org.ohdsi.webapi.source.SourceRepository;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

/**
 * Drives the MCP endpoint over HTTP the way an agent would: JSON-RPC tools/list and tools/call on
 * POST /mcp, authenticated with a WebAPI JWT, against the embedded CDM.
 */
@TestPropertySource(properties = "spring.ai.mcp.server.enabled=true")
public class McpServerIT extends WebApiIT {

    private static final long T2DM = 201826L;
    private static final long NON_STANDARD = 44054006L;
    private static final long GI_BLEED = 192671L;
    /** People in Eunomia with a gastrointestinal hemorrhage inside an observation period, all ages / aged 40+. */
    private static final long EUNOMIA_GI_BLEED_PEOPLE = 479;
    private static final long EUNOMIA_GI_BLEED_PEOPLE_40_PLUS = 204;

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private SourceRepository sourceRepository;

    @Autowired
    private ObjectMapper appObjectMapper;

    private int rpcId;

    @Before
    public void init() throws Exception {
        truncateTable(String.format("%s.%s", getOhdsiSchema(), "source"));
        resetSequence(String.format("%s.%s", getOhdsiSchema(), "source_sequence"));
        sourceRepository.saveAndFlush(getCdmSource());
        prepareCdmSchema();
        prepareResultSchema();
        jdbcTemplate.execute("insert into " + CDM_SCHEMA_NAME + ".concept (concept_id, concept_name, domain_id, "
                + "vocabulary_id, concept_class_id, standard_concept, concept_code, valid_start_date, valid_end_date, "
                + "invalid_reason) values "
                + "(" + T2DM + ", 'Type 2 diabetes mellitus', 'Condition', 'SNOMED', 'Clinical Finding', 'S', "
                + "'44054006', '1970-01-01', '2099-12-31', null), "
                + "(" + NON_STANDARD + ", 'Diabetes mellitus type 2', 'Condition', 'ICD10CM', '3-char nonbill code', "
                + "null, 'E11', '1970-01-01', '2099-12-31', null)");
    }

    @Test
    public void listsToolsAndRequiresRationaleOnConceptSetChanges() throws Exception {
        JsonNode result = rpc("tools/list", mapper.createObjectNode()).path("result");
        List<String> names = new ArrayList<>();
        result.path("tools").forEach(t -> names.add(t.path("name").asText()));
        assertTrue(names.toString(), names.containsAll(List.of("list_sources", "search_concepts", "get_concept_details",
                "get_concept_hierarchy", "create_concept_set", "add_concept_to_set", "validate_cohort_definition",
                "generate_cohort", "get_cohort_generation_status", "get_audit_trail")));

        JsonNode add = null;
        for (JsonNode t : result.path("tools")) {
            if ("add_concept_to_set".equals(t.path("name").asText())) {
                add = t;
            }
        }
        List<String> required = new ArrayList<>();
        add.path("inputSchema").path("required").forEach(r -> required.add(r.asText()));
        assertTrue(required.toString(), required.containsAll(
                List.of("concept_id", "concept_name", "include_descendants", "clinical_rationale", "evidence_reference")));
    }

    @Test
    public void searchContinuesTheCallersTraceAndIsAudited() throws Exception {
        String traceId = newTraceId();
        String clientSpan = "00f067aa0ba902b7";
        JsonNode result = callTool("search_concepts",
                Map.of("source_key", SOURCE_KEY, "query", "diabetes", "standard_concept", "S"),
                traceparent(traceId, clientSpan), "STEP_1_CONCEPT_EXPLORATION");

        assertFalse(result.toString(), result.path("isError").asBoolean());
        JsonNode body = mapper.readTree(result.path("content").get(0).path("text").asText());
        assertEquals(1, body.path("total_matches").asInt());
        assertEquals(T2DM, body.path("concepts").get(0).path("concept_id").asLong());

        String returned = result.path("_meta").path("traceparent").asText();
        assertTrue(returned, returned.startsWith("00-" + traceId + "-"));
        assertNotEquals("the tool call gets its own span", clientSpan, returned.split("-")[2]);

        Map<String, Object> row = jdbcTemplate.queryForMap("select * from " + getOhdsiSchema()
                + ".mcp_audit_log where trace_id = ?", traceId);
        assertEquals("SUCCESS", row.get("outcome"));
        assertEquals(clientSpan, row.get("parent_span_id"));
        assertEquals(returned.split("-")[2], row.get("span_id"));
        assertEquals("STEP_1_CONCEPT_EXPLORATION", row.get("reasoning_step"));
        assertEquals("search_concepts", row.get("tool_name"));
    }

    @Test
    public void addConceptRefusesAConceptWhoseNameDoesNotMatchTheVocabulary() throws Exception {
        String traceId = newTraceId();
        int conceptSetId = createConceptSet(traceId);

        JsonNode result = callTool("add_concept_to_set", Map.of(
                "concept_set_id", conceptSetId,
                "source_key", SOURCE_KEY,
                "concept_id", T2DM,
                "concept_name", "Type 1 diabetes mellitus",
                "include_descendants", true,
                "clinical_rationale", "Core diagnosis concept for the target diabetes phenotype.",
                "evidence_reference", "SNOMED-CT clinical finding hierarchy"),
                traceparent(traceId, "1111111111111111"), null);

        assertTrue(result.isEmpty() ? "no result" : result.toString(), result.path("isError").asBoolean());
        assertTrue(result.toString(), text(result).contains("Type 2 diabetes mellitus"));
        assertEquals(0, itemCount(conceptSetId));
        assertEquals("REJECTED", lastOutcome(traceId, "add_concept_to_set"));

        // The refused attempt is part of the concept set's provenance.
        JsonNode provenance = provenance("conceptset", conceptSetId);
        assertEquals(provenance.toString(), 2, provenance.size());
        assertEquals("add_concept_to_set", provenance.get(1).path("tool").asText());
        assertEquals("REJECTED", provenance.get(1).path("outcome").asText());
    }

    @Test
    public void addConceptRefusesACallWithoutClinicalRationale() throws Exception {
        String traceId = newTraceId();
        int conceptSetId = createConceptSet(traceId);

        JsonNode result = callTool("add_concept_to_set", Map.of(
                "concept_set_id", conceptSetId,
                "source_key", SOURCE_KEY,
                "concept_id", T2DM,
                "concept_name", "Type 2 diabetes mellitus",
                "include_descendants", true),
                traceparent(traceId, "2222222222222222"), null);

        assertTrue(result.path("isError").asBoolean());
        assertTrue(text(result), text(result).contains("clinical_rationale"));
        assertEquals(0, itemCount(conceptSetId));
        assertEquals("REJECTED", lastOutcome(traceId, "add_concept_to_set"));
    }

    @Test
    public void addConceptSavesAVerifiedConceptAndTheTrailShowsTheRationale() throws Exception {
        String traceId = newTraceId();
        int conceptSetId = createConceptSet(traceId);
        String rationale = "Core primary diagnosis concept for adult-onset T2DM; descendants capture complications.";

        JsonNode result = callTool("add_concept_to_set", Map.of(
                "concept_set_id", conceptSetId,
                "source_key", SOURCE_KEY,
                "concept_id", T2DM,
                "concept_name", "type 2 diabetes  mellitus",
                "include_descendants", true,
                "clinical_rationale", rationale,
                "evidence_reference", "ADA Standards of Care; SNOMED-CT clinical finding hierarchy"),
                traceparent(traceId, "3333333333333333"), "STEP_2_CONCEPT_SELECTION");

        assertFalse(result.toString(), result.path("isError").asBoolean());
        assertEquals(1, itemCount(conceptSetId));

        JsonNode trail = mapper.readTree(text(callTool("get_audit_trail", Map.of("trace_id", traceId),
                traceparent(traceId, "4444444444444444"), "AUDIT")));
        JsonNode log = trail.path("decision_log");
        assertEquals("create_concept_set", log.get(0).path("tool").asText());
        assertEquals("add_concept_to_set", log.get(1).path("tool").asText());
        assertEquals(rationale, log.get(1).path("clinical_rationale").asText());
        assertEquals("STEP_2_CONCEPT_SELECTION", log.get(1).path("reasoning_step").asText());
        assertEquals("SUCCESS", log.get(1).path("outcome").asText());

        // Atlas reads the same history from the concept set's provenance endpoint.
        JsonNode provenance = provenance("conceptset", conceptSetId);
        assertEquals(provenance.toString(), 2, provenance.size());
        assertEquals("create_concept_set", provenance.get(0).path("tool").asText());
        assertEquals("add_concept_to_set", provenance.get(1).path("tool").asText());
        assertEquals(rationale, provenance.get(1).path("clinicalRationale").asText());
        assertEquals(traceId, provenance.get(1).path("traceId").asText());
        assertEquals("SUCCESS", provenance.get(1).path("outcome").asText());
        assertEquals("anonymous", provenance.get(1).path("userLogin").asText());
        assertTrue(provenance.get(1).path("startedAt").isNumber());
        assertFalse("payloads are not exposed", provenance.get(1).has("argumentsJson"));
        assertEquals(0, provenance("conceptset", conceptSetId + 1000).size());
    }

    @Test
    public void auditRowsCannotBeChangedOrDeleted() throws Exception {
        String traceId = newTraceId();
        callTool("list_sources", Map.of(), traceparent(traceId, "5555555555555555"), null);
        String table = getOhdsiSchema() + ".mcp_audit_log";

        try {
            jdbcTemplate.update("update " + table + " set clinical_rationale = 'rewritten' where trace_id = ?", traceId);
            fail("a completed audit row must not be updatable");
        } catch (Exception expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("append-only"));
        }
        try {
            jdbcTemplate.update("delete from " + table + " where trace_id = ?", traceId);
            fail("audit rows must not be deletable");
        } catch (Exception expected) {
            assertTrue(expected.getMessage(), expected.getMessage().contains("append-only"));
        }
    }

    @Test
    public void validationTranslatesSqlAndFlagsUnverifiedConcepts() throws Exception {
        String traceId = newTraceId();
        JsonNode ok = mapper.readTree(text(callTool("validate_cohort_definition", Map.of(
                "source_key", SOURCE_KEY,
                "expression", cohortExpression("Type 2 diabetes mellitus"),
                "target_dialect", "sql server",
                "include_sql", true),
                traceparent(traceId, "6666666666666666"), null)));
        assertTrue(ok.toString(), ok.path("valid").asBoolean());
        assertTrue(ok.path("sql").path("translated").asBoolean());
        assertTrue(ok.path("sql").path("sql").asText().contains("#Codesets"));

        JsonNode bad = mapper.readTree(text(callTool("validate_cohort_definition", Map.of(
                "source_key", SOURCE_KEY,
                "expression", cohortExpression("Gestational diabetes")),
                traceparent(traceId, "7777777777777777"), null)));
        assertFalse(bad.path("valid").asBoolean());
        assertEquals(1, bad.path("concept_issues").size());
    }

    @Test
    public void createCohortDefinitionRefusesUnverifiedConcepts() throws Exception {
        String traceId = newTraceId();
        JsonNode result = callTool("create_cohort_definition", Map.of(
                "name", "MCP IT cohort " + UUID.randomUUID(),
                "source_key", SOURCE_KEY,
                "expression", cohortExpression("Gestational diabetes"),
                "clinical_rationale", "Adults with a first recorded T2DM diagnosis as index event.",
                "evidence_reference", "ADA Standards of Care"),
                traceparent(traceId, "8888888888888888"), null);
        assertTrue(result.path("isError").asBoolean());
        assertTrue(text(result), text(result).contains("nothing was saved"));
        assertEquals("REJECTED", lastOutcome(traceId, "create_cohort_definition"));
    }

    @Test
    public void generatesACohortOnEunomiaAndReportsCountsAndAttrition() throws Exception {
        loadEunomia("person", "observation_period", "condition_occurrence", "concept", "concept_ancestor");

        // First gastrointestinal hemorrhage in an observation period; the inclusion rule keeps people aged 40+.
        String traceId = newTraceId();
        Map<String, Object> expression = cohortExpression(GI_BLEED, "Gastrointestinal hemorrhage");
        expression.put("InclusionRules", List.of(Map.of("name", "Age 40+", "expression", Map.of(
                "Type", "ALL", "CriteriaList", List.of(), "Groups", List.of(),
                "DemographicCriteriaList", List.of(Map.of("Age", Map.of("Value", 40, "Op", "gte")))))));
        JsonNode created = callTool("create_cohort_definition", Map.of(
                "name", "MCP IT GI bleed cohort " + UUID.randomUUID(),
                "source_key", SOURCE_KEY,
                "expression", expression,
                "clinical_rationale", "Adults aged 40+ at their first recorded gastrointestinal hemorrhage.",
                "evidence_reference", "OHDSI Eunomia GiBleed example study"),
                traceparent(traceId, "9999999999999999"), "STEP_3_COHORT_CRITERIA");
        assertFalse(created.toString(), created.path("isError").asBoolean());
        int cohortId = mapper.readTree(text(created)).path("cohort_definition_id").asInt();

        JsonNode queued = callTool("generate_cohort", Map.of("cohort_definition_id", cohortId, "source_key", SOURCE_KEY,
                "clinical_rationale", "Generate to check the cohort size is plausible.",
                "evidence_reference", "Phenotype review plan"),
                traceparent(traceId, "bbbbbbbbbbbbbbbb"), "STEP_5_GENERATION_VERIFICATION");
        assertFalse(queued.toString(), queued.path("isError").asBoolean());

        JsonNode status = null;
        long deadline = System.currentTimeMillis() + 120_000;
        while (System.currentTimeMillis() < deadline) {
            status = mapper.readTree(text(callTool("get_cohort_generation_status",
                    Map.of("cohort_definition_id", cohortId, "source_key", SOURCE_KEY),
                    traceparent(traceId, "cccccccccccccccc"), "STEP_5_GENERATION_VERIFICATION")));
            if ("COMPLETE".equals(status.path("status").asText())) {
                break;
            }
            Thread.sleep(500);
        }
        assertEquals(status.toString(), "COMPLETE", status.path("status").asText());
        assertTrue(status.toString(), status.path("is_valid").asBoolean());

        // The same cohort counted independently with plain SQL on the CDM.
        String firstEvents = "select co.person_id, min(co.condition_start_date) as index_date from "
                + CDM_SCHEMA_NAME + ".condition_occurrence co join " + CDM_SCHEMA_NAME + ".observation_period op "
                + "on op.person_id = co.person_id and co.condition_start_date between "
                + "op.observation_period_start_date and op.observation_period_end_date "
                + "where co.condition_concept_id = " + GI_BLEED + " group by co.person_id";
        long base = jdbcTemplate.queryForObject("select count(*) from (" + firstEvents + ") e", Long.class);
        long aged40 = jdbcTemplate.queryForObject("select count(*) from (" + firstEvents + ") e join "
                + CDM_SCHEMA_NAME + ".person p on p.person_id = e.person_id "
                + "where extract(year from e.index_date) - p.year_of_birth >= 40", Long.class);
        assertEquals(EUNOMIA_GI_BLEED_PEOPLE, base);
        assertEquals(EUNOMIA_GI_BLEED_PEOPLE_40_PLUS, aged40);

        assertEquals(status.toString(), aged40, status.path("person_count").asLong());
        assertEquals(status.toString(), aged40, status.path("record_count").asLong());
        JsonNode attrition = status.path("attrition_all_events");
        assertEquals(status.toString(), base, attrition.path("base_count").asLong());
        assertEquals(status.toString(), aged40, attrition.path("final_count").asLong());
        JsonNode rule = attrition.path("inclusion_rules").get(0);
        assertEquals("Age 40+", rule.path("name").asText());
        assertEquals(aged40, rule.path("count_satisfying").asLong());

        long written = jdbcTemplate.queryForObject("select count(distinct subject_id) from " + RESULT_SCHEMA_NAME
                + ".cohort where cohort_definition_id = ?", Long.class, cohortId);
        assertEquals(aged40, written);
        assertEquals("SUCCESS", lastOutcome(traceId, "generate_cohort"));

        // Status polls are reads and stay out of the cohort's provenance.
        JsonNode provenance = provenance("cohortdefinition", cohortId);
        List<String> tools = new ArrayList<>();
        provenance.forEach(e -> tools.add(e.path("tool").asText()));
        assertEquals(List.of("create_cohort_definition", "generate_cohort"), tools);
        assertEquals("STEP_3_COHORT_CRITERIA", provenance.get(0).path("reasoningStep").asText());
    }

    @Test
    public void callWithoutTraceparentStartsANewTrace() throws Exception {
        JsonNode result = callTool("list_sources", Map.of(), null, null);
        assertFalse(result.path("isError").asBoolean());
        String traceId = result.path("_meta").path("traceparent").asText().split("-")[1];
        Boolean generated = jdbcTemplate.queryForObject("select trace_generated from " + getOhdsiSchema()
                + ".mcp_audit_log where trace_id = ?", Boolean.class, traceId);
        assertTrue(generated);
    }

    @Test
    public void enablingMcpDoesNotReplaceTheApplicationObjectMapper() {
        // Spring AI registers its own non-default-candidate "mcpServerObjectMapper" (dates as ISO strings);
        // the REST API must keep the Boot mapper configured by spring.jackson (dates as timestamps).
        assertTrue(appObjectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS));
    }

    @Test
    public void endpointRequiresTheMcpAcceptHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        ResponseEntity<String> response = getRestTemplate().exchange(getBaseUri() + "/mcp", HttpMethod.POST,
                new HttpEntity<>("{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}", headers), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ---- helpers ------------------------------------------------------------------------

    private int createConceptSet(String traceId) throws Exception {
        JsonNode result = callTool("create_concept_set", Map.of(
                "name", "MCP IT concept set " + UUID.randomUUID(),
                "clinical_rationale", "Concept set for the adult-onset type 2 diabetes phenotype.",
                "evidence_reference", "ADA Standards of Care"),
                traceparent(traceId, "aaaaaaaaaaaaaaaa"), null);
        assertFalse(result.toString(), result.path("isError").asBoolean());
        return mapper.readTree(text(result)).path("concept_set_id").asInt();
    }

    private Map<String, Object> cohortExpression(String conceptName) throws Exception {
        return cohortExpression(T2DM, conceptName);
    }

    private Map<String, Object> cohortExpression(long conceptId, String conceptName) throws Exception {
        String json = "{\"ConceptSets\":[{\"id\":0,\"name\":\"Codes\",\"expression\":{\"items\":[{\"concept\":"
                + "{\"CONCEPT_ID\":" + conceptId + ",\"CONCEPT_NAME\":\"" + conceptName + "\"},\"includeDescendants\":true}]}}],"
                + "\"PrimaryCriteria\":{\"CriteriaList\":[{\"ConditionOccurrence\":{\"CodesetId\":0}}],"
                + "\"ObservationWindow\":{\"PriorDays\":0,\"PostDays\":0},\"PrimaryCriteriaLimit\":{\"Type\":\"First\"}},"
                + "\"QualifiedLimit\":{\"Type\":\"First\"},\"ExpressionLimit\":{\"Type\":\"First\"},"
                + "\"InclusionRules\":[],\"CensoringCriteria\":[],\"CollapseSettings\":{\"CollapseType\":\"ERA\",\"EraPad\":0}}";
        @SuppressWarnings("unchecked")
        Map<String, Object> m = mapper.readValue(json, Map.class);
        return m;
    }

    /** Streams the given tables of the bundled Eunomia CDM (src/test/resources/eunomia.csv.zip) into the cdm schema. */
    private static void loadEunomia(String... tables) throws Exception {
        Set<String> wanted = new HashSet<>(Arrays.asList(tables));
        try (Connection connection = ITStarter.getDataSource().getConnection();
             ZipInputStream zip = new ZipInputStream(McpServerIT.class.getResourceAsStream("/eunomia.csv.zip"))) {
            CopyManager copy = connection.unwrap(PGConnection.class).getCopyAPI();
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                String table = entry.getName().replace(".csv", "");
                if (!wanted.remove(table)) {
                    continue;
                }
                BufferedReader csv = new BufferedReader(new InputStreamReader(zip, StandardCharsets.UTF_8));
                String columns = csv.readLine();
                copy.copyIn("copy " + CDM_SCHEMA_NAME + "." + table + " (" + columns + ") from stdin with (format csv)",
                        csv);
            }
        }
        assertTrue("missing from eunomia.csv.zip: " + wanted, wanted.isEmpty());
    }

    private JsonNode provenance(String resource, int id) throws Exception {
        ResponseEntity<String> response = getRestTemplate().getForEntity(
                getBaseUri() + "/" + resource + "/" + id + "/mcp-provenance", String.class);
        assertEquals(response.getBody(), HttpStatus.OK, response.getStatusCode());
        return mapper.readTree(response.getBody());
    }

    private int itemCount(int conceptSetId) {
        return jdbcTemplate.queryForObject("select count(*) from " + getOhdsiSchema()
                + ".concept_set_item where concept_set_id = ?", Integer.class, conceptSetId);
    }

    private String lastOutcome(String traceId, String tool) {
        return jdbcTemplate.queryForObject("select outcome from " + getOhdsiSchema()
                + ".mcp_audit_log where trace_id = ? and tool_name = ? order by id desc limit 1",
                String.class, traceId, tool);
    }

    private static String newTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private static String traceparent(String traceId, String spanId) {
        return "00-" + traceId + "-" + spanId + "-01";
    }

    private static String text(JsonNode toolResult) {
        return toolResult.path("content").get(0).path("text").asText();
    }

    private JsonNode callTool(String name, Map<String, Object> arguments, String traceparent, String step)
            throws Exception {
        ObjectNode params = mapper.createObjectNode();
        params.put("name", name);
        params.set("arguments", mapper.valueToTree(arguments));
        if (traceparent != null || step != null) {
            ObjectNode meta = params.putObject("_meta");
            if (traceparent != null) {
                meta.put("traceparent", traceparent);
            }
            if (step != null) {
                meta.put("reasoning_step", step);
            }
        }
        JsonNode response = rpc("tools/call", params);
        assertTrue(response.toString(), response.has("result"));
        return response.path("result");
    }

    private JsonNode rpc(String method, ObjectNode params) throws Exception {
        ObjectNode request = mapper.createObjectNode();
        request.put("jsonrpc", "2.0");
        request.put("id", ++rpcId);
        request.put("method", method);
        request.set("params", params);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM));
        TestRestTemplate rest = getRestTemplate();
        ResponseEntity<String> response = rest.exchange(getBaseUri() + "/mcp", HttpMethod.POST,
                new HttpEntity<>(mapper.writeValueAsString(request), headers), String.class);
        assertEquals(response.getBody(), HttpStatus.OK, response.getStatusCode());
        return mapper.readTree(response.getBody());
    }
}
