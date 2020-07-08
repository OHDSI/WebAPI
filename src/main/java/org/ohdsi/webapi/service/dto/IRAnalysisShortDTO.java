package org.ohdsi.webapi.service.dto;

import org.ohdsi.webapi.events.EntityName;

public class IRAnalysisShortDTO extends CommonEntityDTO {

    private Integer id;
    private String name;
    private String description;
    private final EntityName entityName = EntityName.INCIDENCE_RATE;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public EntityName getEntityName() {

        return this.entityName;
    }
}
