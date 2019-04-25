package org.ohdsi.webapi.prediction.dto;

import org.ohdsi.webapi.common.analyses.CommonAnalysisDTO;

public class PredictionAnalysisDTO extends CommonAnalysisDTO {

    private String specification;

    public String getSpecification() {

        return specification;
    }

    public void setSpecification(String specification) {

        this.specification = specification;
    }
}
