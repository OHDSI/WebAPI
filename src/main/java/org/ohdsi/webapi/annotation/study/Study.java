package org.ohdsi.webapi.annotation.study;

import org.ohdsi.webapi.annotation.set.QuestionSet;
import org.ohdsi.webapi.cohortdefinition.CohortDefinition;
import org.ohdsi.webapi.cohortsample.CohortSample;
import org.ohdsi.webapi.source.Source;

import javax.persistence.*;

@Entity(name = "Study")
@Table(name = "annotation_study")
public class Study {
    @Id
    @GeneratedValue
    @Column(name = "study_id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "question_set_id")
    private QuestionSet questionSet;

    @ManyToOne
    @JoinColumn(name = "cohort_definition_id")
    private CohortDefinition cohortDefinition;

    @ManyToOne
    @JoinColumn(name = "cohort_sample_id")
    private CohortSample cohortSample;

    @ManyToOne
    @JoinColumn(name = "source_id")
    private Source source;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public QuestionSet getQuestionSet() {
        return questionSet;
    }

    public void setQuestionSet(QuestionSet questionSet) {
        this.questionSet = questionSet;
    }

    public CohortDefinition getCohortDefinition() {
        return cohortDefinition;
    }

    public void setCohortDefinition(CohortDefinition cohortDefinition) {
        this.cohortDefinition = cohortDefinition;
    }

    public CohortSample getCohortSample() {
        return cohortSample;
    }

    public void setCohortSample(CohortSample cohortSample) {
        this.cohortSample = cohortSample;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }
}
