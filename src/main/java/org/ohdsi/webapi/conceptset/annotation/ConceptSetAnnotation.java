package org.ohdsi.webapi.conceptset.annotation;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

@Entity(name = "ConceptSetAnnotation")
@Table(name = "concept_set_annotation")
public class ConceptSetAnnotation implements Serializable {
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

    @Column(name = "concept_set_version")
    private Integer conceptSetVersion;

    @Column(name = "copied_from_concept_set_ids")
    private String copiedFromConceptSetIds;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", updatable = false)
    private UserEntity createdBy;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modified_by_id")
    private UserEntity modifiedBy;
    @Column(name = "created_date", updatable = false)
    private Date createdDate;
    @Column(name = "modified_date")
    private Date modifiedDate;

    public UserEntity getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {

        this.createdBy = createdBy;
    }

    public UserEntity getModifiedBy() {

        return modifiedBy;
    }

    public void setModifiedBy(UserEntity modifiedBy) {

        this.modifiedBy = modifiedBy;
    }

    public Date getCreatedDate() {

        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {

        this.createdDate = createdDate;
    }

    public Date getModifiedDate() {

        return modifiedDate;
    }

    public void setModifiedDate(Date modifiedDate) {

        this.modifiedDate = modifiedDate;
    }

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

    public Integer getConceptSetVersion() {
        return conceptSetVersion;
    }

    public void setConceptSetVersion(Integer conceptSetVersion) {
        this.conceptSetVersion = conceptSetVersion;
    }

    public String getCopiedFromConceptSetIds() {
        return copiedFromConceptSetIds;
    }

    public void setCopiedFromConceptSetIds(String copiedFromConceptSetIds) {
        this.copiedFromConceptSetIds = copiedFromConceptSetIds;
    }
}
