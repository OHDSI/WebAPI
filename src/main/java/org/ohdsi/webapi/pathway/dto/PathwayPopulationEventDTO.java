package org.ohdsi.webapi.pathway.dto;

import org.ohdsi.analysis.pathway.result.PathwayPopulationEvent;

public class PathwayPopulationEventDTO implements PathwayPopulationEvent {

    private String path;
    private Integer personCount;

    public PathwayPopulationEventDTO(String path, Integer personCount) {

        this.path = path;
        this.personCount = personCount;
    }

    public String getPath() {

        return path;
    }

    public void setPath(String path) {

        this.path = path;
    }

    public Integer getPersonCount() {

        return personCount;
    }

    public void setPersonCount(Integer personCount) {

        this.personCount = personCount;
    }
}
