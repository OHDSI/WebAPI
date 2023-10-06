package org.ohdsi.webapi.statistic.controller;

import com.opencsv.CSVWriter;
import org.apache.commons.lang3.tuple.Pair;
import org.ohdsi.webapi.statistic.dto.AccessTrendDto;
import org.ohdsi.webapi.statistic.dto.AccessTrendsDto;
import org.ohdsi.webapi.statistic.dto.SourceExecutionDto;
import org.ohdsi.webapi.statistic.dto.SourceExecutionsDto;
import org.ohdsi.webapi.statistic.service.StatisticService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Controller;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@Path("/statistic/")
@ConditionalOnProperty(value = "audit.trail.enabled", havingValue = "true")
public class StatisticController {
    private StatisticService service;

    private static final List<String[]> EXECUTION_STASTICS_HEADER = new ArrayList<String[]>() {{
        add(new String[]{"Date", "Source", "Execution Type"});
    }};

    public StatisticController(StatisticService service) {
        this.service = service;
    }

    /**
     * Returns execution statistics
     * @param executionStatisticsRequest - filter settings for statistics
     */
    @GET
    @Path("/executions")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response executionStatistics(ExecutionStatisticsRequest executionStatisticsRequest) {
        SourceExecutionsDto sourceExecutions = service.getSourceExecutions(executionStatisticsRequest.getStartDate(),
                executionStatisticsRequest.getEndDate(), executionStatisticsRequest.getSourceKey());

        return prepareExecutionResultResponse(sourceExecutions.getExecutions(), "execution_statistics.zip");
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
        AccessTrendsDto trends = service.getAccessTrends(accessTrendsStatisticsRequest.getStartDate(),
                accessTrendsStatisticsRequest.getEndDate(), accessTrendsStatisticsRequest.getEndpoints());

        return prepareAccessTrendsResponse(trends.getTrends(), "execution_trends.zip");
    }

    private Response prepareExecutionResultResponse(List<SourceExecutionDto> executions, String filename) {
        List<String[]> data = executions.stream()
                .flatMap(execution ->
                        new ArrayList<String[]>() {{
                            add(new String[]{execution.getExecutionDate().toString(), execution.getSourceName(), execution.getExecutionName()});
                        }}.stream()
                )
                .collect(Collectors.toList());
        return prepareResponse(data, filename);
    }

    private Response prepareAccessTrendsResponse(List<AccessTrendDto> trends, String filename) {
        List<String[]> data = trends.stream()
                .flatMap(trend ->
                        new ArrayList<String[]>() {{
                            add(new String[]{trend.getExecutionDate().toString(), trend.getEndpointName()});
                        }}.stream()
                )
                .collect(Collectors.toList());
        return prepareResponse(data, filename);
    }

    private Response prepareResponse(List<String[]> data, String filename) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            StringWriter sw = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(sw, ',', CSVWriter.DEFAULT_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER);
            csvWriter.writeAll(EXECUTION_STASTICS_HEADER);
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

    private static final class ExecutionStatisticsRequest {
        // Format - yyyy-MM-dd
        LocalDate startDate;
        // Format - yyyy-MM-dd
        LocalDate endDate;
        String sourceKey;

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public String getSourceKey() {
            return sourceKey;
        }

        public void setSourceKey(String sourceKey) {
            this.sourceKey = sourceKey;
        }
    }

    private static final class AccessTrendsStatisticsRequest {
        // Format - yyyy-MM-dd
        LocalDate startDate;
        // Format - yyyy-MM-dd
        LocalDate endDate;
        // Key - method (POST, GET)
        // Value - endpoint ("{}" can be used as a placeholder, will be converted to ".*" in regular expression)
        List<Pair<String, String>> endpoints;

        public LocalDate getStartDate() {
            return startDate;
        }

        public void setStartDate(LocalDate startDate) {
            this.startDate = startDate;
        }

        public LocalDate getEndDate() {
            return endDate;
        }

        public void setEndDate(LocalDate endDate) {
            this.endDate = endDate;
        }

        public List<Pair<String, String>> getEndpoints() {
            return endpoints;
        }

        public void setEndpoints(List<Pair<String, String>> endpoints) {
            this.endpoints = endpoints;
        }
    }
}
