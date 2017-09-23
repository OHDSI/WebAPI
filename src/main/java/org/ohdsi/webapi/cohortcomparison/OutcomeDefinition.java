/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ohdsi.webapi.cohortcomparison;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 *
 * @author asena5
 */
@Entity(name = "OutcomeDefinition")
@Table(name = "cca_o")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope=OutcomeDefinition.class)
public class OutcomeDefinition  implements Serializable {
	  @Id
		@Column(name = "id", insertable = false, updatable = false)
		@SequenceGenerator(name = "CCA_O_SEQUENCE_GENERATOR", sequenceName = "CCA_O_SEQUENCE", allocationSize = 1, initialValue = 1)
		@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "CCA_O_SEQUENCE_GENERATOR")
		private Integer id;
  
		@ManyToOne
		@JoinColumn(name="cca_id", referencedColumnName="cca_id")
		@JsonIgnore
    private ComparativeCohortAnalysis comparativeCohortAnalysis;

    @Column(name = "outcome_id")
    private Integer outcomeId;

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
	 * @return the outcomeId
	 */
	public Integer getOutcomeId() {
		return outcomeId;
	}

	/**
	 * @param outcomeId the outcomeId to set
	 */
	public void setOutcomeId(Integer outcomeId) {
		this.outcomeId = outcomeId;
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
