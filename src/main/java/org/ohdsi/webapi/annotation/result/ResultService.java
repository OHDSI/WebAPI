package org.ohdsi.webapi.annotation.result;

import org.json.JSONArray;
import org.json.JSONObject;
import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.annotation.annotation.AnnotationService;
import org.ohdsi.webapi.annotation.result.Result;
import org.ohdsi.webapi.annotation.result.ResultRepository;
import org.ohdsi.webapi.cohortsample.CohortSample;
import org.ohdsi.webapi.cohortsample.CohortSampleRepository;
import org.ohdsi.webapi.cohortsample.CohortSamplingService;
import org.ohdsi.webapi.cohortsample.SampleElement;
import org.ohdsi.webapi.service.AbstractDaoService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceDaimon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.ohdsi.webapi.util.PreparedStatementRenderer;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class ResultService extends AbstractDaoService {

  @Autowired
  private CohortSampleRepository sampleRepository;

  @Autowired
  private ResultRepository resultRepository;

  @Autowired
  private AnnotationService annotationService;

  public List<Result> getResultsByAnnotationID(int AnnotationID) {
    return resultRepository.findByAnnotationId(AnnotationID);
  }

  public Result getLatestResultByAnnotationID(int AnnotationID){
    Result result= null;
    Annotation ourAnno =annotationService.getAnnotationsByAnnotationId(AnnotationID);
    CohortSample sample = sampleRepository.findById(ourAnno.getCohortSampleId());
    Source source = getSourceRepository().findBySourceId(sample.getSourceId());
    JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
    PreparedStatementRenderer renderer = new PreparedStatementRenderer(source, "/resources/annotationresult/sql/findResultsByAnnotationId.sql",
            new String[]{"results_schema", "CDM_schema"},
            new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results), source.getTableQualifier(SourceDaimon.DaimonType.CDM)},
            "annotationId", AnnotationID);
    Collection<String>  optionalFields = Collections.emptySet();
    return jdbcTemplate.query(renderer.getSql(), renderer.getOrderedParams(),new ResultRowMapper(optionalFields)).get(0);
  }

  public List<Result> findByQuestionId(int questionID) {
    return resultRepository.findByQuestionId(questionID);
  }

  public void insertResults(Annotation annotation, JSONArray results) {
//    might want to do a check- if the annotation ID+question ID already exists, update the existing row instead. Other option is just only query the latest instead
    CohortSample sample = sampleRepository.findById(annotation.getCohortSampleId());
    Source source = getSourceRepository().findBySourceId(sample.getSourceId());
    JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
    if (results.isNull(0)) {
      return;
    }

    String[] parameters = new String[] { "results_schema" };
    String[] parameterValues = new String[] { source.getTableQualifier(SourceDaimon.DaimonType.Results) };
    String[] sqlParameters = new String[] { "annotation_id", "question_id", "answer_id", "value", "type" };

    String statement = null;
    List<Object[]> variables = new ArrayList<>(results.length());
    for(int i=0; i < results.length(); i++){
      JSONObject object = results.getJSONObject(i);
      Object[] sqlValues = new Object[] {
              annotation.getId(),
              Long.parseLong(object.get("questionId").toString()),
              Long.parseLong(object.get("answerId").toString()),
              object.get("value").toString(),
              object.get("type").toString() };

      PreparedStatementRenderer renderer = new PreparedStatementRenderer(source, "/resources/annotationresult/sql/insertResults.sql", parameters, parameterValues, sqlParameters, sqlValues);

      if (statement == null) {
        statement = renderer.getSql();
        System.out.printf("statement: %s\n",statement);
      }

      variables.add(renderer.getOrderedParams());
    }
    System.out.printf("variables: %s\n",variables);
    jdbcTemplate.batchUpdate(statement, variables);
  }

  /** Maps a SQL result to a sample element. */
  private static class ResultRowMapper implements RowMapper<Result> {
    private final Collection<String> optionalFields;

    @Autowired
    private AnnotationService annotationService;

    ResultRowMapper(Collection<String> optionalFields) {
      this.optionalFields = optionalFields;
    }

    @Override
    public Result mapRow(ResultSet rs, int rowNum) throws SQLException {
      Result result = new Result();
      result.setAnnotation(annotationService.getAnnotationsByAnnotationId(rs.getInt("annotation_id")));
      result.setQuestionId(rs.getLong("question_id"));
      result.setAnswerId(rs.getLong("answer_id"));
      result.setValue(rs.getString("value"));
      result.setType(rs.getString("type"));
      return result;
    }
  }
}
