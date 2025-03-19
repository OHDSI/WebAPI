package org.ohdsi.webapi.service.lock.dto;

public class ConceptSetSnapshotParameters {
	private Long snapshotId;
	private SnapshotAction action;
	private String snapshotDate;
	private String user;
	private String vocabularyBundleName;
	private String vocabularyBundleSchema;
	private String vocabularyBundleVersion;
	private String conceptSetVersion;
	private String message;

	public String getSnapshotDate() {
		return snapshotDate;
	}

	public void setSnapshotDate(String snapshotDate) {
		this.snapshotDate = snapshotDate;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getVocabularyBundleName() {
		return vocabularyBundleName;
	}

	public void setVocabularyBundleName(String vocabularyBundleName) {
		this.vocabularyBundleName = vocabularyBundleName;
	}

	public String getVocabularyBundleSchema() {
		return vocabularyBundleSchema;
	}

	public void setVocabularyBundleSchema(String vocabularyBundleSchema) {
		this.vocabularyBundleSchema = vocabularyBundleSchema;
	}

	public String getVocabularyBundleVersion() {
		return vocabularyBundleVersion;
	}

	public void setVocabularyBundleVersion(String vocabularyBundleVersion) {
		this.vocabularyBundleVersion = vocabularyBundleVersion;
	}

	public String getConceptSetVersion() {
		return conceptSetVersion;
	}

	public void setConceptSetVersion(String conceptSetVersion) {
		this.conceptSetVersion = conceptSetVersion;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public SnapshotAction getAction() {
		return action;
	}

	public void setAction(SnapshotAction action) {
		this.action = action;
	}

	public Long getSnapshotId() {
		return snapshotId;
	}

	public void setSnapshotId(Long snapshotId) {
		this.snapshotId = snapshotId;
	}
}
