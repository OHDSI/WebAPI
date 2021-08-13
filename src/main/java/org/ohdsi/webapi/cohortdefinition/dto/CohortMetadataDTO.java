package org.ohdsi.webapi.cohortdefinition.dto;

import org.ohdsi.webapi.cohortdefinition.CohortMetadataExt;
import org.ohdsi.webapi.service.dto.CommonEntityExtDTO;

public abstract class CohortMetadataDTO extends CommonEntityExtDTO implements CohortMetadataExt {

    private Integer id;
    private String name;
    private String description;

    public Integer getId() {

        return id;
    }

    public void setId(Integer id) {

        this.id = id;
    }

    @Override
    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    @Override
    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }
}
