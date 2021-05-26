package org.ohdsi.webapi.pathway.dto;

import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.ohdsi.webapi.versioning.dto.VersionDTO;

public class PathwayVersionFullDTO {
    private PathwayAnalysisDTO pathwayAnalysisDTO;

    private VersionDTO versionDTO;

    public PathwayAnalysisDTO getPathwayAnalysisDTO() {
        return pathwayAnalysisDTO;
    }

    public void setPathwayAnalysisDTO(PathwayAnalysisDTO pathwayAnalysisDTO) {
        this.pathwayAnalysisDTO = pathwayAnalysisDTO;
    }

    public VersionDTO getVersionDTO() {
        return versionDTO;
    }

    public void setVersionDTO(VersionDTO versionDTO) {
        this.versionDTO = versionDTO;
    }
}
