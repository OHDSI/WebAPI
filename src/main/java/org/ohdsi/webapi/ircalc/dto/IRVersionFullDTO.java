package org.ohdsi.webapi.ircalc.dto;

import org.ohdsi.webapi.service.dto.IRAnalysisDTO;
import org.ohdsi.webapi.versioning.dto.VersionDTO;

public class IRVersionFullDTO {
    private IRAnalysisDTO irAnalysisDTO;

    private VersionDTO versionDTO;

    public IRAnalysisDTO getIrAnalysisDTO() {
        return irAnalysisDTO;
    }

    public void setIrAnalysisDTO(IRAnalysisDTO irAnalysisDTO) {
        this.irAnalysisDTO = irAnalysisDTO;
    }

    public VersionDTO getVersionDTO() {
        return versionDTO;
    }

    public void setVersionDTO(VersionDTO versionDTO) {
        this.versionDTO = versionDTO;
    }
}
