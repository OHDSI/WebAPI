package org.ohdsi.webapi.annotation.result;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.annotation.annotation.AnnotationService;
import org.ohdsi.webapi.annotation.answer.Answer;
import org.ohdsi.webapi.annotation.answer.AnswerService;
import org.ohdsi.webapi.annotation.question.Question;
import org.ohdsi.webapi.annotation.question.QuestionService;
import org.ohdsi.webapi.annotation.study.Study;
import org.ohdsi.webapi.annotation.study.StudyService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Path("/annotations/results")
@Component
public class ResultController {

  @Autowired
  private ResultService resultService;

  @Autowired
  private QuestionService questionService;

  @Autowired
  private AnnotationService annotationService;

  @Autowired
  private StudyService studyService;

  @Autowired
  private SourceService sourceService;

  @Autowired
  private AnswerService answerService;

  @GET
  @Path("/{annotationID}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Result> getResultsByAnnotationId(@PathParam("annotationID") int annotationID) {
    return resultService.getResultsByAnnotationId(annotationID);
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
          @QueryParam("questionSetId") final int questionSetId,
          @QueryParam("cohortSampleId") final int cohortSampleId,
          @QueryParam("studyId") final int studyId
  ) {
    Study study = null;
    if(studyId!=0){
      study=studyService.getStudyById(studyId);
    }
    else if (questionSetId!=0 && cohortSampleId!=0){
      study=studyService.getStudyByQuestionSetIdAndSampleId(questionSetId,cohortSampleId);
    }
    else{
      return null;
    }
    List<Result> resultlist=resultService.getResultsByStudy(study);
    List<SuperResultDto> superList = new ArrayList();
    Source source = sourceService.findBySourceId(study.getCohortSample().getSourceId());
    for (Result result : resultlist){
      Question myQuestion = questionService.getQuestionByQuestionId(result.getQuestionId());
      SuperResultDto tempdto = new SuperResultDto(result);
      Annotation tempanno = annotationService.getAnnotationsByAnnotationId(result.getAnnotation());
      Answer tempAnswer = answerService.getAnswerById(result.getAnswerId());
      tempdto.setAnswerText(tempAnswer.getText());
      tempdto.setAnswerValue(result.getValue());
      tempdto.setPatientId(tempanno.getSubjectId());
      tempdto.setCohortName(study.getCohortDefinition().getName());
      tempdto.setCohortId( study.getCohortDefinition().getId());
      tempdto.setDataSourceKey(source.getSourceKey());
      tempdto.setCohortSampleName(study.getCohortSample().getName());
      tempdto.setQuestionSetName(study.getQuestionSet().getName());
      tempdto.setCaseStatus(myQuestion.getCaseQuestion());
      tempdto.setQuestionText(myQuestion.getText());
      superList.add(tempdto);
    }
    return superList;
  }
}
