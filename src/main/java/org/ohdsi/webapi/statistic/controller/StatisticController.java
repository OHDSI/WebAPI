package org.ohdsi.webapi.statistic.controller;

import com.opencsv.CSVWriter;

import org.ohdsi.webapi.statistic.dto.AccessTrendDto;
import org.ohdsi.webapi.statistic.dto.AccessTrendsDto;
import org.ohdsi.webapi.statistic.dto.EndpointDto;
import org.ohdsi.webapi.statistic.dto.SourceExecutionDto;
import org.ohdsi.webapi.statistic.dto.SourceExecutionsDto;
import org.ohdsi.webapi.statistic.service.StatisticService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Path("/statistic/")
@ConditionalOnProperty(value = "audit.trail.enabled", havingValue = "true")
public class StatisticController {
    private StatisticService service;

    public enum ResponseFormat {
        CSV, JSON
    }

    private static final List<String[]> EXECUTION_STATISTICS_CSV_RESULT_HEADER = new ArrayList<String[]>() {{
        add(new String[]{"Date", "Source", "Execution Type"});
    }};
    
    private static final List<String[]> ACCESS_TRENDS_CSV_RESULT_HEADER = new ArrayList<String[]>() {{
        add(new String[]{"Date", "Endpoint", "UserID"});
    }};

    public StatisticController(StatisticService service) {
        this.service = service;
    }

    /**
     * Returns execution statistics
     * @param executionStatisticsRequest - filter settings for statistics
     */
    @POST
    @Path("/executions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executionStatistics(ExecutionStatisticsRequest executionStatisticsRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        boolean showUserInformation = executionStatisticsRequest.isShowUserInformation();
        
        SourceExecutionsDto sourceExecutions = service.getSourceExecutions(LocalDate.parse(executionStatisticsRequest.getStartDate(), formatter),
                LocalDate.parse(executionStatisticsRequest.getEndDate(), formatter), executionStatisticsRequest.getSourceKey(), showUserInformation);

        if (ResponseFormat.CSV.equals(executionStatisticsRequest.getResponseFormat())) {
            return prepareExecutionResultResponse(sourceExecutions.getExecutions(), "execution_statistics.zip", showUserInformation);
        } else {
            return Response.ok(sourceExecutions).build();
        }
    }

    /**
     * Returns access trends statistics
     * @param accessTrendsStatisticsRequest - filter settings for statistics
     */
    @POST
    @Path("/accesstrends")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response accessStatistics(AccessTrendsStatisticsRequest accessTrendsStatisticsRequest) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        boolean showUserInformation = accessTrendsStatisticsRequest.isShowUserInformation();

        AccessTrendsDto trends = service.getAccessTrends(LocalDate.parse(accessTrendsStatisticsRequest.getStartDate(), formatter),
                LocalDate.parse(accessTrendsStatisticsRequest.getEndDate(), formatter), accessTrendsStatisticsRequest.getEndpoints(), showUserInformation);

        if (ResponseFormat.CSV.equals(accessTrendsStatisticsRequest.getResponseFormat())) {
            return prepareAccessTrendsResponse(trends.getTrends(), "execution_trends.zip", showUserInformation);
        } else {
            return Response.ok(trends).build();
        }
    }

    private Response prepareExecutionResultResponse(List<SourceExecutionDto> executions, String filename, boolean showUserInformation) {
        updateExecutionStatisticsHeader(showUserInformation);
        List<String[]> data = executions.stream()
                .map(execution -> showUserInformation
                        ? new String[]{execution.getExecutionDate(), execution.getSourceName(), execution.getExecutionName(), execution.getUserID()}
                        : new String[]{execution.getExecutionDate(), execution.getSourceName(), execution.getExecutionName()}
                )
                .collect(Collectors.toList());
        return prepareResponse(data, filename, EXECUTION_STATISTICS_CSV_RESULT_HEADER);
    }

    private Response prepareAccessTrendsResponse(List<AccessTrendDto> trends, String filename, boolean showUserInformation) {
        updateAccessTrendsHeader(showUserInformation);
        List<String[]> data = trends.stream()
                .map(trend -> showUserInformation
                        ? new String[]{trend.getExecutionDate().toString(), trend.getEndpointName(), trend.getUserID()}
                        : new String[]{trend.getExecutionDate().toString(), trend.getEndpointName()}
                )
                .collect(Collectors.toList());
        return prepareResponse(data, filename, ACCESS_TRENDS_CSV_RESULT_HEADER);
    }

    private Response prepareResponse(List<String[]> data, String filename, List<String[]> header) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            StringWriter sw = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(sw, ',', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
            csvWriter.writeAll(header);
            csvWriter.writeAll(data);
            csvWriter.flush();
            baos.write(sw.getBuffer().toString().getBytes());

            return Response
                    .ok(baos)
                    .type(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", String.format("attachment; filename=\"%s\"", filename))
                    .build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void updateExecutionStatisticsHeader(boolean showUserInformation) {
        EXECUTION_STATISTICS_CSV_RESULT_HEADER.clear();
        if (showUserInformation) {
            EXECUTION_STATISTICS_CSV_RESULT_HEADER.add(new String[]{"Date", "Source", "Execution Type", "User ID"});
        } else {
            EXECUTION_STATISTICS_CSV_RESULT_HEADER.add(new String[]{"Date", "Source", "Execution Type"});
        }
    }

    private void updateAccessTrendsHeader(boolean showUserInformation) {
        ACCESS_TRENDS_CSV_RESULT_HEADER.clear();
        if (showUserInformation) {
            ACCESS_TRENDS_CSV_RESULT_HEADER.add(new String[]{"Date", "Endpoint", "UserID"});
        } else {
            ACCESS_TRENDS_CSV_RESULT_HEADER.add(new String[]{"Date", "Endpoint"});
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
