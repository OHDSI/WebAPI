package org.ohdsi.webapi.cohortdefinition.event;

import org.ohdsi.webapi.cohortdefinition.CohortDefinitionEntity;

public class CohortDefinitionChangedEvent {

    private CohortDefinitionEntity cohortDefinition;

    public CohortDefinitionChangedEvent(CohortDefinitionEntity cohortDefinition) {
        this.cohortDefinition = cohortDefinition;
    }

    public CohortDefinitionEntity getCohortDefinition() {
        return cohortDefinition;
    }
}
