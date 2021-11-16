package org.ohdsi.webapi.annotation.annotation;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import org.json.JSONObject;
import org.json.JSONArray;
import org.ohdsi.webapi.annotation.result.ResultService;
import org.ohdsi.webapi.annotation.study.Study;
import org.ohdsi.webapi.annotation.study.StudyService;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortdefinition.CohortDefinitionRepository;
import org.ohdsi.webapi.cohortsample.CohortSample;
import org.ohdsi.webapi.cohortsample.CohortSampleRepository;
import org.ohdsi.webapi.cohortsample.CohortSamplingService;
import org.ohdsi.webapi.cohortsample.dto.SampleElementDTO;
import org.ohdsi.webapi.source.Source;
import org.ohdsi.webapi.source.SourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.annotation.set.QuestionSetRepository;
import org.ohdsi.webapi.annotation.set.QuestionSet;
import org.springframework.web.bind.annotation.RequestBody;


@Path("annotation")
@Component
public class AnnotationController {

  @Autowired
  private StudyService studyService;

  @Autowired
  private ResultService resultService;

  @Autowired
  private CohortDefinitionRepository cohortDefinitionRepository;

  @Autowired
  private CohortSamplingService cohortSamplingService;

  @Autowired
  private CohortSampleRepository sampleRepository;

  @Autowired
  private AnnotationService annotationService;

  @Autowired
  private QuestionSetRepository questionSetRepository;

  @Autowired
  private SourceService sourceService;

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<AnnotationSummary> getAnnotations(
    @QueryParam("cohortSampleId") final int cohortSampleId,
    @QueryParam("subjectId") final int subjectId,
    @QueryParam("setId") final int setId
  ) {
    List<Annotation> returnAnnotations;
    returnAnnotations = getFullAnnotations(cohortSampleId,subjectId,setId);
    List<AnnotationSummary> summaries = new ArrayList();
    for(Annotation singleAnno : returnAnnotations){
//      can we see about doing this in a more performant manner?
      AnnotationSummary tempAnnoSummary=new AnnotationSummary(singleAnno);
      summaries.add(tempAnnoSummary);
    }
    return summaries;
  }

  @GET
  @Path("/fullquestion")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Annotation> getFullAnnotations(
          @QueryParam("cohortSampleId") final int cohortSampleId,
          @QueryParam("subjectId") final int subjectId,
          @QueryParam("setId") final int setId
          ) {
    List<Annotation> returnAnnotations=null;
    if (cohortSampleId != 0 && subjectId != 0 && setId != 0) {
      System.out.println("made it into the search function");
      returnAnnotations= annotationService.getAnnotationByCohortSampleIdAndBySubjectIdAndByQuestionSetId(cohortSampleId, subjectId, setId);
    }
    else if (cohortSampleId !=0 && setId!=0){
      returnAnnotations= annotationService.getAnnotationByCohortSampleIdAndByQuestionSetId(cohortSampleId,setId);
    }
    else if(cohortSampleId!=0){
      returnAnnotations= annotationService.getAnnotationsByCohortSampleId(cohortSampleId);
    }
    else if(setId !=0){
      returnAnnotations= annotationService.getAnnotationsByQuestionSetId(setId);
    }
    else{
      returnAnnotations=annotationService.getAnnotations();
    }
    return returnAnnotations;
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void addResult(@RequestBody String payload) {
    System.out.println(payload);
    JSONObject jsonpayload = new JSONObject(payload);
    System.out.println(jsonpayload);
    System.out.println(jsonpayload.get("results"));
    System.out.printf("cohortId: %s\n",jsonpayload.get("cohortId").toString());
    System.out.printf("cohortSampleId: %s\n",jsonpayload.get("cohortSampleId").toString());
    System.out.printf("subjectId: %s\n",jsonpayload.get("subjectId").toString());
    System.out.printf("setId: %s\n",jsonpayload.get("setId").toString());
    Annotation tempAnnotation = annotationService.getAnnotationsByAnnotationId(jsonpayload.getInt("id"));
    System.out.printf("annotationID:%d\n",tempAnnotation.getId());
    JSONArray array = jsonpayload.getJSONArray("results");
    Study study = studyService.getStudyByQuestionSetIdAndSampleId(jsonpayload.getInt("setId"),jsonpayload.getInt("cohortSampleId") );
    resultService.insertResults(tempAnnotation,array,study);
  }

  @POST
  @Path("/sample")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void addAnnotation(@RequestBody String payload) {
    JSONObject jsonpayload = new JSONObject(payload);
    int cohortSampleId = Integer.parseInt(jsonpayload.get("sampleId").toString());
    CohortSample cohortSample =sampleRepository.findById(cohortSampleId);
    List<SampleElementDTO> temp = cohortSamplingService.getSample(cohortSampleId, false).getElements();
    Study study = new Study();
    int questionSetId = Integer.parseInt(jsonpayload.get("annotationSetId").toString());
    QuestionSet questionSet = questionSetRepository.findById(questionSetId);
    study.setQuestionSet(questionSetRepository.findById(questionSetId));
    study.setCohortSample(cohortSample);
    CohortDefinition cohortDefinition= cohortDefinitionRepository.findOneWithDetail(cohortSample.getCohortDefinitionId());
    study.setCohortDefinition(cohortDefinition);
    Source source = sourceService.findBySourceKey(jsonpayload.get("sourceKey").toString());
    study.setSource(source);
    studyService.addStudy(study);
    for (SampleElementDTO element : temp){
      System.out.println("element"+element);
      System.out.println("element GetPersonID"+element.getPersonId());
      Annotation annotation = new Annotation();
      annotation.setSubjectId(Integer.parseInt(element.getPersonId()));
      annotation.setCohortSampleId(cohortSampleId);
      annotation.setQuestionSet(questionSet);
      annotationService.addAnnotation(annotation);
    }
  }

  @GET
  @Path("/{annotationID}")
  @Produces(MediaType.APPLICATION_JSON)
  public Annotation getResults(@PathParam("annotationID") String annotationID) {
    int annotationIdInt=Integer.parseInt(annotationID);
    Annotation ourAnno =annotationService.getAnnotationsByAnnotationId(annotationIdInt);
    return ourAnno;
  }
}
