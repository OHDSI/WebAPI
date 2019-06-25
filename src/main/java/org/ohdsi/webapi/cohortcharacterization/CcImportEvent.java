package org.ohdsi.webapi.cohortcharacterization;

import org.ohdsi.webapi.cohortcharacterization.domain.CohortCharacterizationEntity;

public class CcImportEvent {

    private CohortCharacterizationEntity entity;

    public CcImportEvent(CohortCharacterizationEntity entity) {

        this.entity = entity;
    }

    public CohortCharacterizationEntity getEntity() {

        return entity;
    }
}
