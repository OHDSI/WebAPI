package org.ohdsi.webapi.cohortsample;

/** A single person that is part of a given sample. */
public class SampleElement {
	private int sampleId;

	private int rank;

	private long personId;

	private long genderConceptId;

	private int age;

	private Integer recordCount;

	public int getSampleId() {
		return sampleId;
	}

	public void setSampleId(int sampleId) {
		this.sampleId = sampleId;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public long getPersonId() {
		return personId;
	}

	public void setPersonId(long personId) {
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
