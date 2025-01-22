package org.ohdsi.webapi.service.lock.dto;

import java.util.HashMap;
import java.util.Map;

public class IsLockedBatchCheckResponse {
	private Map<Integer, Boolean> lockStatus;

	public IsLockedBatchCheckResponse() {
		this.lockStatus = new HashMap<>();
	}

	public Map<Integer, Boolean> getLockStatus() {
		return lockStatus;
	}

	public void setLockStatus(Map<Integer, Boolean> lockStatus) {
		this.lockStatus = lockStatus;
	}

}
