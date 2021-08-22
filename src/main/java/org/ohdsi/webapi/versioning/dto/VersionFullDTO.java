package org.ohdsi.webapi.versioning.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VersionFullDTO<T> {
    private T entityDTO;
    private VersionDTO versionDTO;

    public T getEntityDTO() {
        return entityDTO;
    }

    public void setEntityDTO(T entityDTO) {
        this.entityDTO = entityDTO;
    }

    public VersionDTO getVersionDTO() {
        return versionDTO;
    }

    public void setVersionDTO(VersionDTO versionDTO) {
        this.versionDTO = versionDTO;
    }
}
