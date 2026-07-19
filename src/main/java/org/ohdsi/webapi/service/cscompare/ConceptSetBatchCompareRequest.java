package org.ohdsi.webapi.service.cscompare;

import java.util.List;

public class ConceptSetBatchCompareRequest {

	private String jobName;
	private String source1Key;
	private String source2Key;
	private String createdDateFrom;
	private String createdDateTo;
	private String updatedDateFrom;
	private String updatedDateTo;
	private List<String> tags;
	private List<Long> authors;
	private boolean compareSourceCodes;
	private List<Integer> conceptSetIds;

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public String getSource1Key() {
		return source1Key;
	}

	public void setSource1Key(String source1Key) {
		this.source1Key = source1Key;
	}

	public String getSource2Key() {
		return source2Key;
	}

	public void setSource2Key(String source2Key) {
		this.source2Key = source2Key;
	}

	public String getCreatedDateFrom() {
		return createdDateFrom;
	}

	public void setCreatedDateFrom(String createdDateFrom) {
		this.createdDateFrom = createdDateFrom;
	}

	public String getCreatedDateTo() {
		return createdDateTo;
	}

	public void setCreatedDateTo(String createdDateTo) {
		this.createdDateTo = createdDateTo;
	}

	public List<String> getTags() {
		return tags;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public String getUpdatedDateFrom() {
		return updatedDateFrom;
	}

	public void setUpdatedDateFrom(String updatedDateFrom) {
		this.updatedDateFrom = updatedDateFrom;
	}

	public String getUpdatedDateTo() {
		return updatedDateTo;
	}

	public void setUpdatedDateTo(String updatedDateTo) {
		this.updatedDateTo = updatedDateTo;
	}

	public List<Long> getAuthors() {
		return authors;
	}

	public void setAuthors(List<Long> authors) {
		this.authors = authors;
	}

	public boolean isCompareSourceCodes() {
		return compareSourceCodes;
	}

	public void setCompareSourceCodes(boolean compareSourceCodes) {
		this.compareSourceCodes = compareSourceCodes;
	}

	public List<Integer> getConceptSetIds() {
		return conceptSetIds;
	}

	public void setConceptSetIds(List<Integer> conceptSetIds) {
		this.conceptSetIds = conceptSetIds;
	}
}