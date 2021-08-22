package org.ohdsi.webapi.annotation.annotation;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnnotationService {

  @Autowired
  private AnnotationRepository annotationRepository;

  public List<Annotation> getAnnotations() {
    List<Annotation> annotations = new ArrayList();
    annotationRepository.findAll().forEach(annotations::add);
    return annotations;
  }

  public Annotation getAnnotationsByAnnotationId(int annotationId) {
    Annotation result = annotationRepository.findByAnnotationId(annotationId);
    return result;
  }

  public Annotation getAnnotationsByAnnotationId(Annotation annotation) {
    Annotation result = annotationRepository.findByAnnotationId(annotation.getId());
    return result;
  }

  public List<Annotation> getAnnotationByCohortSampleIdAndBySubjectIdAndByQuestionSetId(int cohortSampleId, int subjectId, int questionSetId) {
    System.out.printf("cohortSampleId %d\n",cohortSampleId);
    System.out.printf("subjectId %d\n",subjectId);
    System.out.printf("questionSetId %d\n",questionSetId);

    List<Annotation> annotations = new ArrayList();
    annotationRepository.findOneByCohortSampleIdAndSubjectIdAndQuestionSetId(cohortSampleId, subjectId, questionSetId)
    .forEach(annotations::add);
    return annotations;
  }

  public List<Annotation> getAnnotationByCohortSampleIdAndByQuestionSetId(int cohortSampleId, int setId) {
//      this becomes getByStudyId
//      checking if there is a study with the sample and question set that has already been created
    List<Annotation> annotations = new ArrayList();
    annotationRepository.findByCohortSampleIdAndQuestionSetId(cohortSampleId, setId)
    .forEach(annotations::add);
    return annotations;
  }

  public Annotation addAnnotation(Annotation annotation) {
    return annotationRepository.save(annotation);
  }

  public List<Annotation> getAnnotationsByCohortSampleId(int cohortSampleId) {
    return annotationRepository.findByCohortSampleId(cohortSampleId);
  }
  public List<Annotation> getAnnotationsByQuestionSetId(int questionSetId) {
    return annotationRepository.findByQuestionSetId(questionSetId);
  }
}
