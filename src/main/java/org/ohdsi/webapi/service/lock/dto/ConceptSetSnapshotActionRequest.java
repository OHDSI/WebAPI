package org.ohdsi.webapi.service.lock.dto;

public class ConceptSetSnapshotActionRequest {
	private String sourceKey;
	private String action;
	private String user;
	private String message;
	private boolean takeSnapshot;


	public String getSourceKey() {
		return sourceKey;
	}

	public void setSourceKey(String sourceKey) {
		this.sourceKey = sourceKey;
	}


	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public boolean isTakeSnapshot() {
		return takeSnapshot;
	}

	public void setTakeSnapshot(boolean takeSnapshot) {
		this.takeSnapshot = takeSnapshot;
	}
}
