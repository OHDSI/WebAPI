package org.ohdsi.webapi.conceptset.dto;

import org.ohdsi.webapi.conceptset.ConceptSetItem;
import org.ohdsi.webapi.service.dto.ConceptSetDTO;
import org.ohdsi.webapi.versioning.dto.VersionFullDTO;

import java.util.List;

public class ConceptSetVersionFullDTO extends VersionFullDTO<ConceptSetDTO> {
    private List<ConceptSetItem> items;

    public List<ConceptSetItem> getItems() {
        return items;
    }

    public void setItems(List<ConceptSetItem> items) {
        this.items = items;
    }
}
