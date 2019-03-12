package org.ohdsi.webapi.cohortanalysis;

import jersey.repackaged.com.google.common.base.Joiner;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.ArrayUtils;
import org.ohdsi.circe.helper.ResourceHelper;
import org.ohdsi.sql.SqlRender;
import org.ohdsi.webapi.cohortresults.PeriodType;
import org.ohdsi.webapi.source.SourceDaimon;
import org.ohdsi.webapi.util.CSVRecordMapper;
import org.springframework.beans.factory.BeanInitializationException;
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

  private final static String HERACLES_ANALYSES_TABLE = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/heraclesanalyses/csv/heraclesAnalyses.csv");

  private final static String HERACLES_ANALYSES_PARAMS = ResourceHelper.GetResourceAsString("/resources/cohortanalysis/heraclesanalyses/csv/heraclesAnalysesParams.csv");

  private final static String ANALYSES_QUERY_PREFIX = "/resources/cohortanalysis/heraclesanalyses/sql/";

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
    analysesParamsMap.keySet().forEach(id -> {
      HeraclesAnalysis analysis = heraclesAnalysisMap.get(id);
      if (Objects.nonNull(analysis)) {
        Set<HeraclesAnalysisParameter> params = analysesParamsMap.get(id);
        params.add(new HeraclesAnalysisParameter(id, "analysisId", id.toString()));
        params.add(new HeraclesAnalysisParameter(id, "analysisName", analysis.getName()));
      }
    });
  }

  private <T> List<T> parseCSV(String source, CSVRecordMapper<T> mapper) {

    List<T> result = new ArrayList<>();
    try(Reader in = new StringReader(source)) {
      CSVParser parser = new CSVParser(in, CSVFormat.RFC4180.withSkipHeaderRecord());
      for(final CSVRecord record : parser.getRecords()) {
        result.add(mapper.mapRecord(record));
      }
    } catch (IOException e) {
      throw new BeanInitializationException("Failed to read heracles analyses");
    }
    return result;
  }

  public String buildHeraclesAnalysisQuery(CohortAnalysisTask task) {

    return new Builder(task).buildQuery();
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

    public Builder(CohortAnalysisTask analysisTask) {

      this.analysisTask = analysisTask;
      this.values = buildAnalysisParams(this.analysisTask);
      this.analysesIds = analysisTask.getAnalysisIds().stream().map(Integer::parseInt).collect(Collectors.toList());
    }

    public String buildQuery() {

      return buildInitQuery() + "\n" + buildAnalysesQueries();
    }

    private String buildInitQuery() {

      return SqlRender.renderSql(INIT_QUERY, PARAM_NAMES, values);
    }

    private String buildAnalysesQueries() {

      return analysesIds.stream().map(this::getAnalysisQuery).collect(Collectors.joining("\n"));
    }

    private String getAnalysisQuery(Integer id) {

      HeraclesAnalysis analysis = heraclesAnalysisMap.get(id);
      if (Objects.nonNull(analysis)) {
        String query = ResourceHelper.GetResourceAsString(ANALYSES_QUERY_PREFIX + analysis.getFilename());
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
        return SqlRender.renderSql(query, paramNames, paramValues);
      } else {
        return "";
      }
    }

    private String buildSelectResultQuery() {

      StringBuilder resultsQuery = new StringBuilder();
      StringBuilder distResultsQuery = new StringBuilder();
      analysesIds.forEach(id -> {
        HeraclesAnalysis analysis = heraclesAnalysisMap.get(id);
        if (Objects.nonNull(analysis)) {
          if (analysis.isHasResults()) {
            resultsQuery.append()
          }
        }
      });
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
