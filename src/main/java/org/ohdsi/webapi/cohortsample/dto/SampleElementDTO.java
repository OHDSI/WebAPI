package org.ohdsi.webapi.cohortsample.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SampleElementDTO {
	/**
	 * Sample ID that this element belongs to. May be null if this object is part of a
	 * {@link CohortSampleDTO} object.
	 */
	private Integer sampleId;

	/**
	 * Rank of the object within the sample. This establishes order between elements.
	 */
	private int rank;

	/**
	 * Person ID of the element.
	 */
	private String personId;

	/**
	 * Gender ID of the person.
	 */
	private long genderConceptId;

	/**
	 * Age of the person.
	 */
	private int age;

	private Integer recordCount;

	public Integer getSampleId() {
		return sampleId;
	}

	public void setSampleId(Integer sampleId) {
		this.sampleId = sampleId;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getPersonId() {
		return personId;
	}

	public void setPersonId(String personId) {
		this.personId = personId;
	}

	public long getGenderConceptId() {
		return genderConceptId;
	}

	public void setGenderConceptId(long genderConceptId) {
		this.genderConceptId = genderConceptId;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public Integer getRecordCount() {
		return recordCount;
	}

	public void setRecordCount(Integer recordCount) {
		this.recordCount = recordCount;
	}
}
