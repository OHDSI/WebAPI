package org.ohdsi.webapi.util;

import org.ohdsi.webapi.analysis.AnalysisCohortDefinition;
import org.ohdsi.webapi.estimation.specification.EstimationAnalysisImpl;
import org.ohdsi.webapi.model.CommonEntity;
import org.ohdsi.webapi.prediction.specification.PatientLevelPredictionAnalysisImpl;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.service.dto.IRAnalysisDTO;

public class ExportUtils {
    public static void clearCreateAndUpdateInfo(CommonEntityDTO commonEntityDTO) {
        commonEntityDTO.setCreatedBy(null);
        commonEntityDTO.setCreatedDate(null);
        commonEntityDTO.setModifiedBy(null);
        commonEntityDTO.setModifiedDate(null);
    }

    public static void clearCreateAndUpdateInfo(CommonEntity commonEntity) {
        commonEntity.setCreatedBy(null);
        commonEntity.setCreatedDate(null);
        commonEntity.setModifiedBy(null);
        commonEntity.setModifiedDate(null);
    }

    public static void clearCreateAndUpdateInfo(AnalysisCohortDefinition analysisCohortDefinition) {
        analysisCohortDefinition.setCreatedBy(null);
        analysisCohortDefinition.setCreatedDate(null);
        analysisCohortDefinition.setModifiedBy(null);
        analysisCohortDefinition.setModifiedDate(null);
    }

    public static void clearCreateAndUpdateInfo(EstimationAnalysisImpl analysis) {
        analysis.setCreatedBy(null);
        analysis.setCreatedDate(null);
        analysis.setModifiedBy(null);
        analysis.setModifiedDate(null);

        analysis.getCohortDefinitions().forEach(ExportUtils::clearCreateAndUpdateInfo);
    }

    public static void clearCreateAndUpdateInfo(PatientLevelPredictionAnalysisImpl analysis) {
        analysis.setCreatedBy(null);
        analysis.setCreatedDate(null);
        analysis.setModifiedBy(null);
        analysis.setModifiedDate(null);

        analysis.getCohortDefinitions().forEach(ExportUtils::clearCreateAndUpdateInfo);
    }
}
