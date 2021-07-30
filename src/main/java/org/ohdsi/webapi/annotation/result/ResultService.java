package org.ohdsi.webapi.annotation.result;

import io.swagger.models.auth.In;
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
import java.util.stream.Collectors;

@Service
public class ResultService extends AbstractDaoService {

  @Autowired
  private CohortSampleRepository sampleRepository;

  @Autowired
  private ResultRepository resultRepository;

  @Autowired
  private AnnotationService annotationService;

//  public List<Result> getResultsByAnnotationID(int AnnotationID) {
//    return resultRepository.findByAnnotationId(AnnotationID);
//  }

  public List<Result> getResultsByAnnotationID(int AnnotationID){
    Annotation ourAnno =annotationService.getAnnotationsByAnnotationId(AnnotationID);
    return getResultsByAnnotation(ourAnno);
  }

  public List<Result> getResultsByAnnotation(Annotation annotation){
    CohortSample sample = sampleRepository.findById(annotation.getCohortSampleId());
    Source source = getSourceRepository().findBySourceId(sample.getSourceId());
    JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
    PreparedStatementRenderer renderer = new PreparedStatementRenderer(source, "/resources/annotationresult/sql/findResultsByAnnotationId.sql",
            new String[]{"results_schema", "CDM_schema"},
            new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results), source.getTableQualifier(SourceDaimon.DaimonType.CDM)},
            "annotation_id", annotation.getId());
    return jdbcTemplate.query(renderer.getSql(),new ResultRowMapper());
  }

  public List<Result> getResultsByAnnotations(List<Annotation> annotations){
    String AnnotationIds=""+annotations.get(0).getId();
    int[] annotationIds = annotations.stream()
            .mapToInt(Annotation::getId)
            .toArray();
    System.out.println("AnnotationIDs: "+AnnotationIds);
    CohortSample sample = sampleRepository.findById(annotations.get(0).getCohortSampleId());
    Source source = getSourceRepository().findBySourceId(sample.getSourceId());
    JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
    PreparedStatementRenderer renderer = new PreparedStatementRenderer(source, "/resources/annotationresult/sql/findResultsByAnnotationIds.sql",
            new String[]{"results_schema", "CDM_schema"},
            new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results), source.getTableQualifier(SourceDaimon.DaimonType.CDM)},
            "idList", annotationIds);
    System.out.println(renderer);
    return jdbcTemplate.query(renderer.getSql(),renderer.getOrderedParams(),new ResultRowMapper());
  }

  public Result getResultByAnnotationIDAndQuestionID(int AnnotationID,int QuestionID){
    System.out.printf("checking for result with questionID:%s and annotationID:%s \n",QuestionID,AnnotationID);
    Annotation ourAnno =annotationService.getAnnotationsByAnnotationId(AnnotationID);
    CohortSample sample = sampleRepository.findById(ourAnno.getCohortSampleId());
    Source source = getSourceRepository().findBySourceId(sample.getSourceId());
    JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
    String[] sqlParameters = new String[] { "annotation_id", "question_id"};
    Object[] sqlValues = new Object[] {AnnotationID,QuestionID};
    PreparedStatementRenderer renderer = new PreparedStatementRenderer(source, "/resources/annotationresult/sql/findResultsByAnnotationIdAndQuestionId.sql",
            new String[]{"results_schema", "CDM_schema"},
            new String[]{source.getTableQualifier(SourceDaimon.DaimonType.Results), source.getTableQualifier(SourceDaimon.DaimonType.CDM)},
            sqlParameters, sqlValues);
    System.out.printf("Running query: %s with params: %s\n",renderer.getSql(),renderer.getOrderedParams().toString());
    List<Result> results= jdbcTemplate.query(renderer.getSql(), new ResultRowMapper());
    if (results.isEmpty()){
      return null;
    }
    return results.get(0);
  }

  public <AnnotationCollection> List<Result> getResultsByQuestionSetId(int questionSetId) {
    List <Result> results = new ArrayList<Result>();
    List <Annotation> annos=annotationService.getAnnotationsByQuestionSetId(questionSetId);
    List<ArrayList<Annotation>> collections = annos.stream()
            .collect(Collectors.groupingBy(x -> x.getCohortSampleId()))
            .entrySet().stream()
            .map(e -> { ArrayList<Annotation> c = new ArrayList<Annotation>(); c.addAll(e.getValue()); return c; })
            .collect(Collectors.toList());
    for(ArrayList<Annotation> annoList : collections){
      results.addAll(getResultsByAnnotations(annoList));
    }
    return results;
  }

  public void deleteResultsByAnnotationIdAndQuestionId(Annotation annotation,int questionId){
    List<Object[]> deleteVariables = new ArrayList<>();
    CohortSample sample = sampleRepository.findById(annotation.getCohortSampleId());
    Source source = getSourceRepository().findBySourceId(sample.getSourceId());
    JdbcTemplate jdbcTemplate = getSourceJdbcTemplate(source);
    String[] parameters = new String[] { "results_schema" };
    String[] parameterValues = new String[] { source.getTableQualifier(SourceDaimon.DaimonType.Results) };
    String[] deletesqlParameters = new String[] { "annotation_id", "question_id"};
    Object[] deletesqlValues = new Object[] {annotation.getId(),questionId};
    PreparedStatementRenderer deleterenderer = new PreparedStatementRenderer(source, "/resources/annotationresult/sql/deleteResultsByAnnotationIdAndQuestionId.sql", parameters, parameterValues, deletesqlParameters, deletesqlValues);
    String deleteStatement = deleterenderer.getSql();
    deleteVariables.add(deleterenderer.getOrderedParams());
    jdbcTemplate.batchUpdate(deleteStatement, deleteVariables);
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
    List<Object[]> variables = new ArrayList<>();
    Boolean hasCleared=false;
    for(int i=0; i < results.length(); i++){
      JSONObject object = results.getJSONObject(i);
      if(!hasCleared && getResultByAnnotationIDAndQuestionID(annotation.getId(),Integer.parseInt(object.get("questionId").toString()))!=null){
//        this entry already exists, need to update here instead of adding to the pile
        deleteResultsByAnnotationIdAndQuestionId(annotation,Integer.parseInt(object.get("questionId").toString()));
        hasCleared=true;
      }
      Object[] sqlValues = new Object[] {
              annotation.getId(),
              Integer.parseInt(object.get("questionId").toString()),
              Integer.parseInt(object.get("answerId").toString()),
              object.get("value").toString(),
              object.get("type").toString() };

      PreparedStatementRenderer renderer = new PreparedStatementRenderer(source, "/resources/annotationresult/sql/insertResults.sql", parameters, parameterValues, sqlParameters, sqlValues);

      if (statement == null) {
        statement = renderer.getSql();
        System.out.printf("statement: %s\n",statement);
      }

      variables.add(renderer.getOrderedParams());
    }
    System.out.printf("variables: %s\n",variables.toArray().toString());
    jdbcTemplate.batchUpdate(statement, variables);
  }

  /** Maps a SQL result to a sample element. */
  private class ResultRowMapper implements RowMapper<Result> {

    ResultRowMapper() {
    }

    @Override
    public Result mapRow(ResultSet rs, int rowNum) throws SQLException {
      int AnnotationIdInt=rs.getInt("annotation_id");
      if(AnnotationIdInt == 0){
        System.out.println("Annotation was null, none found");
        return null;
      }
      Result result = new Result();
      Annotation tempAnno = annotationService.getAnnotationsByAnnotationId(AnnotationIdInt);
      if(tempAnno == null){
        System.out.println("Annotation was null, none found");
        return null;
      }
      result.setAnnotation(tempAnno);
      result.setId(rs.getInt("result_id"));
      result.setQuestionId(rs.getInt("question_id"));
      result.setAnswerId(rs.getInt("answer_id"));
      result.setValue(rs.getString("value"));
      result.setType(rs.getString("type"));
      return result;
    }
  }
}
