package org.ohdsi.webapi.conceptset.annotation;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.model.CommonEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "concept_set_annotation")
public class ConceptSetAnnotation extends CommonEntity<Integer> implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GenericGenerator(
            name = "concept_set_annotation_generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "concept_set_annotation_sequence"),
                    @Parameter(name = "increment_size", value = "1")
            }
    )
    @GeneratedValue(generator = "concept_set_annotation_generator")
    @Column(name = "concept_set_annotation_id")
    private Integer id;

    @Column(name = "concept_set_id", nullable = false)
    private Integer conceptSetId;

    @Column(name = "concept_id")
    private Integer conceptId;

    @Column(name = "annotation_details")
    private String annotationDetails;

    @Column(name = "vocabulary_version")
    private String vocabularyVersion;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getConceptSetId() {
        return conceptSetId;
    }

    public void setConceptSetId(Integer conceptSetId) {
        this.conceptSetId = conceptSetId;
    }

    public Integer getConceptId() {
        return conceptId;
    }

    public void setConceptId(Integer conceptId) {
        this.conceptId = conceptId;
    }

    public String getAnnotationDetails() {
        return annotationDetails;
    }

    public void setAnnotationDetails(String annotationDetails) {
        this.annotationDetails = annotationDetails;
    }

    public String getVocabularyVersion() {
        return vocabularyVersion;
    }

    public void setVocabularyVersion(String vocabularyVersion) {
        this.vocabularyVersion = vocabularyVersion;
    }
}
