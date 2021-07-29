package org.ohdsi.webapi.annotation.annotation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.ohdsi.webapi.cohortsample.CohortSample;
import org.ohdsi.webapi.cohortsample.CohortSampleRepository;
import org.ohdsi.webapi.cohortsample.CohortSamplingService;
import org.ohdsi.webapi.cohortsample.dto.SampleElementDTO;
import org.ohdsi.webapi.source.Source;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.annotation.annotation.AnnotationService;
import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.annotation.set.QuestionSetRepository;
import org.ohdsi.webapi.annotation.set.QuestionSet;
import org.ohdsi.webapi.annotation.result.Result;
import org.springframework.web.bind.annotation.RequestBody;
import org.ohdsi.webapi.service.AbstractDaoService;


@Path("annotations")
@Component
public class AnnotationController {

  @Autowired
  private ResultService resultService;

  @Autowired
  public CohortSamplingService cohortSamplingService;

  @Autowired
  private AnnotationService annotationService;

  @Autowired
  private QuestionSetRepository questionSetRepository;

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
//      TODO see about doing this in a more performant manner?
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
    System.out.printf("cohortSampleId: %s\n",jsonpayload.get("sampleName").toString());
    System.out.printf("subjectId: %s\n",jsonpayload.get("subjectId").toString());
    System.out.printf("setId: %s\n",jsonpayload.get("setId").toString());
//    TODO: change this to use annotationService.getAnnotationsByAnnotationId potentially
    Annotation tempAnnotation = annotationService.getAnnotationByCohortSampleIdAndBySubjectIdAndByQuestionSetId(Integer.parseInt(jsonpayload.get("sampleName").toString())
            ,Integer.parseInt(jsonpayload.get("subjectId").toString()),Integer.parseInt(jsonpayload.get("setId").toString())).get(0);
    System.out.printf("annotationID:%d\n",tempAnnotation.getId());
    JSONArray array = jsonpayload.getJSONArray("results");

    resultService.insertResults(tempAnnotation,array);
//    for(int i=0; i < array.length(); i++){
//      JSONObject object = array.getJSONObject(i);
//      Result result = new Result();
//      result.setQuestionId(Long.parseLong(object.get("questionId").toString()));
//      result.setAnswerId(Long.parseLong(object.get("answerId").toString()));
//      result.setValue(object.get("value").toString());
//      result.setType(object.get("type").toString());
//      result.setAnnotation(tempAnnotation);
//      resultService.save(result);
//    }
  }

  @POST
  @Path("/sample")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void addAnnotation(AnnotationDto annotationDto) {
    System.out.println("annotationDto: "+annotationDto);
    System.out.println("annotationDtoGetId: "+annotationDto.getId());
    System.out.println("annotationDtoGesampleId"+annotationDto.getsampleId());
    System.out.println("annotationDtoGetAnnotationSetID"+annotationDto.getannotationSetId());
    List<SampleElementDTO> temp = cohortSamplingService.getSample(annotationDto.getsampleId(), false).getElements();
    System.out.println("SampleElementDTO"+temp);
    for (SampleElementDTO element : temp){
      System.out.println("element"+element);
      System.out.println("element GetPersonID"+element.getPersonId());
      Annotation annotation = new Annotation();
      annotation.setId(annotationDto.getId());
      annotation.setSubjectId(Integer.parseInt(element.getPersonId()));
      annotation.setCohortSampleId(annotationDto.getsampleId());
      QuestionSet questionSet = questionSetRepository.findById(annotationDto.getannotationSetId());
      annotation.setQuestionSet(questionSet);
      annotationService.addAnnotation(annotation, annotationDto.getSampleName());
    }
  }

  @GET
  @Path("/getsets")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Annotation> getSets(
    @QueryParam("cohortSampleId") final Long cohortSampleId,
    @QueryParam("subjectId") final Long subjectId,
    @QueryParam("setId") final Long setId
  ) {
    return annotationService.getAnnotations();
  }

//just for testing
  @Path("/csvData")
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public List<Object[]> getAnnotationCSV(
      @QueryParam("cohortID") Long cohortID,
      @QueryParam("sourceKey") String sourceKey,
      @QueryParam("sampleName") String sampleName
      ) {

      if (sampleName.indexOf("_") != -1) {
          sampleName = sampleName.replaceAll("_", " ");
      }

      if (cohortID == null || sourceKey == null || sampleName == null) {
        return null;
      }

      return annotationService.getAnnotationCSVData(cohortID, sourceKey, sampleName);

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

