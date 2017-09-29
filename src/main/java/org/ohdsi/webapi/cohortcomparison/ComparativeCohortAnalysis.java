/*
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ohdsi.webapi.cohortcomparison;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.ohdsi.webapi.cohortdefinition.CohortGenerationInfo;

/**
 * @author Frank DeFalco <fdefalco@ohdsi.org>
 */
@Entity(name = "ComparativeCohortAnalysis")
@Table(name = "cca")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "analysisId", scope=ComparativeCohortAnalysis.class)
public class ComparativeCohortAnalysis implements Serializable {

    @Id
    @GeneratedValue
    @Column(name = "cca_id")
    private Integer analysisId;

    @Column(name = "name")
    private String name;
		
    @Column(name = "created_date")
    private Date createdDate;
		
    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "modified_date")
    private Date modifiedDate;

		@Column(name = "modified_by")
    private String modifiedBy;
		
		@OneToMany(fetch= FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true, mappedBy = "comparativeCohortAnalysis")
		private Set<TargetComparatorDefinition> targetComparatorList;
		
		@OneToMany(fetch= FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true, mappedBy = "comparativeCohortAnalysis")
		private Set<OutcomeDefinition> outcomeList;

		@OneToMany(fetch= FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true, mappedBy = "comparativeCohortAnalysis")
		private Set<ComparativeCohortAnalysisDefinition> analysisList;

    public Integer getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(Integer id) {
        this.analysisId = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

	/**
	 * @return the outcomeList
	 */
	public Set<OutcomeDefinition> getOutcomeList() {
		return outcomeList;
	}

	/**
	 * @param outcomeList the outcomeList to set
	 */
	public void setOutcomeList(Set<OutcomeDefinition> outcomeList) {
		this.outcomeList = outcomeList;
	}
	
	/**
	 * @return the targetComparatorList
	 */
	public Set<TargetComparatorDefinition> getTargetComparatorList() {
		return targetComparatorList;
	}

	/**
	 * @param targetComparatorList the targetComparatorList to set
	 */
	public void setTargetComparatorList(Set<TargetComparatorDefinition> targetComparatorList) {
		this.targetComparatorList = targetComparatorList;
	}

	/**
	 * @return the analysisList
	 */
	public Set<ComparativeCohortAnalysisDefinition> getAnalysisList() {
		return analysisList;
	}

	/**
	 * @param analysisList the analysisList to set
	 */
	public void setAnalysisList(Set<ComparativeCohortAnalysisDefinition> analysisList) {
		this.analysisList = analysisList;
	}

	/**
	 * @return the createdBy
	 */
	public String getCreatedBy() {
		return createdBy;
	}

	/**
	 * @param createdBy the createdBy to set
	 */
	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * @return the modifiedBy
	 */
	public String getModifiedBy() {
		return modifiedBy;
	}

	/**
	 * @param modifiedBy the modifiedBy to set
	 */
	public void setModifiedBy(String modifiedBy) {
		this.modifiedBy = modifiedBy;
	}

}
