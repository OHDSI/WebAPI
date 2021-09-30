package org.ohdsi.webapi.cohortdefinition.event;

import org.ohdsi.webapi.cohortdefinition.CohortDefinition;

public class CohortDefinitionChangedEvent {

    private CohortDefinition cohortDefinition;

    public CohortDefinitionChangedEvent(CohortDefinition cohortDefinition) {
        this.cohortDefinition = cohortDefinition;
    }

    public CohortDefinition getCohortDefinition() {
        return cohortDefinition;
    }
}
