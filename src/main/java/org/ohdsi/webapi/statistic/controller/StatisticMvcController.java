package org.ohdsi.webapi.statistic.controller;

import com.opencsv.CSVWriter;
import org.ohdsi.webapi.mvc.AbstractMvcController;
import org.ohdsi.webapi.statistic.dto.AccessTrendDto;
import org.ohdsi.webapi.statistic.dto.AccessTrendsDto;
import org.ohdsi.webapi.statistic.dto.EndpointDto;
import org.ohdsi.webapi.statistic.dto.SourceExecutionDto;
import org.ohdsi.webapi.statistic.dto.SourceExecutionsDto;
import org.ohdsi.webapi.statistic.service.StatisticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spring MVC version of StatisticController
 *
 * Migration Status: Replaces /statistic/controller/StatisticController.java (Jersey)
 * Endpoints: 2 POST endpoints
 * Complexity: Medium - statistics with CSV generation
 */
@RestController
@RequestMapping("/statistic")
public class StatisticMvcController extends AbstractMvcController {

    private static final Logger log = LoggerFactory.getLogger(StatisticMvcController.class);

    private final StatisticService service;

    @Value("${audit.trail.enabled}")
    private boolean auditTrailEnabled;

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

    @Autowired
    public StatisticMvcController(StatisticService service) {
        this.service = service;
    }

    /**
     * Returns execution statistics
     *
     * Jersey: POST /WebAPI/statistic/executions
     * Spring MVC: POST /WebAPI/v2/statistic/executions
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

        SourceExecutionsDto sourceExecutions = service.getSourceExecutions(
                LocalDate.parse(executionStatisticsRequest.getStartDate(), formatter),
                LocalDate.parse(executionStatisticsRequest.getEndDate(), formatter),
                executionStatisticsRequest.getSourceKey(),
                showUserInformation);

        if (ResponseFormat.CSV.equals(executionStatisticsRequest.getResponseFormat())) {
            return prepareExecutionResultResponse(sourceExecutions.getExecutions(), "execution_statistics.zip", showUserInformation);
        } else {
            return ok(sourceExecutions);
        }
    }

    /**
     * Returns access trends statistics
     *
     * Jersey: POST /WebAPI/statistic/accesstrends
     * Spring MVC: POST /WebAPI/v2/statistic/accesstrends
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

        AccessTrendsDto trends = service.getAccessTrends(
                LocalDate.parse(accessTrendsStatisticsRequest.getStartDate(), formatter),
                LocalDate.parse(accessTrendsStatisticsRequest.getEndDate(), formatter),
                accessTrendsStatisticsRequest.getEndpoints(),
                showUserInformation);

        if (ResponseFormat.CSV.equals(accessTrendsStatisticsRequest.getResponseFormat())) {
            return prepareAccessTrendsResponse(trends.getTrends(), "execution_trends.zip", showUserInformation);
        } else {
            return ok(trends);
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
            log.error("An error occurred while building a response", ex);
            throw new RuntimeException(ex);
        }
    }

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
