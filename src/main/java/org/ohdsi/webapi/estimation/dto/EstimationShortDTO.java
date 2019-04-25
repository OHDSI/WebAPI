package org.ohdsi.webapi.estimation.dto;

import org.ohdsi.analysis.estimation.design.EstimationTypeEnum;
import org.ohdsi.webapi.common.analyses.CommonAnalysisDTO;

public class EstimationShortDTO extends CommonAnalysisDTO {
    private EstimationTypeEnum type = EstimationTypeEnum.COMPARATIVE_COHORT_ANALYSIS;

    public void setType(EstimationTypeEnum type) {
        this.type = type;
    }

    public EstimationTypeEnum getType() {
        return this.type;
    }
}
