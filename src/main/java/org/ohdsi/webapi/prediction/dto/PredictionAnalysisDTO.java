package org.ohdsi.webapi.prediction.dto;

import org.ohdsi.webapi.common.analyses.CommonAnalysisDTO;
import org.ohdsi.webapi.events.EntityName;

public class PredictionAnalysisDTO extends CommonAnalysisDTO {

    private String specification;
    private final EntityName entityName = EntityName.PATIENT_LEVEL_PREDICTION; 

    public String getSpecification() {

        return specification;
    }

    public void setSpecification(String specification) {

        this.specification = specification;
    }
    
    public EntityName getEntityName() {
        
        return this.entityName;
    }
}
