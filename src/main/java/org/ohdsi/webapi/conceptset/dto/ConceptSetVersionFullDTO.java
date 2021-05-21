package org.ohdsi.webapi.conceptset.dto;

import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.versioning.dto.VersionDTO;

import java.util.List;

public class ConceptSetVersionFullDTO {
    private List<ConceptSetItem> items;

    private ConceptSetDTO conceptSetDTO;

    private VersionDTO versionDTO;

    public List<ConceptSetItem> getItems() {
        return items;
    }

    public void setItems(List<ConceptSetItem> items) {
        this.items = items;
    }

    public ConceptSetDTO getConceptSetDTO() {
        return conceptSetDTO;
    }

    public void setConceptSetDTO(ConceptSetDTO conceptSetDTO) {
        this.conceptSetDTO = conceptSetDTO;
    }

    public VersionDTO getVersionDTO() {
        return versionDTO;
    }

    public void setVersionDTO(VersionDTO versionDTO) {
        this.versionDTO = versionDTO;
    }
}
