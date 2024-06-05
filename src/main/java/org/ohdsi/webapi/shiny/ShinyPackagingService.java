package org.ohdsi.webapi.shiny;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;

public interface ShinyPackagingService {
    CommonAnalysisType getType();
    TemporaryFile packageApp(Integer analysisId, String sourceKey, PackagingStrategy packaging);
    ApplicationBrief getBrief(Integer analysisId, String sourceKey);
}
