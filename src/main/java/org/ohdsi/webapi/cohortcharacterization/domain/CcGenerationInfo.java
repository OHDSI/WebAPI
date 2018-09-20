package org.ohdsi.webapi.cohortcharacterization.domain;

import org.ohdsi.analysis.cohortcharacterization.design.CohortCharacterization;
import org.ohdsi.webapi.shiro.Entities.UserEntity;

public interface CcGenerationInfo {

    CohortCharacterization getDesign();
    Integer getHashCode();
    UserEntity getCreatedBy();
}
