package org.ohdsi.webapi.cohortcharacterization.dto;

import org.ohdsi.webapi.versioning.dto.VersionDTO;

public class CcVersionFullDTO {
    private CohortCharacterizationDTO characterizationDTO;

    private VersionDTO versionDTO;

    public CohortCharacterizationDTO getCharacterizationDTO() {
        return characterizationDTO;
    }

    public void setCharacterizationDTO(CohortCharacterizationDTO characterizationDTO) {
        this.characterizationDTO = characterizationDTO;
    }

    public VersionDTO getVersionDTO() {
        return versionDTO;
    }

    public void setVersionDTO(VersionDTO versionDTO) {
        this.versionDTO = versionDTO;
    }
}
