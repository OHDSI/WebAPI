package org.ohdsi.webapi.service.lock.dto;

import org.ohdsi.circe.vocabulary.ConceptSetExpression;

import java.util.List;

public class GetConceptSetSnapshotItemsResponse {
	private List<ConceptSetExpression.ConceptSetItem> conceptSetItems;

	public List<ConceptSetExpression.ConceptSetItem> getConceptSetItems() {
		return conceptSetItems;
	}

	public void setConceptSetItems(List<ConceptSetExpression.ConceptSetItem> conceptSetItems) {
		this.conceptSetItems = conceptSetItems;
	}
}
