package org.ohdsi.webapi.cohortanalysis;

import jersey.repackaged.com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.sql.SqlTranslate;
import org.ohdsi.webapi.cohortresults.PeriodType;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.CSVRecordMapper;
import org.ohdsi.webapi.util.SessionUtils;
import org.ohdsi.webapi.util.SourceUtils;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class HeraclesQueryBuilder {

  private final static String INIT_QUERY = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/initHeraclesAnalyses.sql");
  private final static String FINALIZE_QUERY = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/finalizeHeraclesAnalyses.sql");

  private final static String HERACLES_ANALYSES_TABLE = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/heraclesanalyses/csv/heraclesAnalyses.csv");

  private final static String HERACLES_ANALYSES_PARAMS = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/heraclesanalyses/csv/heraclesAnalysesParams.csv");

  private final static String ANALYSES_QUERY_PREFIX = "/resources/cohortanalysis/heraclesanalyses/sql/";

  private final static String INSERT_RESULT_STATEMENT = "insert into @results_schema.heracles_results (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, count_value)\n";
  private final static String INSERT_DIST_RESULT_STATEMENT = "insert into @results_schema.heracles_results_dist (cohort_definition_id, analysis_id, stratum_1, stratum_2, stratum_3, stratum_4, stratum_5, count_value, min_value, max_value, avg_value, stdev_value, median_value, p10_value, p25_value, p75_value, p90_value)\n";
  private final static String SELECT_RESULT_STATEMENT = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/selectHeraclesResults.sql");
  private final static String SELECT_DIST_RESULT_STATEMENT = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/sql/selectHeraclesDistResults.sql");

  private final static String[] PARAM_NAMES = new String[]{"CDM_schema", "results_schema", "source_name",
          "smallcellcount", "runHERACLESHeel", "CDM_version", "cohort_definition_id", "list_of_analysis_ids",
          "condition_concept_ids", "drug_concept_ids", "procedure_concept_ids", "observation_concept_ids",
          "measurement_concept_ids", "cohort_period_only", "source_id", "periods", "rollupUtilizationVisit", "rollupUtilizationDrug"};

  private Map<Integer, HeraclesAnalysis> heraclesAnalysisMap;
  private Map<Integer, Set<HeraclesAnalysisParameter>> analysesParamsMap = new HashMap<>();

  @PostConstruct
  public void init() {

    initHeraclesAnalyses();
    initAnalysesParams();
  }

  private void initHeraclesAnalyses() {

    heraclesAnalysisMap = parseCSV(HERACLES_ANALYSES_TABLE, record -> new HeraclesAnalysis(Integer.parseInt(record.get("analysisId")),
            record.get("analysisName"), record.get("sqlFileName"),
            Boolean.parseBoolean(record.get("results")), Boolean.parseBoolean(record.get("distResults"))))
            .stream()
            .collect(Collectors.toMap(HeraclesAnalysis::getId, analysis -> analysis));
  }

  private void initAnalysesParams() {

    parseCSV(HERACLES_ANALYSES_PARAMS, record -> new HeraclesAnalysisParameter(Integer.parseInt(record.get("analysisId")),
            record.get("paramName"), record.get("paramValue")))
      .forEach(p -> {
        Set<HeraclesAnalysisParameter> params = analysesParamsMap.getOrDefault(p.getAnalysisId(), new HashSet<>());
        params.add(p);
        analysesParamsMap.put(p.getAnalysisId(), params);
      });
    heraclesAnalysisMap.values().forEach(analysis -> {
      Integer id = analysis.getId();
      Set<HeraclesAnalysisParameter> params = analysesParamsMap.getOrDefault(id, new HashSet<>());
      params.add(new HeraclesAnalysisParameter(id, "analysisId", id.toString()));
      params.add(new HeraclesAnalysisParameter(id, "analysisName", analysis.getName()));
      analysesParamsMap.put(id, params);
    });
  }

  private <T> List<T> parseCSV(String source, CSVRecordMapper<T> mapper) {

    List<T> result = new ArrayList<>();
    try(Reader in = new StringReader(source)) {
      CSVParser parser = new CSVParser(in, CSVFormat.RFC4180.withFirstRecordAsHeader());
      for(final CSVRecord record : parser.getRecords()) {
        result.add(mapper.mapRecord(record));
      }
    } catch (IOException e) {
      throw new BeanInitializationException("Failed to read heracles analyses");
    }
    return result;
  }

  public String buildHeraclesAnalysisQuery(CohortAnalysisTask task) {

    String query = new Builder(task).buildQuery();
    return SqlTranslate.translateSql(query, task.getSource().getSourceDialect(), SessionUtils.sessionId(),
            SourceUtils.getTempQualifier(task.getSource()));
  }

  private String[] buildAnalysisParams(CohortAnalysisTask task) {

    String resultsTableQualifier = task.getSource().getTableQualifier(SourceDaimon.DaimonType.Results);
    String cdmTableQualifier = task.getSource().getTableQualifier(SourceDaimon.DaimonType.CDM);

    String cohortDefinitionIds = (task.getCohortDefinitionIds() == null ? "" : Joiner.on(",").join(
            task.getCohortDefinitionIds()));
    String analysisIds = (task.getAnalysisIds() == null ? "" : Joiner.on(",").join(task.getAnalysisIds()));
    String conditionIds = (task.getConditionConceptIds() == null ? "" : Joiner.on(",").join(
            task.getConditionConceptIds()));
    String drugIds = (task.getDrugConceptIds() == null ? "" : Joiner.on(",").join(task.getDrugConceptIds()));
    String procedureIds = (task.getProcedureConceptIds() == null ? "" : Joiner.on(",").join(
            task.getProcedureConceptIds()));
    String observationIds = (task.getObservationConceptIds() == null ? "" : Joiner.on(",").join(
            task.getObservationConceptIds()));
    String measurementIds = (task.getMeasurementConceptIds() == null ? "" : Joiner.on(",").join(
            task.getMeasurementConceptIds()));

    String concatenatedPeriods = "";
    if (CollectionUtils.isEmpty(task.getPeriods())) {
      // In this case summary stats will be calculated
      concatenatedPeriods = "''";
    } else {
      List<PeriodType> periods = CollectionUtils.isEmpty(task.getPeriods()) ? Arrays.asList(PeriodType.values()) : task.getPeriods();
      concatenatedPeriods = periods.stream()
              .map(PeriodType::getValue)
              .map(StringUtils::quote)
              .collect(Collectors.joining(","));
    }

    return new String[]{
            cdmTableQualifier, resultsTableQualifier, task.getSource().getSourceName(),
            String.valueOf(task.getSmallCellCount()), String.valueOf(task.runHeraclesHeel()).toUpperCase(),
            task.getCdmVersion(), cohortDefinitionIds, analysisIds, conditionIds, drugIds, procedureIds,
            observationIds, measurementIds,String.valueOf(task.isCohortPeriodOnly()),
            String.valueOf(task.getSource().getSourceId()), concatenatedPeriods,
            String.valueOf(task.getRollupUtilizationVisit()).toUpperCase(), String.valueOf(task.getRollupUtilizationDrug()).toUpperCase()
    };
  }


  private class Builder {
    private CohortAnalysisTask analysisTask;
    private String[] values;
    private List<Integer> analysesIds;
    private List<String> resultSql = new ArrayList<>();

    public Builder(CohortAnalysisTask analysisTask) {

      this.analysisTask = analysisTask;
      this.values = buildAnalysisParams(this.analysisTask);
      this.analysesIds = analysisTask.getAnalysisIds().stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public String buildQuery() {

      return buildQuery(INIT_QUERY)
              .buildAnalysesQueries()
              .buildSelectResultQuery()
              .buildQuery(FINALIZE_QUERY)
              .toSql();
    }

    private Builder buildQuery(String query) {

      resultSql.add(SqlRender.renderSql(query, PARAM_NAMES, values));
      return this;
    }

    private Builder buildAnalysesQueries() {

      resultSql.add(analysesIds.stream().map(this::getAnalysisQuery).collect(Collectors.joining("\n")));
      return this;
    }

    private Builder buildSelectResultQuery() {

      StringBuilder result = new StringBuilder();
      List<String> resultsQuery = new ArrayList<>();
      List<String> distResultsQuery = new ArrayList<>();
      analysesIds.forEach(id -> {
        HeraclesAnalysis analysis = heraclesAnalysisMap.get(id);
        if (Objects.nonNull(analysis)) {
          Pair<String[], String[]> params = getAnalysisParams(id);
          if (analysis.isHasResults()) {
            resultsQuery.add(SqlRender.renderSql(SELECT_RESULT_STATEMENT, params.getFirst(), params.getSecond()));
          }
          if (analysis.isHasDistResults()) {
            distResultsQuery.add(SqlRender.renderSql(SELECT_DIST_RESULT_STATEMENT, params.getFirst(), params.getSecond()));
          }
        }
      });
      if (!resultsQuery.isEmpty()) {
        result.append(SqlRender.renderSql(INSERT_RESULT_STATEMENT, PARAM_NAMES, values))
                .append(resultsQuery.stream().collect(Collectors.joining("\nUNION ALL\n")))
                .append(";")
                .append("\n");
      }
      if (!distResultsQuery.isEmpty()) {
        result.append(SqlRender.renderSql(INSERT_DIST_RESULT_STATEMENT, PARAM_NAMES, values))
                .append(distResultsQuery.stream().collect(Collectors.joining("\nUNION ALL\n")))
                .append(";")
                .append("\n");
      }
      resultSql.add(result.toString());
      return this;
    }

    private String toSql() {

      return resultSql.stream().collect(Collectors.joining("\n"));
    }

    private String getAnalysisQuery(Integer id) {

      HeraclesAnalysis analysis = heraclesAnalysisMap.get(id);
      if (Objects.nonNull(analysis)) {
        String query = ResourceHelper.GetResourceAsString(ANALYSES_QUERY_PREFIX + analysis.getFilename());
        Pair<String[], String[]> params = getAnalysisParams(id);
        return SqlRender.renderSql(query, params.getFirst(), params.getSecond());
      } else {
        return "";
      }
    }

    private Pair<String[], String[]> getAnalysisParams(Integer id) {
      List<HeraclesAnalysisParameter> params = new ArrayList<>(analysesParamsMap.getOrDefault(id, new HashSet<>()));

      int size = params.size();
      String[] analysisParamNames = new String[size];
      String[] analysisParamValues = new String[size];
      for(int i = 0; i < size; i++) {
        analysisParamNames[i] = params.get(i).getParamName();
        analysisParamValues[i] = params.get(i).getValue();
      }
      String[] paramNames = ArrayUtils.addAll(PARAM_NAMES, analysisParamNames);
      String[] paramValues = ArrayUtils.addAll(values, analysisParamValues);
      return Pair.of(paramNames, paramValues);
    }

  }

  static class HeraclesAnalysis {
    private Integer id;
    private String name;
    private String filename;
    private boolean hasResults;
    private boolean hasDistResults;

    public HeraclesAnalysis(Integer id, String name, String filename, boolean hasResults, boolean hasDistResults) {
      this.id = id;
      this.name = name;
      this.filename = filename;
      this.hasResults = hasResults;
      this.hasDistResults = hasDistResults;
    }

    public Integer getId() {
      return id;
    }

    public String getName() {
      return name;
    }

    public String getFilename() {
      return filename;
    }

    public boolean isHasResults() {
      return hasResults;
    }

    public boolean isHasDistResults() {
      return hasDistResults;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof HeraclesAnalysis)) return false;
      HeraclesAnalysis analysis = (HeraclesAnalysis) o;
      return Objects.equals(id, analysis.id) &&
              Objects.equals(name, analysis.name);
    }

    @Override
    public int hashCode() {

      return Objects.hash(id, name);
    }
  }

  static class HeraclesAnalysisParameter {
    private Integer analysisId;
    private String paramName;
    private String value;

    public HeraclesAnalysisParameter(Integer analysisId, String paramName, String value) {

      this.analysisId = analysisId;
      this.paramName = paramName;
      this.value = value;
    }

    public Integer getAnalysisId() {

      return analysisId;
    }

    public String getParamName() {

      return paramName;
    }

    public String getValue() {

      return value;
    }

    @Override
    public boolean equals(Object o) {

      if (this == o) return true;
      if (!(o instanceof HeraclesAnalysisParameter)) return false;
      HeraclesAnalysisParameter that = (HeraclesAnalysisParameter) o;
      return Objects.equals(analysisId, that.analysisId) &&
              Objects.equals(paramName, that.paramName) &&
              Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {

      return Objects.hash(analysisId, paramName, value);
    }
  }

}
