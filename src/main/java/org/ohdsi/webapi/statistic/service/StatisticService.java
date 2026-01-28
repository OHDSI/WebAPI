package org.ohdsi.webapi.statistic.service;

import com.opencsv.CSVWriter;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.ohdsi.webapi.statistic.dto.AccessTrendDto;
import org.ohdsi.webapi.statistic.dto.AccessTrendsDto;
import org.ohdsi.webapi.statistic.dto.EndpointDto;
import org.ohdsi.webapi.statistic.dto.SourceExecutionDto;
import org.ohdsi.webapi.statistic.dto.SourceExecutionsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Spring MVC service for statistics
 *
 * Endpoints: 2 POST endpoints
 * Complexity: Medium - statistics with CSV generation
 */
@RestController
@RequestMapping("/statistic")
public class StatisticService {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Value("${audit.trail.enabled}")
    private boolean auditTrailEnabled;

    @Value("${audit.trail.log.file}")
    private String absoluteLogFileName = "/tmp/atlas/audit/audit.log";

    private String logFileName;

    public enum ResponseFormat {
        CSV, JSON
    }

    private static final List<String[]> EXECUTION_STATISTICS_CSV_RESULT_HEADER = new ArrayList<String[]>() {{
        add(new String[]{"Date", "Source", "Execution Type"});
    }};

    private static final List<String[]> EXECUTION_STATISTICS_CSV_RESULT_HEADER_WITH_USER_ID = new ArrayList<String[]>() {{
        add(new String[]{"Date", "Source", "Execution Type", "User ID"});
    }};

    private static final List<String[]> ACCESS_TRENDS_CSV_RESULT_HEADER = new ArrayList<String[]>() {{
        add(new String[]{"Date", "Endpoint"});
    }};

    private static final List<String[]> ACCESS_TRENDS_CSV_RESULT_HEADER_WITH_USER_ID = new ArrayList<String[]>() {{
        add(new String[]{"Date", "Endpoint", "User ID"});
    }};

    @Value("${audit.trail.log.file.pattern}")
    private String absoluteLogFileNamePattern = "/tmp/atlas/audit/audit-%d{yyyy-MM-dd}-%i.log";

    private String logFileNamePattern;

    private SimpleDateFormat logFileDateFormat;

    private int logFileDateStart;

    private int logFileDateEnd;

    // Some execution can have duplicate logs with different parameters
    // Duplicate log entries can exist because sometimes ccontroller methods are called from other controller methods
    // These regular expressions let us to choose only needed log entries
    private static final Pattern COHORT_GENERATION_REGEXP =
            Pattern.compile("^.*(\\d{4}-\\d{2}-\\d{2})T\\d{2}:\\d{2}:\\d{2}.*-\\s-\\s-\\s([\\w-]+)\\s.*GET\\s/WebAPI/cohortdefinition/\\d+/generate/(.+)\\s-\\s.*status::String,startDate::Date,endDate::Date.*$");

    private static final Pattern CHARACTERIZATION_GENERATION_REGEXP =
            Pattern.compile("^.*(\\d{4}-\\d{2}-\\d{2})T\\d{2}:\\d{2}:\\d{2}.*-\\s-\\s-\\s([\\w-]+)\\s.*POST\\s/WebAPI/cohort-characterization/\\d+/generation/(.+)\\s-\\s.*status::String,startDate::Date,endDate::Date.*$");

    private static final Pattern PATHWAY_GENERATION_REGEXP =
            Pattern.compile("^.*(\\d{4}-\\d{2}-\\d{2})T\\d{2}:\\d{2}:\\d{2}.*-\\s-\\s-\\s([\\w-]+)\\s.*POST\\s/WebAPI/pathway-analysis/\\d+/generation/(.+)\\s-\\s.*status::String,startDate::Date,endDate::Date.*$");

    private static final Pattern IR_GENERATION_REGEXP =
            Pattern.compile("^.*(\\d{4}-\\d{2}-\\d{2})T\\d{2}:\\d{2}:\\d{2}.*-\\s-\\s-\\s([\\w-]+)\\s.*GET\\s/WebAPI/ir/\\d+/execute/(.+)\\s-\\s.*status::String,startDate::Date,endDate::Date.*$");

    private static final Pattern PLE_GENERATION_REGEXP =
            Pattern.compile("^.*(\\d{4}-\\d{2}-\\d{2})T\\d{2}:\\d{2}:\\d{2}.*-\\s-\\s-\\s([\\w-]+)\\s.*POST\\s/WebAPI/estimation/\\d+/generation/(.+)\\s-\\s.*status::String,startDate::Date,endDate::Date.*$");

    private static final Pattern PLP_GENERATION_REGEXP =
            Pattern.compile("^.*(\\d{4}-\\d{2}-\\d{2})T\\d{2}:\\d{2}:\\d{2}.*-\\s-\\s-\\s([\\w-]+)\\s.*POST\\s/WebAPI/prediction/\\d+/generation/(.+)\\s-\\s.*status::String,startDate::Date,endDate::Date.*$");

    private static final String ENDPOINT_REGEXP =
            "^.*(\\d{4}-\\d{2}-\\d{2})T(\\d{2}:\\d{2}:\\d{2}).*-\\s-\\s-\\s([\\w-]+)\\s.*-\\s({METHOD_PLACEHOLDER}\\s.*{ENDPOINT_PLACEHOLDER})\\s-.*$";

    private static final String COHORT_GENERATION_NAME = "Cohort Generation";

    private static final String CHARACTERIZATION_GENERATION_NAME = "Characterization Generation";

    private static final String PATHWAY_GENERATION_NAME = "Pathway Generation";

    private static final String IR_GENERATION_NAME = "Incidence Rates Generation";

    private static final String PLE_GENERATION_NAME = "Estimation Generation";

    private static final String PLP_GENERATION_NAME = "Prediction Generation";

    private static final Map<String, Pattern> patternMap = new HashMap<>();

    static {
        patternMap.put(COHORT_GENERATION_NAME, COHORT_GENERATION_REGEXP);
        patternMap.put(CHARACTERIZATION_GENERATION_NAME, CHARACTERIZATION_GENERATION_REGEXP);
        patternMap.put(PATHWAY_GENERATION_NAME, PATHWAY_GENERATION_REGEXP);
        patternMap.put(IR_GENERATION_NAME, IR_GENERATION_REGEXP);
        patternMap.put(PLE_GENERATION_NAME, PLE_GENERATION_REGEXP);
        patternMap.put(PLP_GENERATION_NAME, PLP_GENERATION_REGEXP);
    }

    public StatisticService() {
        
        logFileName = new File(absoluteLogFileName).getName();
        logFileNamePattern = new File(absoluteLogFileNamePattern).getName();

        // Pattern contains "%d{yyyy-MM-dd}". "%d" will not be contained in real log file name
        int placeHolderPrefixLength = 3;
        logFileDateStart = logFileNamePattern.indexOf("{") - placeHolderPrefixLength + 1;
        logFileDateEnd = logFileNamePattern.indexOf("}") - placeHolderPrefixLength;
        String dateString = logFileNamePattern.substring(logFileDateStart + placeHolderPrefixLength,
                logFileDateEnd + placeHolderPrefixLength);
        logFileDateFormat = new SimpleDateFormat(dateString);
    }

    public SourceExecutionsDto getSourceExecutions(LocalDate startDate, LocalDate endDate, String sourceKey, boolean showUserInformation) {
        Set<Path> paths = getLogPaths(startDate, endDate);
        List<SourceExecutionDto> executions = paths.stream()
                .flatMap(path -> extractSourceExecutions(path, sourceKey, showUserInformation).stream())
                .collect(Collectors.toList());
        return new SourceExecutionsDto(executions);
    }

    public AccessTrendsDto getAccessTrends(LocalDate startDate, LocalDate endDate, List<EndpointDto> endpoints, boolean showUserInformation) {
        Set<Path> paths = getLogPaths(startDate, endDate);
        List<AccessTrendDto> trends = paths.stream()
                .flatMap(path -> extractAccessTrends(path, endpoints, showUserInformation).stream())
                .collect(Collectors.toList());
        return new AccessTrendsDto(trends);
    }

    private List<SourceExecutionDto> extractSourceExecutions(Path path, String sourceKey, boolean showUserInformation) {
        try (Stream<String> stream = Files.lines(path)) {
            return stream
                    .map(str -> getMatchedExecution(str, sourceKey, showUserInformation))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error parsing log file {}. {}", path.getFileName(), e);
            throw new RuntimeException(e);
        }
    }

    private List<AccessTrendDto> extractAccessTrends(Path path, List<EndpointDto> endpoints, boolean showUserInformation) {
        List<Pattern> patterns = endpoints.stream()
                .map(endpointPair -> {
                    String method = endpointPair.getMethod();

                    String endpoint = endpointPair.getUrlPattern().replaceAll("\\{\\}", ".*");
                    String regexpStr = ENDPOINT_REGEXP.replace("{METHOD_PLACEHOLDER}", method);
                    regexpStr = regexpStr.replace("{ENDPOINT_PLACEHOLDER}", endpoint);

                    return Pattern.compile(regexpStr);
                })

                .collect(Collectors.toList());
        try (Stream<String> stream = Files.lines(path)) {
            return stream
                    .map(str -> {
                            return patterns.stream()
                                    .map(pattern -> pattern.matcher(str))
                                    .filter(matcher -> matcher.matches())
                                    .map(matcher -> new AccessTrendDto(matcher.group(4), matcher.group(1), showUserInformation ? matcher.group(3) : null))
                                    .findFirst();
                        })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            LOG.error("Error parsing log file {}. {}", path.getFileName(), e);
            throw new RuntimeException(e);
        }
    }

    private Optional<SourceExecutionDto> getMatchedExecution(String str, String sourceKey, boolean showUserInformation) {
        return patternMap.entrySet().stream()
                .map(entry -> new ImmutablePair<>(entry.getKey(), entry.getValue().matcher(str)))
                .filter(pair -> pair.getValue().matches())
                .filter(pair -> sourceKey == null || (sourceKey != null && sourceKey.equals(pair.getValue().group(3))))
                .map(pair -> new SourceExecutionDto(pair.getValue().group(3), pair.getKey(), pair.getValue().group(1), showUserInformation ? pair.getValue().group(2) : null))
                .findFirst();
    }

    private Set<Path> getLogPaths(LocalDate startDate, LocalDate endDate) {
        String folderPath = new File(absoluteLogFileName).getParentFile().getAbsolutePath();
        try (Stream<Path> stream = Files.list(Paths.get(folderPath))) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .filter(this::isValidLogFile)
                    .filter(file -> isLogInDateRange(file, startDate, endDate))
                    .map(Path::toAbsolutePath)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            LOG.error("Error getting list of log files", e);
            throw new RuntimeException(e);
        }
    }

    private boolean isValidLogFile(Path path) {
        return path.getFileName().toString().endsWith(".log");
    }

    private boolean isLogInDateRange(Path path, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return true;
        }
        LocalDate logDate = getFileDate(path.getFileName());
        if ((startDate != null && logDate.isBefore(startDate))
                || (endDate != null && logDate.isAfter(endDate))) {
            return false;
        }
        return true;
    }

    private LocalDate getFileDate(Path path) {
        String fileName = path.toString();
        if (logFileName.equals(fileName)) {
            return LocalDate.now();
        }
        try {
            String dateStr = fileName.substring(logFileDateStart, logFileDateEnd);
            return logFileDateFormat.parse(dateStr).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } catch (ParseException | IndexOutOfBoundsException e) {
            // If we cannot check the date of a file, then assume that it is a file for the current date
            return LocalDate.now();
        }
    }

    // REST Endpoints

    /**
     * Returns execution statistics
     *
     * @param executionStatisticsRequest filter settings for statistics
     * @return execution statistics in JSON or CSV format
     */
    @PostMapping(
        value = "/executions",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> executionStatistics(@RequestBody ExecutionStatisticsRequest executionStatisticsRequest) {
        if (!auditTrailEnabled) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Audit Trail functionality should be enabled (audit.trail.enabled) to serve this endpoint");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        boolean showUserInformation = executionStatisticsRequest.isShowUserInformation();

        SourceExecutionsDto sourceExecutions = getSourceExecutions(
                LocalDate.parse(executionStatisticsRequest.getStartDate(), formatter),
                LocalDate.parse(executionStatisticsRequest.getEndDate(), formatter),
                executionStatisticsRequest.getSourceKey(),
                showUserInformation);

        if (ResponseFormat.CSV.equals(executionStatisticsRequest.getResponseFormat())) {
            return prepareExecutionResultResponse(sourceExecutions.getExecutions(), "execution_statistics.zip", showUserInformation);
        } else {
            return ResponseEntity.ok(sourceExecutions);
        }
    }

    /**
     * Returns access trends statistics
     *
     * @param accessTrendsStatisticsRequest filter settings for statistics
     * @return access trends statistics in JSON or CSV format
     */
    @PostMapping(
        value = "/accesstrends",
        produces = MediaType.APPLICATION_JSON_VALUE,
        consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<?> accessStatistics(@RequestBody AccessTrendsStatisticsRequest accessTrendsStatisticsRequest) {
        if (!auditTrailEnabled) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Audit Trail functionality should be enabled (audit.trail.enabled) to serve this endpoint");
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        boolean showUserInformation = accessTrendsStatisticsRequest.isShowUserInformation();

        AccessTrendsDto trends = getAccessTrends(
                LocalDate.parse(accessTrendsStatisticsRequest.getStartDate(), formatter),
                LocalDate.parse(accessTrendsStatisticsRequest.getEndDate(), formatter),
                accessTrendsStatisticsRequest.getEndpoints(),
                showUserInformation);

        if (ResponseFormat.CSV.equals(accessTrendsStatisticsRequest.getResponseFormat())) {
            return prepareAccessTrendsResponse(trends.getTrends(), "execution_trends.zip", showUserInformation);
        } else {
            return ResponseEntity.ok(trends);
        }
    }

    private ResponseEntity<Resource> prepareExecutionResultResponse(List<SourceExecutionDto> executions, String filename, boolean showUserInformation) {
        List<String[]> data = executions.stream()
                .map(execution -> showUserInformation
                        ? new String[]{execution.getExecutionDate(), execution.getSourceName(), execution.getExecutionName(), execution.getUserId()}
                        : new String[]{execution.getExecutionDate(), execution.getSourceName(), execution.getExecutionName()}
                )
                .collect(Collectors.toList());
        return prepareResponse(data, filename, showUserInformation ? EXECUTION_STATISTICS_CSV_RESULT_HEADER_WITH_USER_ID : EXECUTION_STATISTICS_CSV_RESULT_HEADER);
    }

    private ResponseEntity<Resource> prepareAccessTrendsResponse(List<AccessTrendDto> trends, String filename, boolean showUserInformation) {
        List<String[]> data = trends.stream()
                .map(trend -> showUserInformation
                        ? new String[]{trend.getExecutionDate().toString(), trend.getEndpointName(), trend.getUserID()}
                        : new String[]{trend.getExecutionDate().toString(), trend.getEndpointName()}
                )
                .collect(Collectors.toList());
        return prepareResponse(data, filename, showUserInformation ? ACCESS_TRENDS_CSV_RESULT_HEADER_WITH_USER_ID : ACCESS_TRENDS_CSV_RESULT_HEADER);
    }

    private ResponseEntity<Resource> prepareResponse(List<String[]> data, String filename, List<String[]> header) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             StringWriter sw = new StringWriter();
             CSVWriter csvWriter = new CSVWriter(sw, ',', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER)) {

            csvWriter.writeAll(header);
            csvWriter.writeAll(data);
            csvWriter.flush();
            baos.write(sw.getBuffer().toString().getBytes());

            ByteArrayResource resource = new ByteArrayResource(baos.toByteArray());

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment; filename=\"%s\"", filename))
                    .body(resource);
        } catch (Exception ex) {
            LOG.error("An error occurred while building a response", ex);
            throw new RuntimeException(ex);
        }
    }

    // Request DTOs

    public static final class ExecutionStatisticsRequest {
        // Format - yyyy-MM-dd
        String startDate;
        // Format - yyyy-MM-dd
        String endDate;
        String sourceKey;
        ResponseFormat responseFormat;
        boolean showUserInformation;

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public String getSourceKey() {
            return sourceKey;
        }

        public void setSourceKey(String sourceKey) {
            this.sourceKey = sourceKey;
        }

        public ResponseFormat getResponseFormat() {
            return responseFormat;
        }

        public void setResponseFormat(ResponseFormat responseFormat) {
            this.responseFormat = responseFormat;
        }

        public boolean isShowUserInformation() {
            return showUserInformation;
        }

        public void setShowUserInformation(boolean showUserInformation) {
            this.showUserInformation = showUserInformation;
        }
    }

    public static final class AccessTrendsStatisticsRequest {
        // Format - yyyy-MM-dd
        String startDate;
        // Format - yyyy-MM-dd
        String endDate;
        // Key - method (POST, GET)
        // Value - endpoint ("{}" can be used as a placeholder, will be converted to ".*" in regular expression)
        List<EndpointDto> endpoints;
        ResponseFormat responseFormat;
        boolean showUserInformation;

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
            this.endDate = endDate;
        }

        public List<EndpointDto> getEndpoints() {
            return endpoints;
        }

        public void setEndpoints(List<EndpointDto> endpoints) {
            this.endpoints = endpoints;
        }

        public ResponseFormat getResponseFormat() {
            return responseFormat;
        }

        public void setResponseFormat(ResponseFormat responseFormat) {
            this.responseFormat = responseFormat;
        }

        public boolean isShowUserInformation() {
            return showUserInformation;
        }

        public void setShowUserInformation(boolean showUserInformation) {
            this.showUserInformation = showUserInformation;
        }
    }
}
