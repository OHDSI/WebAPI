package org.ohdsi.webapi.statistic.service;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.ohdsi.webapi.statistic.dto.AccessTrendDto;
import org.ohdsi.webapi.statistic.dto.AccessTrendsDto;
import org.ohdsi.webapi.statistic.dto.EndpointDto;
import org.ohdsi.webapi.statistic.dto.SourceExecutionDto;
import org.ohdsi.webapi.statistic.dto.SourceExecutionsDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@ConditionalOnProperty(value = "audit.trail.enabled", havingValue = "true")
public class StatisticService {
    protected final Logger LOG = LoggerFactory.getLogger(getClass());

    @Value("${audit.trail.log.file}")
    private String absoluteLogFileName;

    private String logFileName;

    @Value("${audit.trail.log.file.pattern}")
    private String absoluteLogFileNamePattern;

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
        if (absoluteLogFileName == null || absoluteLogFileNamePattern == null) {
            throw new RuntimeException("Application statistics can't operate because of missing configuration values for the audit trail log file or its pattern");
        }
        
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
                                    .filter(Matcher::matches)
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
}
