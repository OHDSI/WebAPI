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

    @Column(name = "concept_set_name")
    private String conceptSetName;

		@Column(name = "negative_control")
		private int negativeControl;

    @Column(name = "concept_id")
    private int conceptId;

    @Column(name = "concept_name")
    private String conceptName;

    @Column(name = "domain_id")
    private String domainId;

    @Column(name = "sort_order")
    private Long sortOrder;

    @Column(name = "descendant_pmid_cnt")
    private Long descendantPmidCount;

    @Column(name = "exact_pmid_cnt")
    private Long exactPmidCount ;

    @Column(name = "parent_pmid_cnt")
    private Long parentPmidCount ;

    @Column(name = "ancestor_pmid_cnt")
    private Long ancestorPmidCount;

    @Column(name = "ind_ci")
    private int indCi;
		
		@Column(name = "too_broad")
		private int tooBroad;

    @Column(name = "drug_induced")
    private int drugInduced;
		
    @Column(name = "pregnancy")
    private int pregnancy;

    @Column(name = "descendant_splicer_cnt")
    private Long descendantSplicerCount;
		
    @Column(name = "exact_splicer_cnt")
    private Long exactSplicerCount;

		@Column(name = "parent_splicer_cnt")
    private Long parentSplicerCount;
    
		@Column(name = "ancestor_splicer_cnt")
    private Long ancestorSplicerCount;

    @Column(name = "descendant_faers_cnt")
    private Long descendantFaersCount;
		
    @Column(name = "exact_faers_cnt")
    private Long exactFaersCount;
		
    @Column(name = "parent_faers_cnt")
    private Long parentFaersCount;
		
    @Column(name = "ancestor_faers_cnt")
    private Long ancestorFaersCount;

    @Column(name = "user_excluded")
    private int userExcluded;

    @Column(name = "user_included")
    private int userIncluded;

    @Column(name = "optimized_out")
    private int optimizedOut;

    @Column(name = "not_prevalent")
    private int notPrevalent;
		
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

	/**
	 * @return the conceptSetName
	 */
	public String getConceptSetName() {
		return conceptSetName;
	}

	/**
	 * @param conceptSetName the conceptSetName to set
	 */
	public void setConceptSetName(String conceptSetName) {
		this.conceptSetName = conceptSetName;
	}

	/**
	 * @return the negativeControl
	 */
	public int getNegativeControl() {
		return negativeControl;
	}

	/**
	 * @param negativeControl the negativeControl to set
	 */
	public void setNegativeControl(int negativeControl) {
		this.negativeControl = negativeControl;
	}

	/**
	 * @return the conceptId
	 */
	public int getConceptId() {
		return conceptId;
	}

	/**
	 * @param conceptId the conceptId to set
	 */
	public void setConceptId(int conceptId) {
		this.conceptId = conceptId;
	}

	/**
	 * @return the conceptName
	 */
	public String getConceptName() {
		return conceptName;
	}

	/**
	 * @param conceptName the conceptName to set
	 */
	public void setConceptName(String conceptName) {
		this.conceptName = conceptName;
	}

	/**
	 * @return the domainId
	 */
	public String getDomainId() {
		return domainId;
	}

	/**
	 * @param domainId the domainId to set
	 */
	public void setDomainId(String domainId) {
		this.domainId = domainId;
	}

	/**
	 * @return the sortOrder
	 */
	public Long getSortOrder() {
		return sortOrder;
	}

	/**
	 * @param sortOrder the sortOrder to set
	 */
	public void setSortOrder(Long sortOrder) {
		this.sortOrder = sortOrder;
	}

	/**
	 * @return the descendantPmidCount
	 */
	public Long getDescendantPmidCount() {
		return descendantPmidCount;
	}

	/**
	 * @param descendantPmidCount the descendantPmidCount to set
	 */
	public void setDescendantPmidCount(Long descendantPmidCount) {
		this.descendantPmidCount = descendantPmidCount;
	}

	/**
	 * @return the exactPmidCount
	 */
	public Long getExactPmidCount() {
		return exactPmidCount;
	}

	/**
	 * @param exactPmidCount the exactPmidCount to set
	 */
	public void setExactPmidCount(Long exactPmidCount) {
		this.exactPmidCount = exactPmidCount;
	}

	/**
	 * @return the parentPmidCount
	 */
	public Long getParentPmidCount() {
		return parentPmidCount;
	}

	/**
	 * @param parentPmidCount the parentPmidCount to set
	 */
	public void setParentPmidCount(Long parentPmidCount) {
		this.parentPmidCount = parentPmidCount;
	}

	/**
	 * @return the ancestorPmidCount
	 */
	public Long getAncestorPmidCount() {
		return ancestorPmidCount;
	}

	/**
	 * @param ancestorPmidCount the ancestorPmidCount to set
	 */
	public void setAncestorPmidCount(Long ancestorPmidCount) {
		this.ancestorPmidCount = ancestorPmidCount;
	}

	/**
	 * @return the indCi
	 */
	public int getIndCi() {
		return indCi;
	}

	/**
	 * @param indCi the indCi to set
	 */
	public void setIndCi(int indCi) {
		this.indCi = indCi;
	}

	/**
	 * @return the drugInduced
	 */
	public int getDrugInduced() {
		return drugInduced;
	}

	/**
	 * @param drugInduced the drugInduced to set
	 */
	public void setDrugInduced(int drugInduced) {
		this.drugInduced = drugInduced;
	}

	/**
	 * @return the descendantSplicerCount
	 */
	public Long getDescendantSplicerCount() {
		return descendantSplicerCount;
	}

	/**
	 * @param descendantSplicerCount the descendantSplicerCount to set
	 */
	public void setDescendantSplicerCount(Long descendantSplicerCount) {
		this.descendantSplicerCount = descendantSplicerCount;
	}

	/**
	 * @return the descendantFaersCount
	 */
	public Long getDescendantFaersCount() {
		return descendantFaersCount;
	}

	/**
	 * @param descendantFaersCount the descendantFaersCount to set
	 */
	public void setDescendantFaersCount(Long descendantFaersCount) {
		this.descendantFaersCount = descendantFaersCount;
	}

	/**
	 * @return the userExcluded
	 */
	public int getUserExcluded() {
		return userExcluded;
	}

	/**
	 * @param userExcluded the userExcluded to set
	 */
	public void setUserExcluded(int userExcluded) {
		this.userExcluded = userExcluded;
	}

	/**
	 * @return the userIncluded
	 */
	public int getUserIncluded() {
		return userIncluded;
	}

	/**
	 * @param userIncluded the userIncluded to set
	 */
	public void setUserIncluded(int userIncluded) {
		this.userIncluded = userIncluded;
	}

	/**
	 * @return the tooBroad
	 */
	public int getTooBroad() {
		return tooBroad;
	}

	/**
	 * @param tooBroad the tooBroad to set
	 */
	public void setTooBroad(int tooBroad) {
		this.tooBroad = tooBroad;
	}

	/**
	 * @return the pregnancy
	 */
	public int getPregnancy() {
		return pregnancy;
	}

	/**
	 * @param pregnancy the pregnancy to set
	 */
	public void setPregnancy(int pregnancy) {
		this.pregnancy = pregnancy;
	}

	/**
	 * @return the exactSplicerCount
	 */
	public Long getExactSplicerCount() {
		return exactSplicerCount;
	}

	/**
	 * @param exactSplicerCount the exactSplicerCount to set
	 */
	public void setExactSplicerCount(Long exactSplicerCount) {
		this.exactSplicerCount = exactSplicerCount;
	}

	/**
	 * @return the parentSplicerCount
	 */
	public Long getParentSplicerCount() {
		return parentSplicerCount;
	}

	/**
	 * @param parentSplicerCount the parentSplicerCount to set
	 */
	public void setParentSplicerCount(Long parentSplicerCount) {
		this.parentSplicerCount = parentSplicerCount;
	}

	/**
	 * @return the ancestorSplicerCount
	 */
	public Long getAncestorSplicerCount() {
		return ancestorSplicerCount;
	}

	/**
	 * @param ancestorSplicerCount the ancestorSplicerCount to set
	 */
	public void setAncestorSplicerCount(Long ancestorSplicerCount) {
		this.ancestorSplicerCount = ancestorSplicerCount;
	}

	/**
	 * @return the exactFaersCount
	 */
	public Long getExactFaersCount() {
		return exactFaersCount;
	}

	/**
	 * @param exactFaersCount the exactFaersCount to set
	 */
	public void setExactFaersCount(Long exactFaersCount) {
		this.exactFaersCount = exactFaersCount;
	}

	/**
	 * @return the parentFaersCount
	 */
	public Long getParentFaersCount() {
		return parentFaersCount;
	}

	/**
	 * @param parentFaersCount the parentFaersCount to set
	 */
	public void setParentFaersCount(Long parentFaersCount) {
		this.parentFaersCount = parentFaersCount;
	}

	/**
	 * @return the ancestorFaersCount
	 */
	public Long getAncestorFaersCount() {
		return ancestorFaersCount;
	}

	/**
	 * @param ancestorFaersCount the ancestorFaersCount to set
	 */
	public void setAncestorFaersCount(Long ancestorFaersCount) {
		this.ancestorFaersCount = ancestorFaersCount;
	}

	/**
	 * @return the optimizedOut
	 */
	public int getOptimizedOut() {
		return optimizedOut;
	}

	/**
	 * @param optimizedOut the optimizedOut to set
	 */
	public void setOptimizedOut(int optimizedOut) {
		this.optimizedOut = optimizedOut;
	}

	/**
	 * @return the notPrevalent
	 */
	public int getNotPrevalent() {
		return notPrevalent;
	}

	/**
	 * @param notPrevalent the notPrevalent to set
	 */
	public void setNotPrevalent(int notPrevalent) {
		this.notPrevalent = notPrevalent;
	}


}
