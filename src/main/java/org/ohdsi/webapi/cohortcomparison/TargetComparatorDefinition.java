/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortcomparison;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author asena5
 */
@Entity(name = "TargetComparatorDefinition")
@Table(name = "cca_t_c")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope=TargetComparatorDefinition.class)
public class TargetComparatorDefinition implements Serializable {
	  @Id
		@Column(name = "id", insertable = false, updatable = false)
		@SequenceGenerator(name = "CCA_T_C_SEQUENCE_GENERATOR", sequenceName = "CCA_T_C_SEQUENCE", allocationSize = 1, initialValue = 1)
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CCA_T_C_SEQUENCE_GENERATOR")
		private Integer id;
		  
		@ManyToOne
		@JoinColumn(name="cca_id", referencedColumnName="cca_id")
		@JsonIgnore
    private ComparativeCohortAnalysis comparativeCohortAnalysis;

    @Column(name = "target_id")
    private Integer targetId;

    @Column(name = "comparator_id")
    private Integer comparatorId;
	
    @Column(name = "ps_exclusion_id")
    private int psExclusionId;

    @Column(name = "ps_inclusion_id")
    private int psInclusionId;
				
	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the targetId
	 */
	public Integer getTargetId() {
		return targetId;
	}

	/**
	 * @param targetId the targetId to set
	 */
	public void setTargetId(Integer targetId) {
		this.targetId = targetId;
	}

	/**
	 * @return the comparatorId
	 */
	public Integer getComparatorId() {
		return comparatorId;
	}

	/**
	 * @param comparatorId the comparatorId to set
	 */
	public void setComparatorId(Integer comparatorId) {
		this.comparatorId = comparatorId;
	}

	/**
	 * @return the psExclusionId
	 */
	public int getPsExclusionId() {
		return psExclusionId;
	}

	/**
	 * @param psExclusionId the psExclusionId to set
	 */
	public void setPsExclusionId(int psExclusionId) {
		this.psExclusionId = psExclusionId;
	}

	/**
	 * @return the psInclusionId
	 */
	public int getPsInclusionId() {
		return psInclusionId;
	}

	/**
	 * @param psInclusionId the psInclusionId to set
	 */
	public void setPsInclusionId(int psInclusionId) {
		this.psInclusionId = psInclusionId;
	}
	
	/**
	 * @return the comparativeCohortAnalysis
	 */
	@JsonIgnore
	public ComparativeCohortAnalysis getComparativeCohortAnalysis() {
		return comparativeCohortAnalysis;
	}

	/**
	 * @param comparativeCohortAnalysis the comparativeCohortAnalysis to set
	 */
	@JsonProperty
	public void setComparativeCohortAnalysis(ComparativeCohortAnalysis comparativeCohortAnalysis) {
		this.comparativeCohortAnalysis = comparativeCohortAnalysis;
	}
}
