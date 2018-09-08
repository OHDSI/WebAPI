package org.ohdsi.webapi.pathway.dto;

import org.ohdsi.webapi.user.dto.UserDTO;
import org.ohdsi.webapi.cohortdefinition.dto.CohortMetadataDTO;

import java.util.Date;
import java.util.List;

public abstract class BasePathwayAnalysisDTO<T extends CohortMetadataDTO> {

    private Long id;
    private String name;
    private List<T> targetCohorts;
    private List<T> eventCohorts;
    private Integer combinationWindow;
    private Integer minCellCount;
    private Integer maxDepth;
    private UserDTO createdBy;
    private Date createdAt;
    private UserDTO updatedBy;
    private Date updatedAt;

    public Long getId() {

        return id;
    }

    public void setId(Long id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public List<T> getTargetCohorts() {

        return targetCohorts;
    }

    public void setTargetCohorts(List<T> targetCohorts) {

        this.targetCohorts = targetCohorts;
    }

    public List<T> getEventCohorts() {

        return eventCohorts;
    }

    public void setEventCohorts(List<T> eventCohorts) {

        this.eventCohorts = eventCohorts;
    }

    public Integer getCombinationWindow() {

        return combinationWindow;
    }

    public void setCombinationWindow(Integer combinationWindow) {

        this.combinationWindow = combinationWindow;
    }

    public Integer getMinCellCount() {

        return minCellCount;
    }

    public void setMinCellCount(Integer minCellCount) {

        this.minCellCount = minCellCount;
    }

    public Integer getMaxDepth() {

        return maxDepth;
    }

    public void setMaxDepth(Integer maxDepth) {

        this.maxDepth = maxDepth;
    }

    public UserDTO getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(UserDTO createdBy) {

        this.createdBy = createdBy;
    }

    public Date getCreatedAt() {

        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {

        this.createdAt = createdAt;
    }

    public UserDTO getUpdatedBy() {

        return updatedBy;
    }

    public void setUpdatedBy(UserDTO updatedBy) {

        this.updatedBy = updatedBy;
    }

    public Date getUpdatedAt() {

        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {

        this.updatedAt = updatedAt;
    }
}
