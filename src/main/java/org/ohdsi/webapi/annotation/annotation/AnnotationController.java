package org.ohdsi.webapi.annotation.annotation;

import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import javax.ws.rs.PathParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.ohdsi.webapi.annotation.annotation.AnnotationService;
import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.annotation.set.QuestionSetRepository;
import org.ohdsi.webapi.annotation.set.QuestionSet;
import org.ohdsi.webapi.annotation.result.Result;



@Path("annotations")
@Component
public class AnnotationController {

  @Autowired
  private AnnotationService annotationService;

  @Autowired
  private QuestionSetRepository questionSetRepository;

  @GET
  @Path("/")
  @Produces(MediaType.APPLICATION_JSON)
  public List<Annotation> getAnnotations(
    @QueryParam("cohortId") final Long cohortId,
    @QueryParam("subjectId") final Long subjectId,
    @QueryParam("setId") final Long setId
  ) {


    if (cohortId != null && subjectId != null && setId != null) {
        return annotationService.getAnnotationByCohortIdAndBySubjectIdAndBySetId(cohortId, subjectId, setId);
    }

    return annotationService.getAnnotations();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public void addAnnotation(AnnotationDto annotationDto) {

    Annotation annotation = new Annotation();
    annotation.setId(annotationDto.getId());
    annotation.setSubjectId(annotationDto.getSubjectId());
    annotation.setCohortSampleId(annotationDto.getCohortSampleId());
    QuestionSet set = questionSetRepository.findById(annotationDto.getSetId());
    annotation.setSet(set);

    annotationDto.getResults().forEach((resultDto) -> {
      Result result = new Result();
      result.setQuestionId(resultDto.getQuestionId());
      result.setAnswerId(resultDto.getAnswerId());
      result.setSetId(set.getId());
      result.setSubjectId(annotationDto.getSubjectId());
      result.setValue(resultDto.getValue());
      result.setType(resultDto.getType());
      result.setSampleName(annotationDto.getSampleName());
      annotation.addToResults(result);
    });

    annotationService.addAnnotation(annotation, annotationDto.getSampleName());
  }

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
}
