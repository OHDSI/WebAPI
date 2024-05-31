package org.ohdsi.webapi.shiny;

import com.odysseusinc.arachne.commons.api.v1.dto.CommonAnalysisType;
import org.ohdsi.webapi.shiny.ApplicationBrief;
import org.ohdsi.webapi.shiny.PackagingStrategy;
import org.ohdsi.webapi.shiny.TemporaryFile;

import java.nio.file.Path;

public interface ShinyPackagingService {
    CommonAnalysisType getType();
    TemporaryFile packageApp(Integer analysisId, String sourceKey, PackagingStrategy packaging);
    ApplicationBrief getBrief(Integer analysisId, String sourceKey);
}
