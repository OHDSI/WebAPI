package org.ohdsi.webapi.annotation.annotation;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.ohdsi.webapi.annotation.annotation.AnnotationRepository;
import org.ohdsi.webapi.annotation.set.QuestionSet;
import org.ohdsi.webapi.annotation.question.Question;
import org.ohdsi.webapi.annotation.result.Result;
import org.ohdsi.webapi.annotation.result.ResultRepository;
import org.ohdsi.webapi.annotation.set.QuestionSetRepository;
// import org.aspectj.weaver.patterns.TypePatternQuestions.Question;
import org.ohdsi.webapi.annotation.annotation.Annotation;
import org.ohdsi.webapi.cohortsample.CohortSampleRepository;
import org.ohdsi.webapi.cohortsample.CohortSample;


@Service
public class AnnotationService {

  @Autowired
  private AnnotationRepository annotationRepository;

  @Autowired
  private CohortSampleRepository cohortSampleRepository;

  @Autowired
  private QuestionSetRepository questionSetRepository;

  @Autowired
  private ResultRepository resultRepository;

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
    List<Annotation> annotations = new ArrayList();
    annotationRepository.findByCohortSampleIdAndQuestionSetId(cohortSampleId, setId)
    .forEach(annotations::add);
    return annotations;
  }

  public Annotation addAnnotation(Annotation annotation, String name) {
//    cohortSampleRepository.annotated(name, annotation.getSubjectId(), annotation.getCohortId());
//    TODO implement above function and getSampleMembership
    return annotationRepository.save(annotation);
  }

  public List<Object[]> getAnnotationCSVData(int cohortID, String source, String sampleName) {
      List<Object[]> results = new ArrayList<Object[]>();
//      List<Long> samples = cohortSampleRepository.getSampleMembership(sampleName, cohortID);
      List<Integer> samples = new ArrayList<Integer>();
      Set<QuestionSet> questionSet = questionSetRepository.findByCohortId(cohortID);
      List<QuestionSet> qs = new ArrayList<QuestionSet>(questionSet);
      List<Integer> qIDs = new ArrayList<Integer>();

      for (Question q: qs.get(0).getQuestions()) {
        qIDs.add(q.getId());
      }

      Object[] metaData = new Object[3];
      metaData[0] = source;
      metaData[1] = cohortID;
      metaData[2] = questionSet;

      results.add(metaData);


      for (int i = 0; i<samples.size(); i++) {
        Object[] row = new Object[2];

        row[0] = samples.get(i);
        List<Object> answers = new ArrayList<>();
        for (int j = 0; j < qIDs.size(); j++) {
//            TODO fix if necessary
//          answers.add(resultRepository.getAnswers(sampleName, qIDs.get(j), samples.get(i)));
        }

        row[1] = answers;


        results.add(row);
      }

    return results;
  }

    public List<Annotation> getAnnotationsByCohortSampleId(int cohortSampleId) {
      return annotationRepository.findByCohortSampleId(cohortSampleId);
    }
    public List<Annotation> getAnnotationsByQuestionSetId(int questionSetId) {
        return annotationRepository.findByQuestionSetId(questionSetId);
    }
}
