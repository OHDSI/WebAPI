package org.ohdsi.webapi.cdmresults;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.ohdsi.webapi.achilles.service.AchillesCacheService;
import org.ohdsi.webapi.report.CDMResultsAnalysisRunner;
import org.ohdsi.webapi.service.CDMResultsService;
import org.ohdsi.webapi.source.Source;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class AchillesCacheTasklet implements Tasklet {
    private static final Logger LOG = LoggerFactory.getLogger(AchillesCacheTasklet.class);

    public static final String DASHBOARD = "dashboard";
    public static final String PERSON = "person";
    public static final String DATA_DENSITY = "datadensity";
    public static final String DEATH = "death";
    public static final String OBSERVATION_PERIOD = "observationperiod";
    public static final String TREEMAP = "treemap";
    public static final String DRILLDOWN = "drilldown";

    public static final Map<String, BiFunction<CDMResultsService, String, Object>> simpleDomains = new HashMap<>();

    private final static String DEFAULT_CONCEPT_ID_NAME = "conceptId";
    // domain with list of scripts for getting domain data and name of the concept_id column for each report
    // concept_id column name must be in camel case
    private static final Map<String, List<Pair<String, String>>> treemapDomains = new HashMap<>();

    private final Source source;
    private final CDMResultsService service;
    private final AchillesCacheService cacheService;
    private final CDMResultsAnalysisRunner analysisRunner;
    private final ObjectMapper objectMapper;

    static {
        // domains with concrete call endpoints
        simpleDomains.put(DASHBOARD, CDMResultsService::getRawDashboard);
        simpleDomains.put(PERSON, CDMResultsService::getRawPerson);
        simpleDomains.put(DATA_DENSITY, CDMResultsService::getRawDataDesity);
        simpleDomains.put(DEATH, CDMResultsService::getRawDeath);
        simpleDomains.put(OBSERVATION_PERIOD, CDMResultsService::getRawObservationPeriod);

        // domains with common call endpoint
        // each entry contains domain and mapping of script and concept id column name in script
        treemapDomains.put("condition", new ArrayList<Pair<String, String>>() {{
            addAll(getDefaultColumnNames("ageAtFirstOccurrence",
                    "prevalenceByGenderAgeYear", "prevalenceByMonth"));
            addAll(getColumnNames("conditionConceptId", "byType"));
        }});
        treemapDomains.put("conditionera", new ArrayList<Pair<String, String>>() {{
            addAll(getDefaultColumnNames("ageAtFirstOccurrence", "lengthOfEra",
                    "prevalenceByGenderAgeYear", "prevalenceByMonth"));
        }});
        treemapDomains.put("drug", new ArrayList<Pair<String, String>>() {{
            addAll(getDefaultColumnNames("ageAtFirstOccurrence", "frequencyDistribution",
                    "prevalenceByGenderAgeYear", "prevalenceByMonth"));
            addAll(getColumnNames("drugConceptId", "byType", "daysSupplyDistribution",
                    "quantityDistribution", "refillsDistribution"));
        }});
        treemapDomains.put("drugera", new ArrayList<Pair<String, String>>() {{
            addAll(getDefaultColumnNames("ageAtFirstOccurrence", "lengthOfEra",
                    "prevalenceByGenderAgeYear", "prevalenceByMonth"));
        }});
        treemapDomains.put("measurement", new ArrayList<Pair<String, String>>() {{
            addAll(getDefaultColumnNames("ageAtFirstOccurrence", "frequencyDistribution",
                    "lowerLimitDistribution", "measurementValueDistribution", "prevalenceByGenderAgeYear",
                    "prevalenceByMonth", "upperLimitDistribution"));
            addAll(getColumnNames("observationConceptId", "byOperator", "byValueAsConcept"));
            addAll(getColumnNames("measurementConceptId", "byType", "recordsByUnit",
                    "valuesRelativeToNorm"));
        }});
        treemapDomains.put("observation", new ArrayList<Pair<String, String>>() {{
            addAll(getDefaultColumnNames("ageAtFirstOccurrence", "frequencyDistribution",
                    "prevalenceByGenderAgeYear", "prevalenceByMonth"));
            addAll(getColumnNames("observationConceptId", "byQualifier", "byType",
                    "byValueAsConcept"));
        }});
        treemapDomains.put("procedure", new ArrayList<Pair<String, String>>() {{
            addAll(getDefaultColumnNames("ageAtFirstOccurrence", "frequencyDistribution",
                    "prevalenceByGenderAgeYear", "prevalenceByMonth"));
            addAll(getColumnNames("procedureConceptId", "byType"));
        }});
        treemapDomains.put("visit", new ArrayList<Pair<String, String>>() {{
            addAll(getDefaultColumnNames("ageAtFirstOccurrence", "prevalenceByGenderAgeYear",
                    "prevalenceByMonth", "visitDurationByType"));
        }});
    }

    public AchillesCacheTasklet(Source source,
                                CDMResultsService service,
                                AchillesCacheService cacheService,
                                CDMResultsAnalysisRunner analysisRunner,
                                ObjectMapper objectMapper) {
        this.source = source;
        this.service = service;
        this.cacheService = cacheService;
        this.analysisRunner = analysisRunner;
        this.objectMapper = objectMapper;
    }
    
    // prepare list of reports where concept id column name is equal to default one
    private static List<Pair<String, String>> getDefaultColumnNames(String... reports) {
        return getColumnNames(DEFAULT_CONCEPT_ID_NAME, reports);
    }

    // prepare list of reports where concept id column name is custom
    private static List<Pair<String, String>> getColumnNames(String columnName, String... reports) {
        return Arrays.stream(reports)
                .map(report -> new ImmutablePair<>(report, columnName))
                .collect(Collectors.toList());
    }

    private void cacheDrilldown(String domain) {
        JdbcTemplate jdbcTemplate = service.getSourceJdbcTemplate(source);

        // get drilldown reports for all concepts 
        JsonNode reports = analysisRunner.getDrilldown(jdbcTemplate, domain, null, source);

        // get reports for each concept 
        Map<Integer, ObjectNode> conceptNodes = splitReportByConcepts(domain, reports);
        cacheService.saveDrilldownCacheMap(source, domain, conceptNodes);
    }

    private List<Integer> getConceptIds(String domain) {
        ArrayNode treeMap = service.getTreemap(domain, source.getSourceKey());
        Stream<JsonNode> nodes = IntStream.range(0, treeMap.size()).mapToObj(treeMap::get);
        return nodes.map(node -> node.get("conceptId").intValue())
                .distinct()
                .collect(Collectors.toList());
    }

    private Map<Integer, ObjectNode> splitReportByConcepts(String domain, JsonNode reports) {
        List<Pair<String, String>> drilldownScripts = treemapDomains.get(domain);

        if (reports.size() != drilldownScripts.size()) {
            throw new RuntimeException("Drilldown reports size must be equal to number of scripts. Check achilles caching!");
        }

        Map<Integer, ObjectNode> conceptNodes = new HashMap<>();
        drilldownScripts.forEach(columnName -> copyItemToConceptReport(domain, reports, conceptNodes, columnName));
        return conceptNodes;
    }

    private void copyItemToConceptReport(String domain, JsonNode reports,
                                         Map<Integer, ObjectNode> conceptNodes, Pair<String, String> columnName) {
        String reportName = columnName.getKey();
        String conceptColumnName = columnName.getValue();

        JsonNode report = reports.get(reportName);
        List<Integer> conceptIds = getConceptIds(domain);

        Objects.requireNonNull(report).forEach(item -> {
            int conceptId = item.get(conceptColumnName).intValue();

            // ignore data for concept which is absent in treemap
            if (conceptIds.contains(conceptId)) {
                ArrayNode reportNode = getReport(conceptNodes, reportName, conceptId);
                reportNode.add(item);
            }
        });
    }

    private ArrayNode getReport(Map<Integer, ObjectNode> conceptNodes, String reportName, int conceptId) {
        // get node for given concept or create new one
        ObjectNode conceptReport = conceptNodes.computeIfAbsent(conceptId, x -> objectMapper.createObjectNode());
        // get concept report with given name or create new one
        ArrayNode reportNode = (ArrayNode) conceptReport.get(reportName);
        if (Objects.isNull(reportNode)) {
            reportNode = conceptReport.putArray(reportName);
        }
        return reportNode;
    }

    private void cacheDomain(String domain, BiFunction<CDMResultsService, String, Object> function) {
        Object result = function.apply(service, source.getSourceKey());
        cache(domain, result);
    }

    private void cacheTreemap(String domain) {
        Object result = service.getRawTreeMap(domain, source.getSourceKey());
        cache(TREEMAP + "_" + domain, result);
    }

    private void cache(String cacheName, Object result) {
        try {
            cacheService.createCache(source, cacheName, result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        simpleDomains.forEach(this::cacheDomain);
        treemapDomains.keySet().forEach(this::cacheTreemap);
        treemapDomains.keySet().forEach(this::cacheDrilldown);

        return RepeatStatus.FINISHED;
    }
}
