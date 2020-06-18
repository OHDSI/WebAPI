package org.ohdsi.webapi.estimation.dto;

import org.ohdsi.analysis.estimation.design.EstimationTypeEnum;
import org.ohdsi.webapi.common.analyses.CommonAnalysisDTO;
import org.ohdsi.webapi.events.EntityName;

public class EstimationShortDTO extends CommonAnalysisDTO {
    private EstimationTypeEnum type = EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;
    private final EntityName entityName = EntityName.COMPARATIVE_COHORT_ANALYSIS;

    public void setType(EstimationTypeEnum type) {
        this.type = type;
    }

    public EstimationTypeEnum getType() {
        return this.type;
    }

    public EntityName getEntityName() {

        return this.entityName;
    }
}
