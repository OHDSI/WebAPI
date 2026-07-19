package org.ohdsi.webapi.service.lock.dto;

public class ConceptSetSnapshotActionRequest {
    private String sourceKey;
    private SnapshotAction action;
    private String message;
    private boolean takeSnapshot;

    public String getSourceKey() {
        return sourceKey;
    }

    public void setSourceKey(String sourceKey) {
        this.sourceKey = sourceKey;
    }

    public SnapshotAction getAction() {
        return action;
    }

    public void setAction(SnapshotAction action) {
        this.action = action;
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
