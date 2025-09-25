package org.ohdsi.webapi.service.lock.dto;

public class GetConceptSetSnapshotItemsRequest {
	private int snapshotId;

	private ItemType snapshotItemType;

	public int getSnapshotId() {
		return snapshotId;
	}

	public void setSnapshotId(int snapshotId) {
		this.snapshotId = snapshotId;
	}

	public ItemType getSnapshotItemType() {
		return snapshotItemType;
	}

	public void setSnapshotItemType(ItemType snapshotItemType) {
		this.snapshotItemType = snapshotItemType;
	}

	public enum ItemType {
		EXPRESSION_ITEMS,
		CONCEPTS,
		SOURCE_CODES
	}
}
