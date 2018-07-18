/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.evidence.negativecontrols;

import java.io.Serializable;
import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author asena5
 */
@Entity(name = "NegativeControlRecord")
@Table(name = "CONCEPT_SET_NEGATIVE_CONTROLS")
public class NegativeControlRecord implements Serializable {

    @Id
    @SequenceGenerator(name = "concept_set_negative_controls_seq", sequenceName = "negative_controls_sequence", allocationSize = 1)
    @GeneratedValue(generator = "concept_set_negative_controls_seq", strategy = GenerationType.SEQUENCE)
    @Access(AccessType.PROPERTY) 
    @Column(name = "id")
    private int id;
    
    @Column(name = "evidence_job_id")
    private Long evidenceJobId;
		
    @Column(name = "source_id")
    private int sourceId;
		
    @Column(name = "concept_set_id")
    private int conceptSetId;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the evidenceJobId
     */
    public Long getEvidenceJobId() {
        return evidenceJobId;
    }

    /**
     * @param evidenceJobId the evidenceJobId to set
     */
    public void setEvidenceJobId(Long evidenceJobId) {
        this.evidenceJobId = evidenceJobId;
    }

    /**
     * @return the sourceId
     */
    public int getSourceId() {
        return sourceId;
    }

    /**
     * @param sourceId the sourceId to set
     */
    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    /**
     * @return the conceptSetId
     */
    public int getConceptSetId() {
        return conceptSetId;
    }

    /**
     * @param conceptSetId the conceptSetId to set
     */
    public void setConceptSetId(int conceptSetId) {
        this.conceptSetId = conceptSetId;
    }
}
