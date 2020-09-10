package org.ohdsi.webapi.common.analyses;

import org.ohdsi.webapi.service.dto.CommonEntityDTO;
import org.ohdsi.webapi.user.dto.UserDTO;

import java.util.Date;

public class CommonAnalysisDTO extends CommonEntityDTO{

    private Integer id;
    private String name;
    private String description;

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
}
