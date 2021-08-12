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
import org.ohdsi.webapi.annotation.study.Study;
import org.ohdsi.webapi.annotation.study.StudyService;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortsample.dto.CohortSampleDTO;
import org.ohdsi.webapi.service.CohortSampleService;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.annotation.result.ResultService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;
import java.util.stream.Collectors;

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

  @Autowired
  private StudyService studyService;

  @Autowired
  private SourceService sourceService;

  @GET
  @Path("/{annotationID}")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Result> getResults(@PathParam("annotationID") String annotationID) {
    int annotationIdInt=Integer.parseInt(annotationID);
    ArrayList al = new ArrayList();
    Annotation ourAnno =annotationService.getAnnotationsByAnnotationId(annotationIdInt);
    return resultService.getResultsByAnnotationID(annotationIdInt);
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
//    May want to replicate this but with an additional parameter-
//    this would be (quesetionSetID + CohortSampleId) or AnnotationStudyId (aka the same thing)

    Study study = null;
    if(studyId!=0){
      study=studyService.getStudyById(studyId);
    }
    else if (questionSetId!=0 && cohortSampleId!=0){
      study=studyService.getStudyByQuestionSetIdAndSampleId(questionSetId,studyId);
    }
    else{
      return null;
    }
    List<Result> resultlist=resultService.getResultsByStudy(study);
    List<SuperResultDto> superList = new ArrayList();
//    Study finalStudy = study;
    Source source = sourceService.findBySourceId(study.getCohortSample().getSourceId());
//    List<SuperResultDto> fastlist = resultlist.stream().map(r -> new SuperResultDto(r, finalStudy,source)).collect(Collectors.toList());
//    List<SuperResultDto> fastlist = resultlist.stream().map(r -> {
//      Question myQuestion = questionService.getQuestionByQuestionId(r.getQuestionId());
//      Annotation tempanno = annotationService.getAnnotationsByAnnotationId(r.getAnnotation());
//      return new SuperResultDto(r, finalStudy,source,myQuestion,tempanno);
//    } ).collect(Collectors.toList());
    for (Result result : resultlist){
//      things we currently query for; Annotation Cohort Sample, Cohort Definition, QuestionSets
      Question myQuestion = questionService.getQuestionByQuestionId(result.getQuestionId());
      SuperResultDto tempdto = new SuperResultDto(result);
      Annotation tempanno = annotationService.getAnnotationsByAnnotationId(result.getAnnotation());
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
//    return fastlist;
  }

  @Path("/{cohortDefinitionId}/{sourceKey}")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void createResult(){

  }
}
