package org.ohdsi.webapi.cdmresults.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "cdm_cache")
public class CDMCacheEntity {
    @Id
    @SequenceGenerator(name = "cdm_cache_seq", sequenceName = "cdm_cache_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cdm_cache_seq")
    private Long id;

    @Column(name = "concept_id")
    private int conceptId;

    @Column(name = "source_id")
    private int sourceId;

    @Column(name = "record_count")
    private Long recordCount;

    @Column(name = "descendant_record_count")
    private Long descendantRecordCount;

    @Column(name = "person_count")
    private Long personCount;

    @Column(name = "descendant_person_count")
    private Long descendantPersonCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getConceptId() {
        return conceptId;
    }

    public void setConceptId(int conceptId) {
        this.conceptId = conceptId;
    }

    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    public Long getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(Long recordCount) {
        this.recordCount = recordCount;
    }

    public Long getDescendantRecordCount() {
        return descendantRecordCount;
    }

    public void setDescendantRecordCount(Long descendantRecordCount) {
        this.descendantRecordCount = descendantRecordCount;
    }

    public Long getPersonCount() {
        return personCount;
    }

    public void setPersonCount(Long personCount) {
        this.personCount = personCount;
    }

    public Long getDescendantPersonCount() {
        return descendantPersonCount;
    }

    public void setDescendantPersonCount(Long descendantPersonCount) {
        this.descendantPersonCount = descendantPersonCount;
    }
}
