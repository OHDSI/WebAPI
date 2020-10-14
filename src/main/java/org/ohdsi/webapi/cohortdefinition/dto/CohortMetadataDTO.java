package org.ohdsi.webapi.cohortdefinition.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.ohdsi.analysis.CohortMetadata;
import org.ohdsi.webapi.service.dto.CommonEntityDTO;

import java.util.Date;

public class CohortMetadataDTO extends CommonEntityDTO implements CohortMetadata {

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
