package org.ohdsi.webapi.annotation.annotation;

import com.fasterxml.jackson.annotation.JsonAutoDetect;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class AnnotationSummary {
    private int id;
    private int subjectId;
    private int cohortSampleId;
    private int questionSetId;

    public AnnotationSummary(Annotation annotation) {
        this.id = annotation.getId();
        this.subjectId= annotation.getSubjectId();
        this.cohortSampleId= annotation.getCohortSampleId();
        this.questionSetId=annotation.getQuestionSet().getId();
    }
}
