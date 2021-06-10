package org.ohdsi.webapi.cohortcharacterization;

import java.util.List;

public class CcImportEvent {

    // should keep list of ids to prevent error for duplication of permissions
    private List<Integer> savedAnalysesIds;

    public CcImportEvent(List<Integer> savedAnalysesIds) {
        this.savedAnalysesIds = savedAnalysesIds;
    }

    public List<Integer> getSavedAnalysesIds() {
        return savedAnalysesIds;
    }
}
