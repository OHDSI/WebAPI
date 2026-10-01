package org.ohdsi.webapi.service.lock.dto;

import java.util.List;

public class IsLockedBatchCheckRequest {

	private List<Integer> conceptSetIds;

	public List<Integer> getConceptSetIds() {
		return conceptSetIds;
	}

	public void setConceptSetIds(List<Integer> conceptSetIds) {
		this.conceptSetIds = conceptSetIds;
	}
}
