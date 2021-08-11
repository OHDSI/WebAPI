package org.ohdsi.webapi.annotation.result;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.checkerframework.checker.units.qual.A;
import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.annotation.annotation.AnnotationService;
import org.ohdsi.webapi.annotation.annotation.AnnotationSummary;
import org.ohdsi.webapi.annotation.question.Question;
import org.ohdsi.webapi.annotation.question.QuestionService;
import org.ohdsi.webapi.annotation.set.QuestionSet;
import org.ohdsi.webapi.annotation.set.QuestionSetService;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortsample.dto.CohortSampleDTO;
import org.ohdsi.webapi.service.CohortSampleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.annotation.result.ResultService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Path("/annotations/results")
@Component
public class ResultController {

  @Autowired
  private CohortSampleService cohortSampleService;

  @Autowired
  private ResultService resultService;

  @Autowired
  private QuestionService questionService;

  @Autowired
  private QuestionSetService questionSetService;

  @Autowired
  private AnnotationService annotationService;

  @Autowired
  private CohortDefinitionRepository cohortDefinitionRepository;

  @GET
  @Path("/{annotationID}")
  @Produces(MediaType.APPLICATION_JSON)
  public ArrayList getResults(@PathParam("annotationID") String annotationID) {
    int annotationIdInt=Integer.parseInt(annotationID);
    ArrayList al = new ArrayList();
    Annotation ourAnno =annotationService.getAnnotationsByAnnotationId(annotationIdInt);
    List<Result> tempResults = resultService.getResultsByAnnotationID(annotationIdInt);
    for(Result tempResult : tempResults ){
      System.out.println(tempResult);
      Map<String, String> testDict =new HashMap<String, String>();
      testDict.put("questionId",String.valueOf(tempResult.getQuestionId()));
      testDict.put("answerId",String.valueOf(tempResult.getAnswerId()));
      testDict.put("subjectId",Integer.toString(ourAnno.getSubjectId()));
      al.add(testDict);
    }
    return al;
  }

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Result> getResults(
          @QueryParam("questionSetId") final int questionId
  ) {
    return resultService.getResultsByQuestionSetId(questionId);
  }

  @GET
  @Path("/completeResults")
  @Produces(MediaType.APPLICATION_JSON)
  public List<SuperResultDto> getFullResults(
          @QueryParam("questionSetId") final int questionSetId
  ) {
//    May want to replicate this but with an additional parameter-
//    this would be (quesetionSetID + CohortSampleId) or AnnotationStudyId (aka the same thing)
    List<Result> resultlist= resultService.getResultsByQuestionSetId(questionSetId);
    List<SuperResultDto> superList = new ArrayList();
    for (Result result : resultlist){
//      things we currently query for; Annotation Cohort Sample, Cohort Definition, QuestionSets
      Question myQuestion = questionService.getQuestionByQuestionId(result.getQuestionId());
      SuperResultDto tempdto = new SuperResultDto(result);
      Annotation tempanno = annotationService.getAnnotationsByAnnotationId(result.getAnnotation());
      tempdto.setCohortSampleId(tempanno.getCohortSampleId());
      tempdto.setPatientId(tempanno.getSubjectId());
      CohortSampleDTO cohortSample = cohortSampleService.getCohortSample(tempanno.getCohortSampleId(),"" );
      CohortDefinition cohortDefinition= cohortDefinitionRepository.findOneWithDetail(cohortSample.getCohortDefinitionId());
      tempdto.setCohortName(cohortDefinition.getName());
      tempdto.setCohortId( cohortSample.getCohortDefinitionId());
      tempdto.setDataSourceId(cohortSample.getSourceId());
//      for above, it would be more useful to use DataSourceKey instead of ID
      tempdto.setCohortSampleName(cohortSample.getName());
      tempdto.setQuestionSetId(questionSetId);
      QuestionSet questionSet = questionSetService.findQuestionSetByQuestionSetId(questionSetId);
      tempdto.setQuestionSetName(questionSet.getName());
      tempdto.setCaseStatus(myQuestion.getCaseQuestion());
      tempdto.setQuestionText(myQuestion.getText());
      superList.add(tempdto);
    }
    return superList;
  }

  @Path("/{cohortDefinitionId}/{sourceKey}")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void createResult(){

  }
}
