package org.ohdsi.webapi.service.dto;

import org.ohdsi.webapi.service.lock.dto.ConceptSetSnapshotParameters;

public class LockedConceptSetsResponse {
    private ConceptSetDTO conceptSet;
    private ConceptSetSnapshotParameters snapshotParameters;

    public LockedConceptSetsResponse(ConceptSetDTO conceptSet, ConceptSetSnapshotParameters snapshotParameters) {
        this.conceptSet = conceptSet;
        this.snapshotParameters = snapshotParameters;
    }


    public ConceptSetSnapshotParameters getSnapshotParameters() {
        return snapshotParameters;
    }

    public void setSnapshotParameters(ConceptSetSnapshotParameters snapshotParameters) {
        this.snapshotParameters = snapshotParameters;
    }

    public ConceptSetDTO getConceptSet() {
        return conceptSet;
    }

    public void setConceptSet(ConceptSetDTO conceptSet) {
        this.conceptSet = conceptSet;
    }
}
