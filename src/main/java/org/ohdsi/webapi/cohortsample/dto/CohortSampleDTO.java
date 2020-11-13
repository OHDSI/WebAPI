package org.ohdsi.webapi.cohortsample.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.ohdsi.webapi.user.dto.UserDTO;

import java.util.Date;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CohortSampleDTO {
	/** Cohort sample ID. */
	private int id;
	/** Cohort sample name. */
	private String name;
	/**
	 * Actual sample size. This may be different from the size specified by the user if not enough
	 * persons could be found matching the criteria.
	 */
	private int size;

	/**
	 * Date that the sample was created.
	 */
	private Date createdDate;

	/**
	 * User that created the sample. If no login system is used, this is null.
	 */
	private UserDTO createdBy;

	/**
	 * Cohort definition ID that was sampled.
	 */
	private Integer cohortDefinitionId;
	/**
	 * Source ID that was sampled.
	 */
	private Integer sourceId;

	/**
	 * Age criteria used to create the sample.
	 */
	private SampleParametersDTO.AgeDTO age;

	/**
	 * Gender criteria used to create the sample.
	 */
	private SampleParametersDTO.GenderDTO gender;

	/**
	 * Actually sampled elements.
	 */
	private List<SampleElementDTO> elements;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	public UserDTO getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(UserDTO createdBy) {
		this.createdBy = createdBy;
	}

	public Integer getCohortDefinitionId() {
		return cohortDefinitionId;
	}

	public void setCohortDefinitionId(Integer cohortDefinitionId) {
		this.cohortDefinitionId = cohortDefinitionId;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public List<SampleElementDTO> getElements() {
		return elements;
	}

	public void setElements(List<SampleElementDTO> elements) {
		this.elements = elements;
	}

	public SampleParametersDTO.AgeDTO getAge() {
		return age;
	}

	public void setAge(SampleParametersDTO.AgeDTO age) {
		this.age = age;
	}

	public SampleParametersDTO.GenderDTO getGender() {
		return gender;
	}

	public void setGender(SampleParametersDTO.GenderDTO gender) {
		this.gender = gender;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
